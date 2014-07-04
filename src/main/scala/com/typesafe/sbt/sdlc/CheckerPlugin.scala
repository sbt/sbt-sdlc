package com.typesafe.sbt.sdlc

import sbt._
import Keys._

object Plugin extends sbt.Plugin {
  val sdlcDocDir = settingKey[File]("The directory containing scaladoc output")
  val sdlcBase = settingKey[String]("The base URI of the scaladoc output")
  val sdlcCheckDir = settingKey[File]("The directory containing HTML files to check")
  val sdlc = taskKey[Unit]("Check scaladoc links in HTML files")

  val sdlcSettings = Seq(
    sdlcDocDir := (target in (Compile, doc)).value,
    sdlc := {
      var ok = true
      val checker = new Checker {
        val scaladocDir = sdlcDocDir.value.getPath
        val scanDir = sdlcCheckDir.value.getPath
        val linkBase = sdlcBase.value
        override def debug(msg: String) = streams.value.log.debug(msg)
        override def info(msg: String) = streams.value.log.info(msg)
        override def error(msg: String) = {
          ok = false
          streams.value.log.error(msg)
        }
      }
      checker.buildModel()
      checker.scanPages()
      if(!ok) sys.error("There were errors during scaladoc link checking")
    }
  )
}
