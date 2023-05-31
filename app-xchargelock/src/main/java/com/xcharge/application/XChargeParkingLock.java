package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.Engine;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.InboundEndpoint;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.integration.Integration;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpMethod;
import cloud.phusion.protocol.http.HttpRequest;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.util.Transformer;

import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class XChargeParkingLock extends HttpBaseApplication {
    private final static String _position = XChargeParkingLock.class.getName();

    @InboundEndpoint(address = "/action/{client}/{joinId}", connectionKeyInConfig = "client", connectionKeyInReqeust = "client")
    public void performAction(HttpRequest request, HttpResponse response, String[] integrationIds,
                              String connectionId, Context ctx) throws Exception {
        response.setStatusCode(200);
        response.setHeader("Content-Type", "application/json; charset=UTF-8");

        if (!request.getMethod().equals(HttpMethod.POST)) {
            response.setBody(new DataObject("{\"errCode\": \"200\", \"msg\": \"请求格式错误\"}"));
            return;
        }

        if (integrationIds == null || integrationIds.length == 0) {
            response.setBody(new DataObject("{\"errCode\": \"900\", \"msg\": \"尚无可用集成流程\"}"));
        } else {
            JSONObject msg;
            try {
                msg = JSONObject.parseObject(URLDecoder.decode(request.getParameter("info")));
            } catch (Exception ex) {
                ctx.logError(_position, "Can not parse the request body", ex);
                response.setBody(new DataObject("{\"errCode\": \"200\", \"msg\": \"请求格式错误\"}"));
                return;
            }
            msg.put("joinId",request.getParameter("joinId"));
            DataObject objMsg = new DataObject(Transformer.translateAction(msg));

            Engine engine = ctx.getEngine();
            boolean hasResult = false;

            for (int i = 0; i < integrationIds.length; i++) {
                Integration it = engine.getIntegration(integrationIds[i]);
                DataObject result = it.execute(objMsg, ctx);

                if (result != null) {
                    JSONObject res = Transformer.translateResponseMessage(result.getJSONObject());
                    response.setBody(new DataObject(res));
                    hasResult = true;
                }
            }

            if (!hasResult) {
                response.setBody(new DataObject("{\"code\": \"901\", \"desc\": \"未匹配到集成流程\"}"));
            }
        }
    }

    @OutboundEndpoint
    public DataObject statusNotification(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String serviceUrl = getApplicationConfig().getString("serviceUrl");
        JSONObject connConfig = getConnectionConfig(connectionId);

        HttpClient http = ctx.getEngine().createHttpClient();
        JSONObject objMsg;
        try {
            objMsg = Transformer.translateNotifyMessage(msg.getJSONObject());
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to translate the message", ex);
            return new DataObject("{\"error\":\"400\", \"desc\":\"" + ex.getMessage() + "\"}");
        }

        HttpResponse response = http.post(serviceUrl + msg.getJSONObject().getString("joinId"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(new DataObject(objMsg))
                .context(ctx)
                .send();

        try {
            JSONObject objResponse = response.getBody().getJSONObject();
            return new DataObject("{\"code\":\"ok\"}");
        } catch (Exception ex) {
            ctx.logError(_position, "Can not parse the response: " + response.getBody().getString(), ex);
            return new DataObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }
}
