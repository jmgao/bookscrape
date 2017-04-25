package us.insolit.bookscrape

import net.ruippeixotog.scalascraper.browser.JsoupBrowser._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

import org.jsoup.nodes.{Element}

class Chapter(var title: String, val body: Seq[Element]) {
  override def toString(): String = {
    title + "\n" + body.mkString("\n")
  }
}

object Chapter {
  def removeNestedItalics(line: JsoupElement) {
    // Remove nested <i>.
    for (outer <- line >> elements("i");
         children <- outer.children;
         inner <- children >> elements("i")) {
      val elem = inner.asInstanceOf[JsoupElement].underlying
      elem.unwrap()
    }
  }

  def joinAdjacentItalics(line: JsoupElement) {
    // Join adjacent <i>s.
    for (elem <- line >> elements("i")) {
      val node = elem.asInstanceOf[JsoupElement].underlying
      val next = node.nextSibling()
      if (next != null) {
        if (next.isInstanceOf[Element]) {
          val nextElem = next.asInstanceOf[Element]
          if (nextElem.tagName.equalsIgnoreCase("i")) {
            nextElem.prepend(node.html)
            node.remove()
          }
        }
      }
    }
  }

  def cleanup(line: JsoupElement) = {
    // Do some cleanups to make the raw HTML output look nicer.
    removeNestedItalics(line)
    joinAdjacentItalics(line)
    line.underlying
  }

  // TODO: This probably take a JsoupElement instead of a Seq[JsoupElement].
  def apply(title: String, body: Seq[JsoupElement]) = {
    new Chapter(title, body.map(cleanup))
  }
}
