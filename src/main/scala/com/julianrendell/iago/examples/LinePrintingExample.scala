package com.twitter.iago.examples

import com.twitter.parrot.processor.RecordProcessor
import com.twitter.ostrich.stats.Stats
import org.jboss.netty.handler.codec.http.HttpResponse
import com.twitter.parrot.server.{ParrotRequest, ParrotService}
import com.twitter.logging.Logger
import com.twitter.parrot.config.ParrotServerConfig
import com.twitter.parrot.util.UriParser
import com.twitter.util.Return
import com.twitter.util.Throw


// Also see src/test/scala/com/twitter/parrot/integration/TestRecordProcessor.scala
class LinePrintingExample(parrotService: ParrotService[ParrotRequest, HttpResponse],
                          config: ParrotServerConfig[ParrotRequest, HttpResponse]) extends RecordProcessor {
  val log = Logger.get(getClass)
  val s2 = parrotService
  val c2 = config

  var properlyShutDown = false

  override def start() {
    println("In start!")

    println("\tConfiguration is:")
    println(c2)
  }


  def processLines(lines: Seq[String]) {
    lines map {
      line =>
        println("got input of -=\"" +line + "\"=- to process")
    }

    println("Processed -" + lines.length + "- lines of input")
    Stats.incr("Lines Processed")
  }

  override def shutdown() {
    properlyShutDown = true
    println("In Shutdown")
    println("Dumping stats")
    println(Stats.get())
  }
}
