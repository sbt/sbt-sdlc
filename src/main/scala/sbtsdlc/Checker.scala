package sbtsdlc

import java.io.File
import org.jsoup.Jsoup
import scala.collection.JavaConverters._

trait Checker {

  def scaladocDir: String
  def scanDir: String
  def linkBase: String

  def debug(msg: => String): Unit = println(msg)
  def info(msg: => String): Unit = println(msg)
  def error(msg: => String): Unit = println(msg)

  var pageMap = Map.empty[String, Page]
  var is212: Boolean = true

  def buildModel() {
    debug(s"Building scaladoc index from $scaladocDir ...")
    val scaladocPages: Vector[Page] = Option(new File(scaladocDir).listFiles).getOrElse(Array.empty).toVector.flatMap {
      f =>
        if (f.getName == "index" || f.getName == "lib" || f.getName == "index.html") Vector.empty
        else scanScaladoc("", f)
    }
    pageMap = scaladocPages.map(p => (p.path, p)).toMap
    debug("Found " + scaladocPages.length + " pages with " + scaladocPages.map(_.ids.size).sum + " fragments:")
    pageMap.foreach {
      case (s, p) =>
        debug(s"- $s")
        debug(s"  (ids: ${p.ids.toSeq.sorted.mkString(", ")})")
        debug(s"  (simplified ids: ${p.simplifiedIds.map(_._2).toSeq.sorted.mkString(", ")})")
    }
  }

  def detect212(): Unit = {
    val marker211 = new File(new File(new File(scaladocDir), "lib"), "permalink.png")
    is212 = !marker211.exists()
    if (is212) debug(s"Marker file $marker211 for 2.11- not found, assuming 2.12+")
    else debug(s"Marker file $marker211 for 2.11- found, assuming 2.11-")
  }

  def scanPages() {
    info(s"Scanning HTML pages in $scanDir against $linkBase ...")
    val errors = scanLinks(new File(scanDir))
    errors.groupBy(_.source.getPath).toVector.sortBy(_._1).foreach {
      case (f, es) =>
        error(s"$f:")
        es.foreach {
          case e @ FragmentError(s, l, p, f) =>
            error("    " + e.message)
            val simplifiedF = Dist.simplifySig(f)
            val dists = pageMap(p).ids.toVector.map(id => (id, Dist.levenshtein(f, id)))
            val sdists = pageMap(p).simplifiedIds.map { case (sid, id) => (id, Dist.levenshtein(simplifiedF, sid)) }
            val limit = f.length / 2
            (dists ++ sdists).filter(_._2 <= limit).toMap.toVector.sortBy(_._2).take(3).foreach {
              case (id, dist) =>
                info(s"      Did you mean: $id")
                debug(s"        (distance: $dist)")
            }
          case e =>
            error("  " + e.message)
        }
    }
    debug("Finished")
  }

  def scanScaladoc(base: String, f: File): Vector[Page] = {
    val path = base + f.getName
    if (f.isDirectory) f.listFiles().toVector.flatMap(ch => scanScaladoc(path + "/", ch))
    else if (f.getName.endsWith(".html")) Vector(new Page(f, path))
    else Vector.empty
  }

  def scanLinks(f: File): Vector[DocError] = {
    if (f.isDirectory) f.listFiles.toVector.flatMap(scanLinks)
    else if (f.getName.endsWith(".html")) {
      debug(s"Scanning $f ...")
      val doc = Jsoup.parse(f, "UTF-8")
      val allLinks = doc.getElementsByAttribute("href").asScala.map(_.attr("href"))
      val refs = allLinks.filter(_.startsWith(linkBase)).map(_.substring(linkBase.length))
      debug(s"- found ${refs.size} scaladoc links, ${allLinks.size - refs.size} other links")
      refs.toVector.flatMap { uri =>
        if (is212) checkLink212(f, uri).toVector
        else {
          if (uri.startsWith("#")) checkLink211(f, uri.substring(1)).toVector
          else if (uri.startsWith("index.html#")) checkLink211(f, uri.substring(11)).toVector
          else if (uri.nonEmpty && uri != "index.html") Vector(MainFileError(f, uri))
          else Vector.empty
        }
      }
    } else Vector.empty
  }

  def checkLink211(source: File, link: String): Option[DocError] = {
    debug(s"  Checking link: $link")
    var page = link.split("@")(0).replace(".", "/")
    if (page.indexOf(".html") < 0) page += ".html"
    pageMap.get(page) match {
      case Some(p) =>
        if (link.indexOf('@') > 0) {
          val fragment = link.split("@", 2)(1)
          if (p.ids.contains(fragment)) None
          else Some(FragmentError(source, link, page, fragment))
        } else None
      case None => Some(PageError(source, link, page))
    }
  }

  def checkLink212(source: File, link: String): Option[DocError] = {
    debug(s"  Checking link: $link")
    def withIndex(s: String) =
      if ((s == "") || s.endsWith("/")) s + "index.html" else s
    val sep = link.indexOf('#')
    val (page, anchor) =
      if (sep == -1) (withIndex(link), None)
      else (withIndex(link.substring(0, sep)), Some(link.substring(sep + 1)))
    pageMap.get(page) match {
      case Some(p) =>
        anchor match {
          case Some(fragment) =>
            if (p.ids.contains(fragment)) None
            else Some(FragmentError(source, link, page, fragment))
          case None => None
        }
      case None => Some(PageError(source, link, page))
    }
  }
}

class Page(val file: File, val path: String) {
  val ids: Set[String] = {
    val doc = Jsoup.parse(file, "UTF-8")
    doc.getElementsByAttribute("id").asScala.filter(_.nodeName().toLowerCase == "a").map(_.attr("id")).toSet
  }
  lazy val simplifiedIds: Vector[(String, String)] =
    ids.toVector.map(id => (Dist.simplifySig(id), id))

  override def toString = s"Page $path"
}

sealed trait DocError {
  def source: File
  def link: String
  def message: String
}
case class MainFileError(source: File, link: String) extends DocError {
  def message = s"Invalid URI [...]$link"
}
case class PageError(source: File, link: String, page: String) extends DocError {
  def message = s"Invalid page '$page' in URI [...]$link"
}
case class FragmentError(source: File, link: String, page: String, fragment: String) extends DocError {
  def message = s"Invalid fragment '$fragment' in URI [...]$link"
}

object Dist {
  def simplifySig(s: String): String = {
    val b = new StringBuilder(s.length)
    var inType = false
    s.foreach { c =>
      if (inType) c match {
        case ')' | ' ' | ']' | ',' =>
          b append c
          inType = false
        case _ =>
        // omit
      } else
        c match {
          case ':' =>
            inType = true
          case _ =>
            b append c
        }
    }
    b.toString
  }

  // Source: http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Scala
  def levenshtein(str1: String, str2: String): Int = {
    def min(nums: Int*): Int = nums.min
    val lenStr1 = str1.length
    val lenStr2 = str2.length

    val d: Array[Array[Int]] = Array.ofDim(lenStr1 + 1, lenStr2 + 1)

    for (i <- 0 to lenStr1) d(i)(0) = i
    for (j <- 0 to lenStr2) d(0)(j) = j

    for (i <- 1 to lenStr1; j <- 1 to lenStr2) {
      val cost = if (str1(i - 1) == str2(j - 1)) 0 else 1

      d(i)(j) = min(
        d(i - 1)(j) + 1, // deletion
        d(i)(j - 1) + 1, // insertion
        d(i - 1)(j - 1) + cost // substitution
      )
    }

    d(lenStr1)(lenStr2)
  }
}
