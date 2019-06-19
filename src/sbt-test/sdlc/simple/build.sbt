lazy val root = project.in(file("."))
    .enablePlugins(SDLCPlugin)

sdlcBase := "http://example.com/api/"
sdlcCheckDir := file("src/html-" + scalaBinaryVersion.value)

//logLevel in sdlc := Level.Debug

crossScalaVersions := Seq("2.11.12", "2.12.8")

scalaVersion := crossScalaVersions.value.head
