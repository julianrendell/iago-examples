import com.twitter.parrot.config.ParrotLauncherConfig
import com.twitter.logging.LoggerFactory
import com.twitter.logging.config._

// See https://github.com/twitter/iago/blob/master/README.md#Configuring%20Your%20Test for the details
// of all the ParrotLauncherConfig options

new ParrotLauncherConfig {

  // useful piece of code from Rosetta code for dumping a stack trace.
  //customLogSource = """try { error("exception") } catch { case ex => println(ex.printStackTrace()) }"""

  jobName = "simplehttprequests"
  port = 5000
  victims = "localhost"

  // HTTP host header
  //header = "api.iago-examples.com"

  // setting some lower than default values to try and stop server under-test failures from causing Iago to hang.
  // Didn't work, but also suspect lower values may be useful.
  hostConnectionMaxIdleTimeInMs = 60000
  hostConnectionMaxLifeTimeInMs = 60000
  requestTimeoutInMs = 3000

  traceLevel = com.twitter.logging.Level.ALL
  verboseCmd = true

  // This does not seem to work! I never get console output...
  loggers = new LoggerFactory(
    level = Level.ALL,
    handlers = new ConsoleHandlerConfig()
  )

  log = "../../simplelistofendpoints.log"
  requestRate = 5
  duration = 3
  timeUnit = "MINUTES"
  // maxRequests defaults to 1000!
  //maxRequests = 1000
  reuseFile = true
  localMode = true

  // Custom Configuration...
  // doesn't seem to be supported, other than by using dynamically compiled code in the loadTest string.

  imports = """
  import com.julianrendell.iago.examples.HttpExample
  import org.jboss.netty.handler.codec.http.HttpResponse
  import com.twitter.conversions.time._
  import com.twitter.parrot.util.SlowStartPoissonProcess
  """

  // example for setting a custom load distribution
  createDistribution = """createDistribution = {
  rate => new SlowStartPoissonProcess(rate, 1.minutes)
  }"""

  responseType = "HttpResponse"
  transport = "FinagleTransport"
  loadTest = "new HttpExample(service.get, this)"
}
