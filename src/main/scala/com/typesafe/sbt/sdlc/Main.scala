package com.typesafe.sbt.sdlc

import java.io.File
import org.jsoup.Jsoup
import scala.collection.JavaConverters._

object Main extends App with Checker {

  val scaladocDir = args(0)
  val scanDir = args(1)
  val linkBase = args(2)

  buildModel()
  scanPages()
}
