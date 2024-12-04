package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.util.Transformer;

public class ZhiWeiYun extends HttpBaseApplication {
    private final static String _position = ZhiWeiYun.class.getName();

    @OutboundEndpoint
    public DataObject requestParkingDiscount(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String serviceUrl = getApplicationConfig().getString("serviceUrl");
        JSONObject config = getConnectionConfig(connectionId);
        String appId = config.getString("appId");
        String appAuthToken = config.getString("appAuthToken");
        return new DataObject(_doRequest(msg, serviceUrl, appId, appAuthToken, ctx));
    }

    private JSONObject _doRequest(DataObject msg, String serviceUrl, String appId, String appAuthToken, Context ctx) throws Exception {
        HttpClient http = ctx.getEngine().createHttpClient();
        JSONObject objMsg;

        try {
            objMsg = Transformer.translateRequestMessage(msg.getJSONObject(), appId, appAuthToken);
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to translate the message: " + msg.getString(), ex);
            return JSON.parseObject("{\"code\":\"501\", \"desc\":\"" + ex.getMessage() + "\"}");
        }

        HttpResponse response = http.post(serviceUrl)
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(new DataObject(objMsg))
                .context(ctx)
                .send();

        try {
            JSONObject objResponse = response.getBody().getJSONObject();
            return Transformer.translateResponseMessage(objResponse);
        } catch (Exception ex) {
            ctx.logError(_position, "Can not parse the response body: " + response.getBody().getString(), ex);
            return JSON.parseObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }
}
