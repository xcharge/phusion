package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.util.Transformer;

/**
 * 江苏慧泽快捷停车
 * Protocol: ParkingDiscount
 */
public class ParkingQuickly extends HttpBaseApplication {
    private final static String _position = ParkingQuickly.class.getName();

    @OutboundEndpoint
    public DataObject requestParkingDiscount(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        JSONObject config = getConnectionConfig(connectionId);
        String channel = config.getString("channel");
        String serviceUrl = config.getString("serviceUrl");
        String secret = config.getString("secret");

        JSONObject objMsg;
        try {
            objMsg = Transformer.translateRequestMessage(msg.getJSONObject());
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to translate the message", ex);
            return new DataObject("{\"code\":\"400\", \"desc\":\""+ex.getMessage()+"\"}");
        }

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post( serviceUrl+Transformer.getURLFromMessage(objMsg, channel, secret) )
                .header("Content-Type", "application/json; charset=UTF-8")
                .body( new DataObject(objMsg) )
                .context(ctx)
                .send();

        try {
            JSONObject objResponse = response.getBody().getJSONObject();
            return new DataObject(Transformer.translateResponseMessage(objResponse));
        } catch (Exception ex) {
            ctx.logError(_position, "Can not parse the response: "+response.getBody().getString(), ex);
            return new DataObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }

}
