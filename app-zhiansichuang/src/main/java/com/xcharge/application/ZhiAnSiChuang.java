package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.util.Transformer;

public class ZhiAnSiChuang extends HttpBaseApplication {
    private final static String _position = ZhiAnSiChuang.class.getName();
    @OutboundEndpoint
    public DataObject requestParkingDiscount(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String serviceUrl = getApplicationConfig().getString("serviceUrl");
        JSONObject connConfig = getConnectionConfig(connectionId);

        String unionId = connConfig.getString("unionId");
        String secret = connConfig.getString("secret");
        String reqId = "" + ctx.getEngine().generateUniqueId(ctx);

        String objMsg;
        try {
            objMsg = Transformer.translateRequestMessage(msg.getJSONObject(), unionId, secret, reqId);
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to translate the message", ex);
            return new DataObject("{\"code\":\"400\", \"desc\":\""+ex.getMessage()+"\"}");
        }

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(serviceUrl)
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body( new DataObject(objMsg) )
                .context(ctx)
                .send();

        try {
            String objResponse = response.getBody().getString();
            return new DataObject(Transformer.translateResponseMessage(objResponse));
        } catch (Exception ex) {
            ctx.logError(_position, "Can not parse the response: "+response.getBody().getString(), ex);
            return new DataObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }
}
