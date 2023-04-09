package sbtsdlc

import sbt._
import Keys._

object SDLCPlugin extends AutoPlugin {
  object autoImport {
    val sdlcDocDir = settingKey[File]("The directory containing scaladoc output")
    val sdlcBase = settingKey[String]("The base URI of the scaladoc output")
    val sdlcCheckDir = settingKey[File]("The directory containing HTML files to check")
    val sdlcVersion = settingKey[String]("The scaladoc format version (2.11/2.12/auto)")
    val sdlc = taskKey[Unit]("Check scaladoc links in HTML files")
  }
  import autoImport._

  //override def trigger = allRequirements

  override lazy val projectSettings = Seq(
    sdlcDocDir := (target in (Compile, doc)).value,
    sdlcVersion := "auto",
    sdlc := {
      var ok = true

      val checker = new Checker {
        def nonnull[T <: AnyRef](x: T, s: String): T =
          if (x eq null) throw new MessageOnlyException(s"SettingKey '$s' not set")
          else x
        val scaladocDir = nonnull(sdlcDocDir.value, "sdlcDocDir").getPath
        val scanDir = nonnull(sdlcCheckDir.value, "sdlcCheckDir").getPath
        val linkBase = nonnull(sdlcBase.value, "sdlcBase")
        override def debug(msg: => String) = streams.value.log.debug(msg)
        override def info(msg: => String) = streams.value.log.info(msg)
        override def error(msg: => String) = {
          ok = false
          streams.value.log.error(msg)
        }
      }

      if (!new File(checker.scaladocDir).exists())
        throw new MessageOnlyException("sdlcDocDir '" + checker.scaladocDir + "' does not exist")

      sdlcVersion.value match {
        case "2.11" => checker.is212 = false
        case "2.12" => checker.is212 = true
        case "auto" => checker.detect212()
        case s      => throw new MessageOnlyException(s"Illegal value '$s' for sdlcVersion")
      }

      checker.buildModel()
      checker.scanPages()

      if (!ok) throw new MessageOnlyException("There were errors during scaladoc link checking")
      else checker.info(s"Scaladoc link checking successful.")
    }
  )
}
