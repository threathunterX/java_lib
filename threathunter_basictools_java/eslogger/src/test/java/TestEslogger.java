import com.threathunter.babel.meta.ServiceMeta;
import com.threathunter.babel.rpc.RemoteException;
import com.threathunter.babel.rpc.impl.ServiceClientImpl;
import com.threathunter.babel.util.BabelMetricsHelper;
import com.threathunter.basictools.eslogger.EsloggerServer;
import com.threathunter.metrics.MetricsAgent;
import com.threathunter.model.Event;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * created by www.threathunter.cn
 */
public class TestEslogger {

    public static final String VALUE = "{\n" +
            "    \"error\": \"no such method\",\n" +
            "    \"message\": \"at line 10\"\n" +
            "}";
    EsloggerServer main;
    ServiceClientImpl client;
    Event event;

    @Before
    public void setUp() {
        MetricsAgent.getInstance().start("/Users/threathunter-dev/Code_threathunter/java_fx/threathunter.basictools/eslogger/src/test/resources/metrics.yml");
//        main = new EsloggerServer("/home/daisy/MYCODE/java_fx/threathunter.basictools/eslogger/src/main/resources/babel.yml",
//                "/home/daisy/MYCODE/java_fx/threathunter.basictools/eslogger/src/main/resources/es.yml",
//                "/home/daisy/MYCODE/java_fx/threathunter.basictools/eslogger/src/main/resources/eslogger.json");
//        main.startServer();
    }

    @After
    public void tearDown() throws IOException {
//        main.stopServer();
        client.close();
//        EsloggerMetricsHelper.getInstance().releaseMetricsHelper();
        MetricsAgent.getInstance().stop();
    }

    @Test
    public void testLogger() throws RemoteException, InterruptedException {
        client = new ServiceClientImpl(getClientMeta(), "esloggerclient");
        client.start();
        client.bindService(getServiceMeta());
        this.event = getEvent();

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            System.out.println("[Send]" + new Date().toString());
            client.notify(event, "eslogger");
            Thread.sleep(1000);
        }
    }

    private ServiceMeta getServiceMeta() {
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setName("eslogger");
        serviceMeta.setCallMode("notify");
        serviceMeta.setDeliverMode("shuffle");
        serviceMeta.setServerImpl("rabbitmq");
        serviceMeta.setCoder("mail");
        serviceMeta.setOption("durable", true);
        serviceMeta.setOption("sdc", "sh");
        serviceMeta.setOption("cdc", "sh");
        return serviceMeta;
    }

    private Event getEvent() {
        Event event = new Event("fx_test2", "eslog", "123");
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("index", "fx_eslogger_ts");
        values.put("type", "test");
        values.put("level", "warn");

        values.put("payload", "{\n" +
                "\"error\":\"no such method\",\n" +
                "\"message\":\"at line 10\",\n" +
                "\"timestamp\":1442546148000\n" +
                "}");
        event.setPropertyValues(values);
        return event;
    }

    private ServiceMeta getClientMeta() {
        ServiceMeta serviceMeta = new ServiceMeta();
        serviceMeta.setCallMode("notify");
        serviceMeta.setDeliverMode("shuffle");
        serviceMeta.setCoder("mail");
        serviceMeta.setName("eslogger");
        serviceMeta.setServerImpl("rabbitmq");
        serviceMeta.setOption("sdc", "sh");
        serviceMeta.setOption("cdc", "sh");
        serviceMeta.setOption("durable", true);

        return serviceMeta;
    }
}
