name := """play-java"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

resolvers += "jitpack" at "https://jitpack.io"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)

libraryDependencies += "com.github.Grover-c13" % "PokeGOAPI-Java" % "0.3"
libraryDependencies += "commons-collections" % "commons-collections" % "3.2.1"

fork in run := true