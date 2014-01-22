package com.julianrendell.iago.examples

import com.twitter.parrot.processor.RecordProcessor
import com.twitter.ostrich.stats.Stats
import org.jboss.netty.handler.codec.http.HttpResponse
import com.twitter.parrot.server.{ParrotRequest, ParrotService}
import com.twitter.logging.Logger
import com.twitter.parrot.config.ParrotServerConfig

// Also see src/test/scala/com/twitter/parrot/integration/TestRecordProcessor.scala
class LinePrintingExample(parrotService: ParrotService[ParrotRequest, HttpResponse],
                          configuration: ParrotServerConfig[ParrotRequest, HttpResponse]) extends RecordProcessor {
  private[this] val log = Logger.get(getClass)
  private[this] var line_count = 0

  private[this] var properlyShutDown = false

  override def start() {
    println("In LinePrintingExample::start!")

    println("Configuration is:")
    println(configuration)
  }

  def processLines(lines: Seq[String]) {
    lines map {
      line =>
        println("got input of -=\"" +line + "\"=- to process")
        Stats.incr("Lines Processed")
        line_count +=1
    }

    println("Processed -" + lines.length + "- lines of input")
  }

  override def shutdown() {
    properlyShutDown = true
    println("In Shutdown")
    println("Dumping stats")
    println(Stats.get())
    println("Total lines processed, as counted by explicit line_count var: " + line_count)
  }
}
