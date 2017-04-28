package us.insolit.bookscrape

import net.ruippeixotog.scalascraper.browser.JsoupBrowser._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

import org.jsoup.nodes.{Element, Node, TextNode}

class Chapter(var title: String, val body: Seq[Node]) {
  override def toString(): String = {
    title + "\n" + body.mkString("\n")
  }
}

object Chapter {
  def apply(title: String, body: Seq[Node]) = {
    new Chapter(title, body)
  }
}
