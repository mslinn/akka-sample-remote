package sample.remote.calculator

import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import scala.util.Random
import akka.actor._
import java.io.File

class CreationApplication extends Bootable {
  println("application.conf found in %s: %s".format(new File(".").getCanonicalPath, new File("application.conf").exists()))
  val system      = ActorSystem("RemoteCreation", ConfigFactory.load.getConfig("remotecreation"))
  val localActorRef  = system.actorOf(Props[CreationActor],           "creationActor")
  val remoteActorRef = system.actorOf(Props[AdvancedCalculatorActor], "advancedCalculator")

  def doSomething(op: MathOp) = {
    localActorRef ! (remoteActorRef, op)
  }

  // ScalaDoc says "Callback run on microkernel startup" but this method never gets called
  def startup() {
    println("CreationApplication started")
  }

  def shutdown() {
    system.shutdown()
  }
}

class CreationActor extends Actor {
  def receive = {
    case (actor: ActorRef, op: MathOp) ⇒ actor ! op
    case result: MathResult ⇒ result match {
      case MultiplicationResult(n1, n2, r) ⇒ println("Mul result: %d * %d = %d".format(n1, n2, r))
      case DivisionResult(n1, n2, r)       ⇒ println("Div result: %.0f / %d = %.2f".format(n1, n2, r))
    }
  }
}

object CreationApp {
  def main(args: Array[String]) {
    val app = new CreationApplication
    println("Started Creation Application")
    while (true) {
      if (Random.nextInt(100) % 2 == 0)
        app.doSomething(Multiply(Random.nextInt(20), Random.nextInt(20)))
      else
        app.doSomething(Divide(Random.nextInt(10000), (Random.nextInt(99) + 1)))
      Thread.sleep(200)
    }
  }
}
