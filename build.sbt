lazy val jsoup = "org.jsoup" % "jsoup" % "1.7.3"
lazy val repoSlug = "sbt/sbt-sdlc"

def scala212 = "2.12.21"
ThisBuild / scalaVersion := scala212

lazy val plugin = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-sdlc",
    crossScalaVersions := Seq(scala212),
    libraryDependencies += jsoup,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.9.0" // set minimum sbt version
      }
    }
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

ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(
    List("+test", "+scripted")
  ),
)
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    commands = List("ci-release"),
    name = Some("Publish project"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)
ThisBuild / githubWorkflowOSes := Seq("ubuntu-latest", "macos-latest", "windows-latest")
ThisBuild / githubWorkflowPublishJavaVersion := JavaSpec.zulu("8")
ThisBuild / githubWorkflowJavaVersions := Seq(
  JavaSpec.temurin("17"),
  JavaSpec.zulu("8"), // only for sbt 1.x
)
ThisBuild / githubWorkflowBuildMatrixExclusions += MatrixExclude(Map("java" -> "zulu@8", "os" -> "macos-latest"))
