lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.11.1",
      version      := "0.1.0-SNAPSHOT",
      javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
      // For project with only Java sources. In order to compile Scala sources, remove the following two lines.
      crossPaths := false
    )),
    name := "client",
    autoScalaLibrary := false,
    libraryDependencies ++= Seq(
      "junit" % "junit" % "4.12" % "test",
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "com.squareup.retrofit2" % "retrofit" % "2.5.0",
	  "org.json4s" %% "json4s-native" % "3.6.4",
	  "org.json4s" %% "json4s" % "3.2.11"
    ) 
)

resolvers += "Local Cached Maven Repository" at Path.userHome.asFile.toURI.toURL + ".ivy2/cache"


