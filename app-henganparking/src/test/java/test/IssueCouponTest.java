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

        String applicationConfig = "{\"serviceUrl\": \"http://beta.52tingche.com/park/zyb/couponTripartite/generalGrant\"}";

        String connectionConfig = "{\"OperatorID\":\"test1\"}";

        String msg = "{\n" +
                "  \"requestId\": \"678539775491710976\",\n" +
                "  \"parkId\": \"1338779870054248449\",\n" +
                "  \"carNo\": \"鄂E99112\",\n" +
                "  \"extraInfo\": {\n" +
                "    \"startTime\": \"2023-10-18 13:50:01\",\n" +
                "    \"endTime\": \"2023-10-18 14:54:35\"\n" +
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
