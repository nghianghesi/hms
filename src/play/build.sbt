name := "HMS"
organization := "letterbllc"

scalaVersion in ThisBuild := "2.11.12"
//scalaVersion in ThisBuild := "2.12.4"

scalacOptions in ThisBuild := Seq("-Xexperimental", "-Xlint:_", "-unchecked", "-deprecation", "-feature", "-target:jvm-1.8")
// Unfortunately there are lots of annoying warnings from the generated Protobuf code:
// javacOptions in ThisBuild := Seq("-Xlint:all")

resolvers += "Local Cached Maven Repository" at Path.userHome.asFile.toURI.toURL + ".ivy2/cache"
//EclipseKeys.skipParents in ThisBuild := false

lazy val dependencies =
  new {
	val junit     = "junit" % "junit" % "4.12" % "test"
	var novocode = "com.novocode" % "junit-interface" % "0.11" % "test"
    val mongodb     = "org.mongodb" % "mongo-java-driver" % "3.10.1"
	var retrofit2_converter_gson = "com.squareup.retrofit2" % "converter-gson" % "2.5.0"
	var retrofit2 = "com.squareup.retrofit2" % "retrofit" % "2.5.0"
	var retrofit2_converter_scalars = "com.squareup.retrofit2" % "converter-scalars" % "2.5.0"
	var slf4japi = "org.slf4j" % "slf4j-api" % "1.7.25"
	var slf4jimpl = "org.slf4j" % "slf4j-log4j12" % "1.7.25"
	var morphia = "xyz.morphia.morphia" % "core" % "1.4.0"
	var modelmapper = "org.modelmapper" % "modelmapper" % "2.3.0"
	var kafkaclient = "org.apache.kafka" % "kafka-clients" % "2.1.1" 
	var kafka = "org.apache.kafka" %% "kafka" % "2.1.1"
	var kafkastream = "org.apache.kafka" % "kafka-streams" % "2.2.0"
	var dslplatform ="com.dslplatform" % "dsl-json-java8" % "1.8.5"
	var playframwork="com.typesafe.play" %% "play" % "2.7"
	var typesafeconfig="com.typesafe" % "config" % "1.2.1"
	var gson = "com.google.code.gson" % "gson" % "2.8.5"
	var cliparser = "commons-cli" % "commons-cli" % "1.4"
  }
  
lazy val protocol = (project in file("./protocol"))
lazy val playmorphia = (project in file("./playmorphia"))
lazy val jvptree = (project in file("./jvptree"))

lazy val `service-common` = (project in file("./service-common"))
  .dependsOn(protocol)
  
lazy val `hms-play` = (project in file("./hms-play"))	
	.settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.playframwork,
		dependencies.mongodb,
		dependencies.morphia))
	.dependsOn(playmorphia,`service-common`)
  
lazy val hubservice = (project in file("./hubservice"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.mongodb,
		dependencies.morphia,
		dependencies.modelmapper))
  .dependsOn(`service-common`, protocol)
  
  
  
 lazy val `provider-service` = (project in file("./provider-service"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.mongodb,
		dependencies.morphia,
		dependencies.modelmapper))
  .dependsOn(`service-common`, hubservice, protocol)
  
 lazy val `kafka-serivce-common` = (project in file("./kafka-service-common"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.dslplatform,
		dependencies.kafkaclient,
		dependencies.kafkastream,
		dependencies.kafka))
  .dependsOn(`service-common`,protocol)   
  
 lazy val `provider-kafka-producer` = (project in file("./provider-kafka-producer"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.kafkaclient,
		dependencies.kafka))
  .dependsOn(`service-common`,`kafka-serivce-common`)   
  
  lazy val `provider-kafka-consumer` = (project in file("./provider-kafka-consumer"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.kafkaclient,
		dependencies.kafka))
  .dependsOn(`service-common`, `kafka-serivce-common`, `provider-service`, jvptree)  
    
  
  lazy val `hub-kafka-consumer` = (project in file("./hub-kafka-consumer"))
  .settings(libraryDependencies ++=  Seq(	  
		guice,  
		dependencies.kafkaclient,
		dependencies.kafka))
  .dependsOn(`service-common`, `kafka-serivce-common`, hubservice)    
  
  
lazy val client = (project in file("./client"))
  .settings(libraryDependencies ++=  Seq(
      dependencies.novocode,
	  dependencies.retrofit2_converter_gson,	
		dependencies.retrofit2_converter_scalars,	  
	  dependencies.retrofit2,	  
	  dependencies.slf4japi,	  
	  dependencies.slf4jimpl,
	  dependencies.typesafeconfig,
	  dependencies.gson))
  .dependsOn(protocol)
  
  
lazy val query = (project in file("./query"))
  .settings(libraryDependencies ++=  Seq(
      dependencies.junit,
      dependencies.novocode,
	  dependencies.retrofit2_converter_gson,
		dependencies.retrofit2_converter_scalars,	  	  
	  dependencies.retrofit2,	  
	  dependencies.slf4japi,	  
	  dependencies.slf4jimpl,
	  dependencies.typesafeconfig,
	  dependencies.gson))
  .dependsOn(protocol)
  
lazy val `test-mongo` = (project in file("./test-mongo"))
  .settings(libraryDependencies ++=  Seq(
      dependencies.junit,
      dependencies.novocode,
	  dependencies.retrofit2_converter_gson,	  
	  dependencies.retrofit2,	  
	  dependencies.slf4japi,	  
	  dependencies.slf4jimpl,
		dependencies.morphia))
  .dependsOn(protocol)
  
lazy val `service-gateway` = (project in file("./service-gateway"))
	.enablePlugins(PlayJava)
	.settings(		
		libraryDependencies ++=  Seq(
		guice,
		dependencies.mongodb,
		dependencies.morphia,
		dependencies.modelmapper,
		cliparser
	))
	.dependsOn(protocol, `service-common`,`kafka-serivce-common`,hubservice,`provider-service`, `provider-kafka-producer`, `hms-play`,`provider-kafka-producer`)	  
  
lazy val `processing-host` = (project in file("./processing-host"))
	.enablePlugins(PlayJava)
	.settings(	
		PlayKeys.playDefaultPort := 9001,
		libraryDependencies ++=  Seq(
		guice,
		dependencies.mongodb,
		dependencies.morphia,
		dependencies.modelmapper,
		cliparser
	))
	.dependsOn(`provider-kafka-consumer`,`hub-kafka-consumer`, `hms-play`,`service-common`)	  
	
lazy val global = project
  .in(file("."))
  .aggregate(
    protocol,
    client,
	query,
	hubservice,
	`provider-service`,
	`provider-kafka-producer`,
	`provider-kafka-consumer`,
	`hub-kafka-consumer`,	
    `service-gateway`,
	`processing-host`
  )
  
  
  