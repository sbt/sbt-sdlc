lazy val plugin = project.in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-sdlc",
    organization := "com.typesafe",
    version := "0.2-SNAPSHOT",
    crossScalaVersions := Seq("2.12.17", "2.10.7"),
    libraryDependencies += "org.jsoup" % "jsoup" % "1.7.3",
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.10" => "0.13.18"
        case "2.12" => "1.2.8" // set minimum sbt version
      }
    },
  )
