package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.InboundEndpoint;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpMethod;
import cloud.phusion.protocol.http.HttpRequest;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.util.Transformer;
import cloud.phusion.Engine;
import cloud.phusion.integration.Integration;

import java.util.Map;

public class DingDing extends HttpBaseApplication {
    private final static String _position = DingDing.class.getName();

    @OutboundEndpoint
    public DataObject performAction(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String serviceUrl = getApplicationConfig().getString("serviceUrl");
        JSONObject connConfig = getConnectionConfig(connectionId);
        String appId = connConfig.getString("appId");
        String secret = connConfig.getString("secret");
        String reqId = "" + ctx.getEngine().generateUniqueId(ctx);

        String objMsg;
        JSONObject action;
        try {
            objMsg = Transformer.translateRequestMessage(msg.getJSONObject(), appId, secret, reqId);
            action = Transformer.getAction(msg.getJSONObject());
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to translate the message", ex);
            return new DataObject("{\"code\":\"400\", \"desc\":\"" + ex.getMessage() + "\"}");
        }

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(serviceUrl + action.getString("action") + ".action")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .body(objMsg)
                .context(ctx)
                .send();

        try {
            JSONObject objResponse = response.getBody().getJSONObject();
            objResponse.put("outDeviceNum", msg.getJSONObject().getString("lockId"));
            return new DataObject(Transformer.translateResponseMessage(objResponse,action));
        } catch (Exception ex) {
            ctx.logError(_position, "Can not parse the response: " + response.getBody().getString(), ex);
            return new DataObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }

    @InboundEndpoint(address = "/notification/{client}/{joinId}", connectionKeyInConfig = "client", connectionKeyInReqeust = "client")
    public void statusNotification(HttpRequest request, HttpResponse response, String[] integrationIds,
                                   String connectionId, Context ctx) throws Exception {

        response.setStatusCode(200);
        response.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        if (!request.getMethod().equals(HttpMethod.POST)) {
            response.setBody(new DataObject("{\"errCode\": \"200\", \"errMsg\": \"请求格式错误\"}"));
            return;
        }

        if (integrationIds == null || integrationIds.length == 0) {
            response.setBody(new DataObject("{\"errCode\": \"900\", \"errMsg\": \"尚无可用集成流程\"}"));
        } else {
            Map<String, String> msg = request.getParameters();

            msg.put("joinId", request.getParameter("joinId"));
            DataObject objMsg = new DataObject(Transformer.translateMessage(msg));

            Engine engine = ctx.getEngine();
            boolean hasResult = false;

            for (String integrationId : integrationIds) {
                Integration it = engine.getIntegration(integrationId);
                DataObject result = it.execute(objMsg, ctx);

                if (result != null) {
                    JSONObject res = Transformer.translatePCResponseMessage(JSONObject.parseObject(result.getString()));
                    response.setBody(new DataObject(res));
                    hasResult = true;
                }
            }
            if (!hasResult) {
                response.setBody(new DataObject("{\"errCode\": \"901\", \"errMsg\": \"未匹配到集成流程\"}"));
            }
        }
    }
}