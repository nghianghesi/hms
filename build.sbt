name := "HMS"
organization := "letterbllc"

scalaVersion in ThisBuild := "2.11.12"
//scalaVersion in ThisBuild := "2.12.4"

scalacOptions in ThisBuild := Seq("-Xexperimental", "-Xlint:_", "-unchecked", "-deprecation", "-feature", "-target:jvm-1.8")
// Unfortunately there are lots of annoying warnings from the generated Protobuf code:
// javacOptions in ThisBuild := Seq("-Xlint:all")

resolvers += "Local Cached Maven Repository" at Path.userHome.asFile.toURI.toURL + ".ivy2/cache"
unmanagedBase := baseDirectory.value / "lib"

lazy val dependencies =
  new {
	val junit     = "junit" % "junit" % "4.12" % "test"
	var novocode = "com.novocode" % "junit-interface" % "0.11" % "test"
    val mongodb     = "org.mongodb" % "mongo-java-driver" % "3.10.1"
	var retrofit2_converter_gson = "com.squareup.retrofit2" % "converter-gson" % "2.5.0"
	var retrofit2 = "com.squareup.retrofit2" % "retrofit" % "2.5.0"
	var slf4japi = "org.slf4j" % "slf4j-api" % "1.7.25"
	var slf4jimpl = "org.slf4j" % "slf4j-log4j12" % "1.7.25"
	var morphia = "xyz.morphia.morphia" % "core" % "1.4.0"
	var modelmapper = "org.modelmapper" % "modelmapper" % "2.3.0"
  }
  
lazy val protocol = (project in file("./protocol"))

unmanagedJars in Compile += file("jar-lib/play-morphia.jar")

lazy val servicecommon = (project in file("./service-common"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.mongodb,
		dependencies.morphia,
		dependencies.modelmapper,
	  dependencies.slf4japi,	  
	  dependencies.slf4jimpl))
	  
lazy val hubservice = (project in file("./hubservice"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.mongodb,
		dependencies.morphia,
		dependencies.modelmapper,
	  dependencies.slf4japi,	  
	  dependencies.slf4jimpl))
  .dependsOn(servicecommon)
  
 lazy val providerserivce = (project in file("./provider-serivce"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.mongodb,
		dependencies.morphia,
		dependencies.modelmapper,
	  dependencies.slf4japi,	  
	  dependencies.slf4jimpl))
  .dependsOn(servicecommon)

lazy val client = (project in file("./client"))
  .settings(libraryDependencies ++=  Seq(
      dependencies.junit,
      dependencies.novocode,
	  dependencies.retrofit2_converter_gson,	  
	  dependencies.retrofit2,	  
	  dependencies.slf4japi,	  
	  dependencies.slf4jimpl))
  .dependsOn(protocol)
  
  
lazy val service = (project in file("./service"))
	.enablePlugins(PlayJava)
	.settings(libraryDependencies ++=  Seq(
		guice,
		dependencies.mongodb,
		dependencies.morphia,
		dependencies.modelmapper
	))
	.dependsOn(protocol,servicecommon,hubservice)	  
	
lazy val global = project
  .in(file("."))
  .aggregate(
    protocol,
    client,
	hubservice,
	providerserivce,
    service
  )