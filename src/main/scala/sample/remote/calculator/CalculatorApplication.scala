package sample.remote.calculator

import akka.kernel.Bootable
import akka.actor.{ Props, Actor, ActorSystem }
import com.typesafe.config.ConfigFactory
import java.io.File

class SimpleCalculatorActor extends Actor {
  def receive = {
    case Add(n1, n2) ⇒
      println("Calculating %d + %d".format(n1, n2))
      sender ! AddResult(n1, n2, n1 + n2)
    case Subtract(n1, n2) ⇒
      println("Calculating %d - %d".format(n1, n2))
      sender ! SubtractResult(n1, n2, n1 - n2)
  }
}

class CalculatorApplication extends Bootable {
  println("application.conf found in %s: %s".format(new File(".").getCanonicalPath, new File("application.conf").exists()))
  val config = ConfigFactory.load(ConfigFactory.parseFile(new File("application.conf")))
  val system = ActorSystem("CalculatorApplication", config.getConfig("calculator"))
  val actor = system.actorOf(Props[SimpleCalculatorActor], "simpleCalculator")
  println(actor.path)

  // Scaladoc says "Callback run on microkernel startup" but this method never gets called
  def startup() {
    println(actor.path)
  }

  def shutdown() {
    system.shutdown()
  }
}

object CalcApp {
  def main(args: Array[String]) {
    new CalculatorApplication
    println("Started CalculatorApplication - waiting for messages")
  }
}
