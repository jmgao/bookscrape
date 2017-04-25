package us.insolit.bookscrape

import java.io.FileOutputStream

import nl.siegmann.epublib.domain.{Author => EpubAuthor}
import nl.siegmann.epublib.domain.{Book => EpubBook}
import nl.siegmann.epublib.domain.{Metadata,Resource}
import nl.siegmann.epublib.epub.EpubWriter

case class Author(lastName: String, restOfName: String)
case class Book(title: String, author: Author, chapters: Seq[Chapter]) {
  def toEpub() {
    val book = new EpubBook()
    val metadata = book.getMetadata()
    metadata.addTitle(title)
    metadata.addAuthor(new EpubAuthor(author.restOfName, author.lastName))

    var counter = 1
    for (chapter <- chapters) {
      val resourcePath = "%03d.html".format(counter)
      counter += 1

      val body = chapter.body.map(_.outerHtml).mkString("\n")
      val resourceHtml = "<html><body><h2>%s</h2>%s</body></html>".format(chapter.title, body)
      val resource = new Resource(resourceHtml.getBytes, resourcePath)
      book.addSection(chapter.title, resource)
    }

    val epubWriter = new EpubWriter()
    val path = "%s.epub".format(title)
    println("Writing epub to %s".format(path))
    epubWriter.write(book, new FileOutputStream(path))
  }
}
