copy /Y processing-host\conf\application-global.conf processing-host\conf\application.conf
sbt "processing-host/runMain hms.AppStart"