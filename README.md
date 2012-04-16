Taken from [Akka 2.0 akka-sample-remote example](https://github.com/akka/akka/tree/master/akka-samples/akka-sample-remote).
I created this project in order to figure out how to run it.
The Java files were deleted because I was not interested in them.
This project uses the Akka microkernel. It contains 3 apps that extend <tt>Bootable</tt>. 

I am testing with IntelliJ IDEA 11; included with this project are 3 run configurations. 
These run configurations launch the programs from <tt>target/scala-2.9.1-1/classes</tt> so <tt>application.conf</tt> and <tt>common.conf</tt> are found.
I hope that this does not introduce problems.

1.  The Scaladoc for <tt>Bootable.startup()</tt> says "Callback run on microkernel startup" but <tt>startup()</tt> never gets called.
2.  I updated the syntax of <tt>common.conf</tt> to match the current docs.
3.  In <tt>CreationApplication</tt>, the following code give the remote actor the same name as the router (<tt>advancedCalculator</tt>):
<pre>val remoteActorRef = system.actorOf(Props[AdvancedCalculatorActor], "advancedCalculator")</pre>
Looks like this is the way to create a remote actor reference, because if I alter the name in the call, it does not work the same.

The only way I could get these applications to run without error was to launch them in this order:

1. <tt>CalculatorApplication</tt>
2. <tt>CreationApplication</tt> or <tt>LookupApplication</tt>

I thought that <tt>CreationApplication</tt> or <tt>LookupApplication</tt> might launch <tt>CalculatorApplication</tt>, but it does not. Should it?
