Scaladoc link checker plugin for sbt
====================================

Add to `project/build.sbt`:

    addSbtPlugin("com.typesafe.sbt" % "sbt-sdlc" % "0.1-SNAPSHOT")

Add something like this to your project settings:

    import com.typesafe.sbt.sdlc.Plugin._

    sdlcSettings ++ Seq(
      sdlcBase := s"http://slick.typesafe.com/doc/${version.value}/api/",
      sdlcCheckDir := (target in com.typesafe.sbt.SbtSite.SiteKeys.makeSite).value,
      sdlc <<= sdlc dependsOn (doc in Compile, com.typesafe.sbt.SbtSite.SiteKeys.makeSite)
    )

Type `sdlc` to run in sbt.
