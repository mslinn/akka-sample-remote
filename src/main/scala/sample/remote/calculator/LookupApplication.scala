package sample.remote.calculator

import akka.kernel.Bootable
import scala.util.Random
import java.io.File

import com.typesafe.config.ConfigFactory
import akka.actor.{ ActorRef, Props, Actor, ActorSystem }

class LookupApplication extends Bootable {
  println("application.conf found in %s: %s".format(new File(".").getCanonicalPath, new File("application.conf").exists()))
  val system = ActorSystem("CalculatorApplication", ConfigFactory.load.getConfig("remotelookup"))
  val actorRef = system.actorOf(Props[LookupActor], "lookupActor")

  val remoteActorRef = system.actorFor("akka://CalculatorApplication@127.0.0.1:2552/user/simpleCalculator")

  def doSomething(op: MathOp) = { actorRef ! (remoteActorRef, op) }

  // Scaladoc says "Callback run on microkernel startup" but this method never gets called
  def startup() {
    println("LookupApplication started")
  }

  def shutdown() {
    system.shutdown()
  }
}

class LookupActor extends Actor {
  def receive = {
    case (actor: ActorRef, op: MathOp) ⇒ actor ! op
    case result: MathResult ⇒ result match {
      case AddResult(n1, n2, r)      ⇒ println("Add result: %d + %d = %d".format(n1, n2, r))
      case SubtractResult(n1, n2, r) ⇒ println("Sub result: %d - %d = %d".format(n1, n2, r))
    }
  }
}

object LookupApp {
  def main(args: Array[String]) {
    val app = new LookupApplication
    println("Started LookupApplication")
    while (true) {
      if (Random.nextInt(100) % 2 == 0)
        app.doSomething(Add(Random.nextInt(100), Random.nextInt(100)))
      else
        app.doSomething(Subtract(Random.nextInt(100), Random.nextInt(100)))
      Thread.sleep(200)
    }
  }
}
