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
 * 领悟洗车
 * Protocol: CarWashDiscount
 */
public class Lingwu extends HttpBaseApplication {
    private final static String _position = Lingwu.class.getName();
    private final static String COUPON_COMMAND = "/Coupon/CouponWashCarForPay";

    @OutboundEndpoint
    public DataObject requestCarWashDiscount(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String serviceUrl = getApplicationConfig().getString("serviceUrl");
        String secret = getConnectionConfig(connectionId).getString("loginSecret");
        String key = getConnectionConfig(connectionId).getString("appKey");

        JSONObject objMsg;
        try {
            objMsg = Transformer.translateRequestMessage(msg.getJSONObject(), key, secret);
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to translate the message", ex);
            return new DataObject("{\"code\":\"400\", \"desc\":\""+ex.getMessage()+"\"}");
        }

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(serviceUrl+COUPON_COMMAND)
                .header("LoginSecret", secret)
                .header("Content-Type", "application/json; charset=utf-8")
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
