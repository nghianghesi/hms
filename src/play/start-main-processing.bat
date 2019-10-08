copy /Y processing-host\conf\application-main.conf processing-host\conf\application.conf
sbt "processing-host/runMain hms.AppStart"