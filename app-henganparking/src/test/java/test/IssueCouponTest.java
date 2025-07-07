package test;

import cloud.phusion.Engine;
import cloud.phusion.dev.TestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.HengAn;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class IssueCouponTest {
    private static Engine engine;

    @BeforeClass
    public static void setUp() throws Exception {
        engine = TestUtil.buildPhusionEngine()
                .done();
        engine.start(null);
    }

    @Test
    public void testDiscount() throws Exception {
        String applicationId = "hengan";
        String endpointId = "requestParkingDiscount";

        String applicationConfig = "{\"serviceUrl\": \"http://abc.abc.com/park/abc/abc/abc\"}";

        String connectionConfig = "{\"OperatorID\":\"test1\"}";

        String msg = "{\n" +
                "  \"requestId\": \"abc\",\n" +
                "  \"parkId\": \"abc\",\n" +
                "  \"carNo\": \"abc\",\n" +
                "  \"extraInfo\": {\n" +
                "    \"startTime\": \"2024-11-13 13:50:01\",\n" +
                "    \"endTime\": \"2024-11-13 14:54:35\"\n" +
                "  },\n" +
                "  \"type\": 1,\n" +
                "  \"value\": 5\n" +
                "}";

        TestUtil.registerApplication()
                .setEngine(engine)
                .setApplicationClass(HengAn.class.getName())
                .setApplicationId(applicationId)
                .setApplicationConfig(applicationConfig)
                .setConnectionConfig(connectionConfig)
                .setEndpointToTest(endpointId)
                .done();

        String result = TestUtil.callOutboundEndpoint(engine, applicationId, endpointId, msg);

        System.out.println();
        System.out.println("Result: " + result);
        System.out.println();

        JSONObject objMsg = JSON.parseObject(result);
        assertTrue(objMsg.containsKey("code"));

        TestUtil.unregisterApplication(engine, applicationId);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        engine.stop(null);
    }

}
