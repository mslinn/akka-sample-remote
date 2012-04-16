name := "akka-sample-remote"

version := "0.1"

scalaVersion := "2.9.1-1"

scalacOptions ++= Seq("-deprecation")

resolvers ++= Seq(
  "Typesafe Snapshots"    at "http://repo.typesafe.com/typesafe/snapshots",
  "Typesafe Releases"     at "http://repo.typesafe.com/typesafe/releases",
  "Scala-Tools Snapshots" at "http://scala-tools.org/repo-snapshots",
  "Scala Tools Releases"  at "http://scala-tools.org/repo-releases"
)

libraryDependencies ++= Seq(
 // "com.typesafe.akka" % "akka-sbt-plugin" % "latest.milestone",
  "com.typesafe.akka" % "akka-actor"      % "latest.milestone" withSources(),
  "com.typesafe.akka" % "akka-remote"     % "latest.milestone" withSources(),
  "com.typesafe.akka" % "akka-kernel"     % "latest.milestone" withSources(),
  "com.typesafe.akka" % "akka-slf4j"      % "latest.milestone" withSources(),
  "ch.qos.logback"    % "logback-classic" % "1.0.0"
)