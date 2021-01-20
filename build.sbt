


name := "jmx-runtime-json"
version := "1.0.0"
organization  := "org.dmonix"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalaVersion  := "2.13.3" //for unit testing

//disable scala version and including scala as library in dependencies
crossPaths := false
autoScalaLibrary := false

libraryDependencies ++= Seq(
    `minimal-json`,
    `specs2-core`          % "test"
  )

jacocoReportSettings := JacocoReportSettings()
  .withThresholds(
    JacocoThresholds(
      instruction = 100,
      method = 100,
      branch = 100,
      complexity = 100,
      line = 100,
      clazz = 100)
  )

