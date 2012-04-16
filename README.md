Taken from [Akka 2.0 akka-sample-remote example](https://github.com/akka/akka/tree/master/akka-samples/akka-sample-remote).
I created this project in order to figure out how to run it, which meant that the errors it contained needed to be fixed first.
The Java files were deleted because I was not interested in them.
This project uses the Akka microkernel. It contains 3 apps that extend <tt>Bootable</tt>. 
I am testing with IntelliJ IDEA 11; included with this project are 3 run configurations. 
These run configurations launch the programs from <tt>target/scala-2.9.1-1/classes</tt> so <tt>application.conf</tt> and <tt>common.conf</tt> are found.
1. The Scaladoc for Bootable says "Callback run on microkernel startup" but startup() never gets called.
2. LookupApplication does not find CalculatorApplication. 
The message is <tt>look-up of unknown path [akka://CalculatorApplication/user/simpleCalculator] failed</tt>.
3. I updated the syntax of <tt>common.conf</tt> to match the current docs.
4. <tt>application.conf</tt> references a router (<tt>advancedCalculator</tt>) that is undefined.
5. The application configurations did not load from <tt>application.conf</tt>. I replaced this code:
<pre>val system = ActorSystem("CalculatorApplication", ConfigFactory.load.getConfig("calculator"))</pre>
with this code:
<pre>val config = ConfigFactory.load(ConfigFactory.parseFile(new File("application.conf")))
val system = ActorSystem("CalculatorApplication", config.getConfig("calculator"))</pre>
