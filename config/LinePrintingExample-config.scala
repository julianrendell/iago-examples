import com.twitter.parrot.config.ParrotLauncherConfig

// See https://github.com/twitter/iago/blob/master/README.md#Configuring%20Your%20Test for the details
// of all the ParrotLauncherConfig options

new ParrotLauncherConfig {

  // useful piece of code from Rosetta code for dumping a stack trace.
  //customLogSource = """try { error("exception") } catch { case ex => println(ex.printStackTrace()) }"""



  jobName = "simpletextlines"
  port = 8081
  victims = "www.google.com"

  log = "../../simpletextlines.log"
  requestRate = 10
  duration = 10
  timeUnit = "SECONDS"
  reuseFile = true
  localMode = true

  // Custom Configuration...
  // doesn't seem to be supported, other than by using dynamically compiled code in the loadTest string.

  imports = """
  import com.twitter.iago.examples.LinePrintingExample
  import org.jboss.netty.handler.codec.http.HttpResponse
  """
  responseType = "HttpResponse"
  transport = "FinagleTransport"
  loadTest = "new LinePrintingExample(service.get, this)"
}
