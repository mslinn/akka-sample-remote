package sample.remote.calculator

import scala.util.Random
import java.io.File
import akka.actor._
import akka.kernel.Bootable
import akka.remote._
import com.typesafe.config.ConfigFactory
import akka.routing.RoundRobinRouter

class LookupApplication extends Bootable {
  println("application.conf found in %s: %s".format(new File(".").getCanonicalPath, new File("application.conf").exists()))
  val system = ActorSystem("CalculatorApplication", ConfigFactory.load.getConfig("remotelookup"))
  val actorRef = system.actorOf(Props[LookupActor], "lookupActor")

  // look up remote actor; must exist or messages cannot be sent
  //val remoteActorRef = system.actorFor("akka://CalculatorApplication@127.0.0.1:2552/user/simpleCalculator")
  // create remote actor; starts a new instance of CalculatorApplication
  val remoteActorRef = system.actorOf(Props[SimpleCalculatorActor].withRouter(RoundRobinRouter(1)))

  /* RemoteClientLifeCycleEvent and the following case classes for RemoteClient and RemoteServer are defined in RemoteTransport.scala
  * Mar 29/12: From https://groups.google.com/forum/?fromgroups#!searchin/akka-user/RemoteClientDisconnected/akka-user/xv6jVAz0OPs/lMKnPWWKVe4J
  * "DeathWatch and the RemoteTransport are not yet hooked up, that is planned for when clustering comes along and brings
  * with it nice failure detectors etc. What you can do in 2.0 is to subscribe to the RemoteClientWriteFailed (and similar) messages
  * on the system.eventStream and react to those basically as you would to a Terminated() message; in that sense
  * system.eventStream.subscribe(self, RemoteClientDisconnected) replaces context.watch() for your use-case. Keep in mind, though,
  * that a broken connection may be reestablished and then you declared some actors “dead” which actually are still alive; this small
  * complication is the reason why that feature is waiting for the cluster membership support so that this “declared dead” step can
  * be done reliably."*/
  val listener = system.actorOf(Props(new Actor {
    def receive = {
      // does not trigger
      case d: DeadLetter ⇒ println(d)

      // triggers
      case RemoteClientError(cause, remote, remoteAddress) ⇒
        println("RemoteClientError %s %s at %s".format(cause, remote, remoteAddress))

      // triggers after RemoteClientShutdown; does not trigger if client is never connected
      case RemoteClientDisconnected(remote, remoteAddress)  =>
        println("RemoteClientDisconnected %s at %s".format(remote, remoteAddress))

      // triggers before RemoteClientStarted
      case RemoteClientConnected(remote, remoteAddress)  =>
        println("RemoteClientConnected %s at %s".format(remote, remoteAddress))

      // triggers
      case RemoteClientShutdown(remote, remoteAddress) =>
        println("RemoteClientShutdown %s at %s".format(remote, remoteAddress))

      // triggers after RemoteClientConnected
      case RemoteClientStarted(remote, remoteAddress)  =>
        println("RemoteClientStarted %s at %s".format(remote, remoteAddress))

      // triggers
      case RemoteClientWriteFailed(request, cause, remote, remoteAddress) =>
        println("RemoteClientWriteFailed %s %s %s at %s".format(request, cause, remote, remoteAddress))

      // does not trigger
      case RemoteServerStarted(remote)  =>
        println("RemoteServerStarted %s".format(remote))

      // does not trigger
      case RemoteServerShutdown(remote)  =>
        println("RemoteServerShutdown %s".format(remote))

      // triggers if execution of remote server is stopped after RemoteServerClientConnected
      case RemoteServerError(cause, remote)  =>
        println("RemoteServerError %s %s".format(cause, remote))

      // triggers after a few RemoteClientStarted and RemoteClientShutdown
      case RemoteServerClientConnected(remote, clientAddress)  =>
        println("RemoteServerClientConnected %s, %s".format(remote, clientAddress))

      // triggers after RemoteServerClientConnected
      case RemoteServerClientDisconnected(remote, clientAddress)  =>
        println("RemoteServerClientDisconnected %s at %s".format(remote, clientAddress))

      // triggers after RemoteClientStarted
      case RemoteServerClientClosed(remote, clientAddress)  =>
        println("RemoteServerClientClosed %s at %s".format(remote, clientAddress))

      // does not trigger
      case msg =>
        println(msg)
    }
  }), "RemoteClientLifeCycleListener")

  system.eventStream.subscribe(listener, classOf[RemoteLifeCycleEvent])
  system.eventStream.subscribe(listener, classOf[DeadLetter])

  def doSomething(op: MathOp) = { actorRef ! (remoteActorRef, op) }

  // ScalaDoc says "Callback run on microkernel startup" but this method never gets called
  def startup() {
    println("LookupApplication started")
  }

  def shutdown() {
    system.shutdown()
  }
}

class LookupActor extends Actor with ActorLogging {

  override def preStart() = {
    log.debug("Starting LookupActor")
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting LookupActor due to [{}] when processing [{}]", reason.getMessage, message.getOrElse(""))
  }

  def receive = {
    case (actor: ActorRef, op: MathOp) ⇒
      try {
        actor ! op
      } catch {
        case t => println("Problem " + t)
      }

    case result: MathResult ⇒ result match {
      case AddResult(n1, n2, r)      ⇒ println("Add result: %d + %d = %d".format(n1, n2, r))
      case SubtractResult(n1, n2, r) ⇒ println("Sub result: %d - %d = %d".format(n1, n2, r))
      case x                         ⇒ log.warning("Unknown MathResult: {}", x)
    }

    case x ⇒ log.warning("LookupActor received unknown message: {}", x)
  }
}

object LookupApp {
  def main(args: Array[String]) {
    val app = new LookupApplication
    while (true) {
      if (Random.nextInt(100) % 2 == 0)
        app.doSomething(Add(Random.nextInt(100), Random.nextInt(100)))
      else
        app.doSomething(Subtract(Random.nextInt(100), Random.nextInt(100)))
      Thread.sleep(200)
    }
  }
}
