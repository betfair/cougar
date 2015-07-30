---
layout: default
---
# Unit Testing

All your application code should live in the `application` submodule of your Cougar project, so your unit tests should go there too.

# Integration Testing

## Manual

Depending on the interface/operation under test, you can use

* SoapUI (or similar SOAP service testing client)
* A browser (for RESCRIPT operations that only take query parameters)
* `curl` (for any RESCRIPT operation - see [examples](Cougar_Baseline_Service_RESCRIPT_curls.html))
* An HTTP debugger/request builder such as Fiddler2

## Automated

You may well want to do some sanity type integration testing, either during development or before a QA release, and you'll
want this to run as part of a CI build.

## Black Box Testing

## Testing using the Cougar Client

* Create a `client` Maven submodule (contains only IDD-generated client code, Spring assembly, and default properties).
* Add a `test` scope dependency on this from your `launcher` module
* Add an integration test class to the `launcher` module.  Sample code follows:

    private static ExampleClient client;
    private static ClassPathXmlApplicationContext context;
    @BeforeClass
    public static void startCougar() throws InterruptedException {
        // Find and stipulate free ports
        int jettyPort = findFreePort(9001);
        int jmxPort = findFreePort(jettyPort + 1);
        int executionVenuePort = findFreePort(jmxPort + 1);
        int socketServerPort = findFreePort(executionVenuePort + 1);
        System.setProperty("jetty.http.port", "" + jettyPort);
        System.setProperty("jmx.html.port", "" + jmxPort);
        System.setProperty("cougar.ev.port", "" + executionVenuePort);
        System.setProperty("cougar.socket.serverport", "" + socketServerPort);
        // Set the endpoint to connect to
        System.setProperty("cougar.client.rescript.remoteaddress", "http://127.0.0.1:" <u> jettyPort </u> "/");
        // Inconveniences (fix pending)
        System.setProperty("betfair.config.host", "/conf/");
        // Start Cougar programmatically
        context = (ClassPathXmlApplicationContext) new CougarSpringCtxFactoryImpl().create(null);
        // Get the client (defined in the 'client' project itself) to test on
        client = (ExampleClient) context.getBean("exampleClient");
    }
    @AfterClass
    public static void stopCougar() throws InterruptedException {
        context.close();
    }
    @Test
    public void testEcho() throws Exception {
        Echo actual = client.echo("hello");
        assertEquals("hello", actual.getEchoMessage());
    }
    /****
     * Implementation pasted from Apache Mina project.
     */
    private static boolean available(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            //
        } finally {
            if (ds != null) {
                ds.close();
            }
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    //
                }
            }
        }
        return false;
    }
    private static int findFreePort(int start) {
        for (int port = start; port < start <u> 100; port</u>+) {
            if (available(port)) {
                return port;
            }
        }
        throw new RuntimeException("Can't find a free port...");
    }

## Testing from the Command Line

You could elect to test your service using `curl` (or alternative; examples of curl invocations can be
[found here](Using_curl_to_Communicate_with_RESCRIPT_Services_in_Cougar.html)), but this seems like a poor alternative
to testing with a Java client.

## White Box Testing

There exists a `cougar-testing-services` module, which can be included in your Cougar application by dropping its
JAR into the `lib` directory of the deployed application.   It has some white box testing features like cache management
and log entry query.  For those services that use it, the testing services module gets dropped into the relevant place as
part of dev/QA deployment automation.

The operations listed by the interface (which you can see if you look at Cougar source, under the `cougar-testing-service` Maven module) are:

* `refreshAllCaches`
* `refreshCache`
* `getIDD`
* `getLogEntries`
* `getLogEntriesByDateRange`

There is no other documentation about this module, so if you need further details then please contact us.

You can always develop your own white box testing service to co-locate with the services you own if you desire.