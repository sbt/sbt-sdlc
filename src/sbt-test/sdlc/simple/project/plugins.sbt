addSbtPlugin("com.github.sbt" % "sbt-sdlc" % Option(System.getProperty("plugin.version")).getOrElse(
  throw new RuntimeException("System property 'plugin.version' must be set to sbt-sdlc's version")
))
