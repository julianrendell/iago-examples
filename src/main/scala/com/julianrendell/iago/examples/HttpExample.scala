package com.julianrendell.iago.examples

import com.twitter.parrot.processor.RecordProcessor
import com.twitter.ostrich.stats.Stats
import org.jboss.netty.handler.codec.http.HttpResponse
import com.twitter.parrot.server.{ParrotRequest, ParrotService}
import com.twitter.logging.Logger
import com.twitter.parrot.config.ParrotServerConfig
import com.twitter.parrot.util.Uri

// Also see src/test/scala/com/twitter/parrot/integration/TestRecordProcessor.scala
class HttpExample(parrotService: ParrotService[ParrotRequest, HttpResponse],
                          configuration: ParrotServerConfig[ParrotRequest, HttpResponse]) extends RecordProcessor {
  private[this] val log = Logger.get(getClass)
  private[this] var properlyShutDown = false
  private[this] val hostHeader = Some((configuration.httpHostHeader, configuration.httpHostHeaderPort))

  // regex's for pulling apart the incoming lines of text.
  // The example data is intended to represent three types of endpoint:
  // - # a comment
  // - api, (oauth) token, data
  // - api, token or data
  // - api
  private[this] val commentParser = """^(#.*)""".r
  private[this] val threeArgParser = """^\s*(.*?)\s*,\s*(.*?)\s*,\s*(.*?)\s*$""".r
  private[this] val twoArgParser = """^\s*(.*?)\s*,\s*(.*?)\s*$""".r
  private[this] val customHeaderParser = """"Custom-Header": "(.*)",""".r

  override def start() {
    log.debug("In start!")

    // do any test setup
    // eg get OAUTH tokens to be used, etc, etc
  }

  // based on https://github.com/twitter/iago/blob/master/src/main/scala/com/twitter/parrot/processor/SimpleRecordProcessor.scala
  def processLines(lines: Seq[String]) {
    log.trace("HttpExample.processLines: processing %d lines", lines.size)
    lines map {
      line =>  line match {
        // Shouldn't affect RPS (as that's handled by the Parrot request handler), but are a waste of processing. Use in moderation?
        case commentParser(comment) => {
          log.debug("Got a comment line- %s", comment)
          Stats.incr("Input/Comments/Count")
        }

        case threeArgParser(api, token, data) => {
          log.debug("Got three arg line- api: %s, token: %s, data: %s", api, token, data)
          Stats.incr("Input/Not_Implemented/Count")
        }

        case twoArgParser(api, data) => {
          log.debug("Got two arg line- api: %s, data: %s", api, data)

          api match {
            case "headers" => {
              log.debug("header request")
              Stats.incr("Input/header/count")
              val customheadercontent = data
              log.debug("setting custom header to: " + customheadercontent)
              val rawheaders = Seq(("custom_header",  customheadercontent))

              val uri = Uri("headers", Seq())

              parrotService(new ParrotRequest(hostHeader, rawheaders, uri, line)) respond { response =>
                log.debug("headers response was %s", response)
                val httpresponse = response.get()
                val httpresponsecode: Int = httpresponse.getStatus().getCode()
                val httpresponsephrase: String = httpresponse.getStatus().getReasonPhrase()
                log.debug("header response code was: " + httpresponsecode.toString)
                log.debug("header response phrase was: " + httpresponsephrase)
                Stats.incr("Input/headers/Response_Codes/" + httpresponsecode.toString)

                // this code would work for looking at the headers of an http response...
                // but httpbin returns the passed headers as part of a JSON struct in the body
                // log.debug("Response headers: " + httpresponseheaders.toString)
                // val httpresponseheaders = httpresponse.getHeaders()
                // if ( httpresponseheaders.contains(("custom_header", customheadercontent)) ) {
                //   Stats.incr("Input/headers/custom_header_found")
                // } else {
                //   Stats.incr("Input/headers/custom_header_missing")
                // }

                // Originally looked for a JSON parser for Scala, but couldn't find a simple
                // one that presents JSON as an eg set of Maps (that is not abandonware.)
                // There are a lot of great looking libraries that do "proper" object (de)serialization-
                // but that's overkill for a simple verification- especially that's being done
                // in a load test.
                // So using a regex to see if the custom-header is in the response body- but this
                // doesn't guarantee the response is valid JSON.

                // get the response content: is there a more direct way of doing this?
                val content = new StringBuilder()
                val buffer = httpresponse.getContent()
                while(buffer.readable()) {
                  content.append( buffer.readByte.asInstanceOf[Char] )
                }

                log.debug("header response content was: " + content)
                if ( customHeaderParser.findFirstMatchIn(content) != None) {
                  Stats.incr("Input/headers/custom_header_found")
                } else {
                  Stats.incr("Input/headers/custom_header_missing")
                }
              }
            }

            case "status" => {
              log.debug("status request")
              Stats.incr("Input/status/count")
              val statusCode = data.toInt
              log.debug("status code requested: " + statusCode.toString)
              val uri = Uri("status/" + statusCode.toString, Seq())

              parrotService(new ParrotRequest(hostHeader, Nil, uri, line)) respond { response =>
                log.debug("status response was %s", response)
                val httpresponse = response.get()
                val httpresponsecode: Int = httpresponse.getStatus().getCode()
                val httpresponsephrase: String = httpresponse.getStatus().getReasonPhrase()
                log.debug("status response code was: " + httpresponsecode.toString + "expected was: " + statusCode.toString)
                log.debug("status response phrase was: " + httpresponsephrase)
                Stats.incr("Input/status/Response_Codes/" + httpresponsecode.toString)

                if (httpresponsecode == statusCode ) {
                  Stats.incr("Input/status/matching_status")
                } else {
                  Stats.incr("Input/status/mismatched_status")
                }
              }
            }
            case _ => {
              Stats.incr("Input/Not_Implemented/Count")
            }
          }
        }

        case "ip" => {
          log.debug("ip request")
          Stats.incr("Input/ip/count")
          val uri = Uri("ip", Seq())

          parrotService(new ParrotRequest(hostHeader, Nil, uri, line)) respond { response =>
            log.debug("ip response was %s", response)
            val httpresponse = response.get()
            val httpresponsecode: Int = httpresponse.getStatus().getCode()
            val httpresponsephrase: String = httpresponse.getStatus().getReasonPhrase()
            log.debug("ip response code was: " + httpresponsecode.toString)
            log.debug("ip response phrase was: " + httpresponsephrase)
            Stats.incr("Input/ip/Response_Codes/" + httpresponsecode.toString)
          }
        }

        case "get" => {
          log.debug("get request")
          Stats.incr("Input/get/count")
          val uri = Uri("get", Seq())

          parrotService(new ParrotRequest(hostHeader, Nil, uri, line)) respond { response =>
            log.debug("get response was %s", response)
            val httpresponse = response.get()
            val httpresponsecode: Int = httpresponse.getStatus().getCode()
            val httpresponsephrase: String = httpresponse.getStatus().getReasonPhrase()
            log.debug("get response code was: " + httpresponsecode.toString)
            log.debug("get response phrase was: " + httpresponsephrase)
            Stats.incr("Input/get/Response_Codes/" + httpresponsecode.toString)
          }
        }

        case _ => {
          log.debug("Unknown Request- %s", line)
          Stats.incr("Input/Bad_Input")
        }
      }

      Stats.incr("Input/Lines_Processed")
    }
  }

  override def shutdown() {
    properlyShutDown = true
    println("In Shutdown")
    println("Dumping stats")
    println(Stats.get())
  }
}
