FROM dockette/jdk8 AS base
WORKDIR /hms
COPY ["src", "src/"]
ENV PATH="/hms/sbt/bin:${PATH}"