copy /Y processing-host\conf\application-los.conf processing-host\conf\application.conf
sbt "processing-host/runMain hms.AppStart"