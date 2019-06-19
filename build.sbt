lazy val plugin = project.in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-sdlc",
    organization := "com.typesafe",
    version := "0.2-SNAPSHOT",
    scalaVersion := "2.12.8",
    libraryDependencies += "org.jsoup" % "jsoup" % "1.7.3",
    sbtPlugin := true,
    publishMavenStyle := false,
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    bintrayReleaseOnPublish := false,
    bintrayRepository := "sbt-plugins",
    bintrayOrganization := None,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false
  )
