package us.insolit.bookscrape.apgte

import java.net.URI

import org.jsoup.nodes.{Document, Element}

import net.ruippeixotog.scalascraper
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

import scalaj.http._

import us.insolit.bookscrape._

object APGTEChapter {
  def parse(doc: JsoupDocument): Chapter = {
    val article = doc >> element(".entry-wrapper")
    val title = article >> text(".entry-title")

    // TODO: Make sure there aren't any tags in between <p> tags.
    val body = article >> element(".entry-content") >> elements("p")
    Chapter(title, body.toList.asInstanceOf[List[JsoupElement]])
  }

  def fromUrl(url: String): Chapter = {
    parse(JsoupBrowser().get(url).asInstanceOf[JsoupDocument])
  }
}

case class ChapterUrl(chapterName: String, chapterUrl: String)
case class BookUrls(bookName: String, chapters: Seq[ChapterUrl])

object APGTETOC {
  def parse(doc: JsoupDocument): Seq[BookUrls] = {
    var result = Vector[BookUrls]()

    val books = doc >> elements(".entry-content > ul")
    for (book <- books) {
      val bookElem = book.asInstanceOf[JsoupElement].underlying
      val bookNameNode = bookElem.previousElementSibling

      assert(bookNameNode.tagName == "h2")
      val bookName = bookNameNode.text.trim

      var chapterList = Vector[ChapterUrl]()
      val chapters = book >> elements("a")
      for (chapter <- chapters) {
        val chapterElem = chapter.asInstanceOf[JsoupElement].underlying
        val chapterName = chapterElem.text.trim
        val chapterUrl = chapterElem.absUrl("href")
        chapterList = chapterList :+ ChapterUrl(chapterElem.text, chapterUrl)
      }

      result = result :+ BookUrls(bookName, chapterList)
    }

    result
  }

  def fetch(): Seq[BookUrls] = {
    val url = "https://practicalguidetoevil.wordpress.com/table-of-contents/"
    parse(JsoupBrowser().get(url).asInstanceOf[JsoupDocument])
  }
}

object APGTEScraper extends App {
  val bookTitleMap = Map(
    "Book I" -> "A Practical Guide to Evil: Book 1 - So You Want to Be a Villain?",
    "Book 1" -> "A Practical Guide to Evil: Book 1 - So You Want to Be a Villain?", // just in case
    "Book 2" -> "A Practical Guide to Evil: Book 2 - The Revolution Will Not Be Civilized",
    "Book 3" -> "A Practical Guide to Evil: Book 3"
  )

  val bookUrls = APGTETOC.fetch()
  for (BookUrls(bookName, chapterUrls) <- bookUrls) {
    val name = bookTitleMap.getOrElse(bookName, bookName)
    var chapters = Vector[Chapter]()
    for (ChapterUrl(chapterName, chapterUrl) <- chapterUrls) {
      // TODO: Switch to futures, parallelize.
      val chapter = APGTEChapter.fromUrl(chapterUrl)
      if (chapterName != chapter.title) {
        println("Different chapter names: '%s' vs '%s'".format(chapterName, chapter.title))
        chapter.title = chapterName
      }
      chapters = chapters :+ chapter
    }

    Book(name, Author("Verburg", "David"), chapters).toEpub
  }
}
