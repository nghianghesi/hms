name := "HMS"
organization := "letterbllc"

scalaVersion in ThisBuild := "2.11.12"
//scalaVersion in ThisBuild := "2.12.4"

scalacOptions in ThisBuild := Seq("-Xexperimental", "-Xlint:_", "-unchecked", "-deprecation", "-feature", "-target:jvm-1.8")
// Unfortunately there are lots of annoying warnings from the generated Protobuf code:
// javacOptions in ThisBuild := Seq("-Xlint:all")

resolvers += "Local Cached Maven Repository" at Path.userHome.asFile.toURI.toURL + ".ivy2/cache"

lazy val protocol = (project in file("./protocol"))


lazy val client = (project in file("./client"))
  .settings(libraryDependencies ++=  Seq(
      "junit" % "junit" % "4.12" % "test",
      "com.novocode" % "junit-interface" % "0.11" % "test",
	  "com.squareup.retrofit2" % "converter-gson" % "2.5.0",
      "com.squareup.retrofit2" % "retrofit" % "2.5.0",
	  "org.slf4j" % "slf4j-api" % "1.7.25",
	  "org.slf4j" % "slf4j-log4j12" % "1.7.25"))
  .dependsOn(protocol)	
  
  
lazy val service = (project in file("./service"))
	.enablePlugins(PlayJava)
	.settings(libraryDependencies ++=  Seq(
		guice,
		"org.mongodb" % "mongo-java-driver" % "3.10.1",
		"xyz.morphia.morphia" % "core" % "1.4.0",
		"org.modelmapper" % "modelmapper" % "2.3.0"
	))
	.dependsOn(protocol)	  
	
lazy val global = project
  .in(file("."))
  .aggregate(
    protocol,
    client,
    service
  )