name := "HMS"
organization := "letterbllc"

scalaVersion in ThisBuild := "2.11.12"
//scalaVersion in ThisBuild := "2.12.4"

scalacOptions in ThisBuild := Seq("-Xexperimental", "-Xlint:_", "-unchecked", "-deprecation", "-feature", "-target:jvm-1.8")
// Unfortunately there are lots of annoying warnings from the generated Protobuf code:
// javacOptions in ThisBuild := Seq("-Xlint:all")

resolvers += "Local Cached Maven Repository" at Path.userHome.asFile.toURI.toURL + ".ivy2/cache"
EclipseKeys.skipParents in ThisBuild := false

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
	var kafkaclient = "org.apache.kafka" % "kafka-clients" % "2.1.1" 
	var kafka = "org.apache.kafka" %% "kafka" % "2.1.1"
	var dslplatform ="com.dslplatform" % "dsl-json-java8" % "1.8.5"
  }
  
lazy val protocol = (project in file("./protocol"))

lazy val servicecommon = (project in file("./service-common"))
  .dependsOn(protocol)
	  
lazy val hubservice = (project in file("./hubservice"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.mongodb,
		dependencies.morphia,
		dependencies.modelmapper))
  .dependsOn(servicecommon, protocol)
  
  
  
 lazy val providerserivce = (project in file("./provider-serivce"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.mongodb,
		dependencies.morphia,
		dependencies.modelmapper))
  .dependsOn(servicecommon, hubservice, protocol)
  
  
 lazy val kafkaserivcecommon = (project in file("./kafka-service-common"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.dslplatform,
		dependencies.kafkaclient,
		dependencies.kafka))
  .dependsOn(servicecommon)   
  
 lazy val providerkafkaproducer = (project in file("./provider-kafka-producer"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.kafkaclient,
		dependencies.kafka))
  .dependsOn(servicecommon,kafkaserivcecommon)   
  
  lazy val providerkafkacomsumer = (project in file("./provider-kafka-comsumer"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.kafkaclient,
		dependencies.kafka))
  .dependsOn(servicecommon, kafkaserivcecommon, providerserivce)  
  
 lazy val hubkafkaproducer = (project in file("./hub-kafka-producer"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.kafkaclient,
		dependencies.kafka))
  .dependsOn(servicecommon, kafkaserivcecommon)   
  
  lazy val hubkafkacomsumer = (project in file("./hub-kafka-comsumer"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.kafkaclient,
		dependencies.kafka))
  .dependsOn(servicecommon, kafkaserivcecommon, hubservice)    

lazy val client = (project in file("./client"))
  .settings(libraryDependencies ++=  Seq(
      dependencies.junit,
      dependencies.novocode,
	  dependencies.retrofit2_converter_gson,	  
	  dependencies.retrofit2,	  
	  dependencies.slf4japi,	  
	  dependencies.slf4jimpl))
  .dependsOn(protocol)
  
  
lazy val servicegateway = (project in file("./service"))
	.enablePlugins(PlayJava)
	.settings(libraryDependencies ++=  Seq(
		guice,
		dependencies.mongodb,
		dependencies.morphia,
		dependencies.modelmapper
	))
	.dependsOn(protocol,servicecommon,kafkaserivcecommon,hubservice,providerserivce,providerkafkaproducer)	  
	
lazy val global = project
  .in(file("."))
  .aggregate(
    protocol,
    client,
	hubservice,
	providerserivce,
	providerkafkaproducer,
	providerkafkacomsumer,
	hubkafkaproducer,
	hubkafkacomsumer,	
    servicegateway
  )