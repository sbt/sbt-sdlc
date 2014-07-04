import bintray.Keys._

organization := "com.typesafe"

name := "sbt-sdlc"

version := "0.1"

scalaVersion := "2.10.3"

libraryDependencies += "org.jsoup" % "jsoup" % "1.7.3"

sbtPlugin := true

publishMavenStyle := false

bintrayPublishSettings

repository in bintray := "sbt-plugins"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

bintrayOrganization in bintray := None
