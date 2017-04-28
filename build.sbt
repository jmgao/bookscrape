import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "us.insolit",
      scalaVersion := "2.12.2",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "bookscrape",
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0",
    libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "1.2.0"
  )
