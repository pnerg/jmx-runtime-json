import sbt.Keys.{javacOptions, scalaVersion}

val componentVersion = "1.1.0"

publishArtifact := false
version := componentVersion

//shared settings for all modules
val baseSettings = Seq(
  organization  := "org.dmonix",
  version := componentVersion,
  crossPaths := false,
  autoScalaLibrary := false
)

lazy val lib = (project in file("lib"))
  .settings(baseSettings)
  .settings(
    //disable scala version and including scala as library in dependencies
    scalaVersion  := "2.13.3",
    name := "jmx-runtime-json",
    libraryDependencies ++= Seq(
      `minimal-json`,
      `specs2-core`          % "test"
    ),
    javacOptions in (Compile, doc) := Seq("-source", "1.8"),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    javacOptions in (Compile, doc) := Seq("-source", "1.8"),
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
      .withFormats(JacocoReportFormats.HTML, JacocoReportFormats.XML)
  )

val jopts = Seq(
  "-Xss256k",
  "-XX:MaxMetaspaceSize=128m",
  "-XX:+CrashOnOutOfMemoryError",
  "-XX:+UseContainerSupport",
  "-XX:MaxRAMPercentage=75.0"
)

lazy val docker = (project in file("docker"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(baseSettings)
  .settings(
    name := "jmx-runtime-json-test-app",
    publishArtifact := false,
    publishArtifact in (Compile, packageBin) := false,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false,
    mainClass in (Compile, run) := Some("org.dmonix.jmx.Main"),
    Universal / javaOptions ++= jopts.map("-J"+_),
    run / javaOptions ++= jopts,
    dockerBaseImage := "adoptopenjdk/openjdk11",
    packageName in Docker := "jmx-runtime-json-test-app"
  ).dependsOn(lib)
