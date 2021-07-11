import sbt._


object Dependencies extends AutoPlugin {

  object autoImport {
    /**
     * ------------------------------
     * Compile/hard dependencies
     * ------------------------------
     */
    val `minimal-json` = "com.eclipsesource.minimal-json" % "minimal-json" % "0.9.5"

    /**
     * ------------------------------
     * Test dependencies
     * ------------------------------
     */
    val `specs2-core` = "org.specs2" %% "specs2-core" % "4.12.3"
  }

}
