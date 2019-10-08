FROM dockette/jdk8 AS base
WORKDIR /hms
COPY ["src", "src/"]
COPY ["sbt", "sbt/"]
ENV PATH="/hms/sbt/bin:${PATH}"