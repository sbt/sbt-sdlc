Scaladoc link checker plugin for sbt
====================================

sdlc checks links in HTML files against a scaladoc site. Unlike a regular link checker it understands scaladoc's
fragment syntax. sdlc only needs HTML files as input and validates all links with a specified prefix, so it should
work with any documentation tool that can output HTML (like Ornate or Paradox). 

Add to `project/plugins.sbt`:

```scala
addSbtPlugin("com.github.sbt" % "sbt-sdlc" % "0.3.0")
```

Add add something like this to `build.sbt`:

```scala
lazy val root = project.in(file("."))
  .enablePlugins(SDLCPlugin)

sdlcBase := s"http://slick.typesafe.com/doc/${version.value}/api/"
sdlcCheckDir := (target in com.typesafe.sbt.SbtSite.SiteKeys.makeSite).value
sdlc := (sdlc dependsOn (doc in Compile)).value
```

Type `sdlc` to run in sbt.

The following setting keys are available:

```scala
    val sdlcDocDir = settingKey[File]("The directory containing scaladoc output")
    val sdlcBase = settingKey[String]("The base URI of the scaladoc output")
    val sdlcCheckDir = settingKey[File]("The directory containing HTML files to check")
    val sdlcVersion = settingKey[String]("The scaladoc format version (2.11/2.12/auto)")
```

By default the `sdlc` task does not depend on `doc` so it won't (re)build the scaladocs automatically. You have to add
the dependency yourself (see above) if you want this behavior. `sdlcDocDir` is set to the scaladoc target directory.
`sdlcVersion` is set to `auto` in order to auto-detect scaladoc 2.11 (or lower) and 2.12 (or higher) which use different
link formats. In most cases you only need to set `sdlcBase` and `sdlcCheckDir`.
