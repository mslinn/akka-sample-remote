Taken from [Akka 2.0 akka-sample-remote example](https://github.com/akka/akka/tree/master/akka-samples/akka-sample-remote).
I created this project in order to figure out how it works and how to run it.
The Java files were deleted because I was not interested in them.
This project uses the Akka microkernel. It contains 3 apps that extend <tt>Bootable</tt>. 

I tested with IntelliJ IDEA 11; included with this git project are 3 IDEA run configurations.
These run configurations launch the programs from <tt>target/scala-2.9.1-1/classes</tt> so <tt>application.conf</tt> and <tt>common.conf</tt> are found.

1.  The Scaladoc for <tt>Bootable.startup()</tt> says "Callback run on microkernel startup" but <tt>startup()</tt> never gets called. Is this a bug in the documentation or in Akka?
2.  I updated the syntax of <tt>common.conf</tt> to match the current docs.
3.  I added subscriptions to actor lifecyle events in <tt>LookupApplication</tt>; some comments and questions are embedded there.
4.  I changed <tt>LookupApplication</tt> so it would launch <tt>CalculatorApplication</tt> using a programmatically specified router.
<pre>val remoteActorRef = system.actorOf(Props[SimpleCalculatorActor].withRouter(RoundRobinRouter(1)))</pre>
Even when only 1 instance of CalculatorApplication is requested, I see 3 created. When I specify 5, 7 are created. Why is that?
5.  In <tt>CreationApplication</tt>, the following code gives the remote actor the same name as the router (<tt>advancedCalculator</tt>):
<pre>val remoteActorRef = system.actorOf(Props[AdvancedCalculatorActor], "advancedCalculator")</pre>
Looks like this is the way to create a remote actor reference, because if I alter the name in the call, it does not work the same.
However, it does not start <tt>CalculatorApplication</tt>.
The [docs](http://doc.akka.io/docs/akka/2.0/scala/remoting.html#Creating_Actors_Remotely) say that the above should launch a remote actor. What is wrong?

<tt>LookupApplication</tt> now launches <tt>CreationApplication</tt>.
As with the original program, if <tt>CalculatorApplication</tt> is not launched before <tt>CreationApplication</tt> then errors occur until
<tt>CalculatorApplication</tt> is launched.


Original Docs, Edited
=====================

There are three actor systems used in the sample:

* CalculatorApplication : the actor system performing the number crunching
* LookupApplication     : illustrates how to look up an actor on a remote node and and how communicate with that actor
* CreationApplication   : illustrates how to create an actor on a remote node and how to communicate with that actor

The CalculatorApplication contains an actor, SimpleCalculatorActor, which can handle simple math operations such as
addition and subtraction. This actor is looked up and used from the LookupApplication.

The CreationApplication wants to use more "advanced" mathematical operations, such as multiplication and division,
but as the CalculatorApplication does not have any actor that can perform those type of calculations the
CreationApplication has to remote deploy an actor that can (which in our case is AdvancedCalculatorActor).
So this actor is deployed, over the network, onto the CalculatorApplication actor system and thereafter the
CreationApplication will send messages to it.

It is important to point out that as the actor system run on different ports it is possible to run all three in parallel.
See the next section for more information of how to run the sample application.

Running
-------

In order to run all three actor systems you have to start SBT in three different terminal windows.

We start off by running the CalculatorApplication:

First type 'sbt' to start SBT interactively, the run 'update' and 'run':
> cd $AKKA_HOME

> sbt

> project akka-sample-remote

> run

Select to run "sample.remote.calculator.CalcApp" which in the case below is number 3:

    Multiple main classes detected, select one to run:

    [1] sample.remote.calculator.LookupApp
    [2] sample.remote.calculator.CreationApp
    [3] sample.remote.calculator.CalcApp

    Enter number: 3

You should see something similar to this::

    [info] Running sample.remote.calculator.CalcApp
    [INFO] [12/22/2011 14:21:51.631] [run-main] [ActorSystem] REMOTE: RemoteServerStarted@akka://CalculatorApplication@127.0.0.1:2552
    [INFO] [12/22/2011 14:21:51.632] [run-main] [Remote] Starting remote server on [akka://CalculatorApplication@127.0.0.1:2552]
    Started Calculator Application - waiting for messages
    [INFO] [12/22/2011 14:22:39.894] [New I/O server worker #1-1] [ActorSystem] REMOTE: RemoteClientStarted@akka://127.0.0.1:2553

Open up a new terminal window and run SBT once more:

> sbt

> project akka-sample-remote

> run

Select to run "sample.remote.calculator.LookupApp" which in the case below is number 1::

    Multiple main classes detected, select one to run:

    [1] sample.remote.calculator.LookupApp
    [2] sample.remote.calculator.CreationApp
    [3] sample.remote.calculator.CalcApp

    Enter number: 1

Now you should see something like this::

    [info] Running sample.remote.calculator.LookupApp
    [INFO] [12/22/2011 14:54:38.630] [run-main] [ActorSystem] REMOTE: RemoteServerStarted@akka://LookupApplication@127.0.0.1:2553
    [INFO] [12/22/2011 14:54:38.632] [run-main] [Remote] Starting remote server on [akka://LookupApplication@127.0.0.1:2553]
    Started Lookup Application
    [INFO] [12/22/2011 14:54:38.801] [default-dispatcher-21] [ActorSystem] REMOTE: RemoteClientStarted@akka://127.0.0.1:2552
    Sub result: 4 - 30 = -26
    Add result: 17 + 1 = 18
    Add result: 37 + 43 = 80
    Add result: 68 + 66 = 134

Congrats! You have now successfully looked up a remote actor and communicated with it.
The next step is to have an actor deployed on a remote note.
Once more you should open a new terminal window and run SBT:

> sbt

> project akka-sample-remote

> run

Select to run "sample.remote.calculator.CreationApp" which in the case below is number 2::

    Multiple main classes detected, select one to run:

    [1] sample.remote.calculator.LookupApp
    [2] sample.remote.calculator.CreationApp
    [3] sample.remote.calculator.CalcApp

    Enter number: 2

Now you should see something like this::

    [info] Running sample.remote.calculator.CreationApp
    [INFO] [12/22/2011 14:57:02.150] [run-main] [ActorSystem] REMOTE: RemoteServerStarted@akka://RemoteCreation@127.0.0.1:2554
    [INFO] [12/22/2011 14:57:02.151] [run-main] [Remote] Starting remote server on [akka://RemoteCreation@127.0.0.1:2554]
    [INFO] [12/22/2011 14:57:02.267] [default-dispatcher-21] [ActorSystem] REMOTE: RemoteClientStarted@akka://127.0.0.1:2552
    Started Creation Application
    Mul result: 14 * 17 = 238
    Div result: 3764 / 80 = 47.00
    Mul result: 16 * 5 = 80
    Mul result: 1 * 18 = 18
    Mul result: 8 * 13 = 104

That's it!

Notice
------

The sample application is just that, i.e. a sample. Parts of it are not the way you would do a "real" application.
Some improvements are to remove all hard coded addresses from the code as they reduce the flexibility of how and
where the application can be run. We leave this to the astute reader to refine the sample into a real-world app.

* `Akka <http://akka.io/>`_
* `SBT <http://https://github.com/harrah/xsbt/wiki/>`_
