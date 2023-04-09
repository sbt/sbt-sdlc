lazy val jsoup = "org.jsoup" % "jsoup" % "1.7.3"
lazy val repoSlug = "sbt/sbt-sdlc"

lazy val plugin = project.in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-sdlc",
    crossScalaVersions := Seq("2.12.17", "2.10.7"),
    libraryDependencies += jsoup,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.10" => "0.13.18"
        case "2.12" => "1.2.8" // set minimum sbt version
      }
    },
  )

ThisBuild / organization := "com.github.sbt"
ThisBuild / dynverSonatypeSnapshots := true
ThisBuild / version := {
  val orig = (ThisBuild / version).value
  if (orig.endsWith("-SNAPSHOT")) "0.2-SNAPSHOT"
  else orig
}
ThisBuild / licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url(s"https://github.com/$repoSlug"),
    s"scm:git@github.com:sbt/$repoSlug.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "szeiger",
    name = "Stefan Zeiger",
    email = "@szeiger",
    url = url("http://szeiger.de/")
  )
)
ThisBuild / description := "An sbt plugin to check Scaladoc links"
ThisBuild / homepage := Some(url(s"https://github.com/$repoSlug"))
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishMavenStyle := true
