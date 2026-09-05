package sbtsdlc

object Main extends App with Checker {

  val scaladocDir = args(0)
  val scanDir = args(1)
  val linkBase = args(2)

  buildModel()
  scanPages()
}
