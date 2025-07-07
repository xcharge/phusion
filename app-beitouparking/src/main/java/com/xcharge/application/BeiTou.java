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

public class BeiTou extends HttpBaseApplication {
    private final static String _position = BeiTou.class.getName();

    @OutboundEndpoint
    public DataObject requestParkingDiscount(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String serviceUrl = getApplicationConfig().getString("serviceUrl");
        JSONObject config = getConnectionConfig(connectionId);
        String key = config.getString("key");
        String secret = config.getString("secret");
        JSONObject parkingInfo = parkingInfo(serviceUrl, key, secret, msg.getJSONObject(), ctx);
        return new DataObject(payFee(serviceUrl, key, secret, msg.getJSONObject(), parkingInfo, ctx));
    }

    private JSONObject payFee(String url, String key, String secret, JSONObject data, JSONObject parkingInfo, Context ctx) {
        JSONObject msg = new JSONObject();
        try {
            if (parkingInfo == null || parkingInfo.getString("consumeUUID") == null) {
                return JSON.parseObject("{\"code\":\"500\", \"desc\":\"consumeUUID is null\"}");
            }
            msg = Transformer.transformerPayFeeMessage(key, secret, parkingInfo.getString("consumeUUID"), data);
            return Transformer.translateResponseMessage(_doRequest(msg, url, "/api/deduction2/payFee", ctx));
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to translate the message: " + msg, ex);
            return JSON.parseObject("{\"code\":\"501\", \"desc\":\"" + ex.getMessage() + "\"}");
        }
    }

    private JSONObject parkingInfo(String url, String key, String secret, JSONObject data, Context ctx) {
        JSONObject msg = new JSONObject();
        try {
            msg = Transformer.transformerParkingInfoMessage(key, secret, data);
            return _doRequest(msg, url, "/api/deduction2/parkingInfo", ctx);
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to translate the message: " + msg, ex);
            return JSON.parseObject("{\"code\":\"501\", \"desc\":\"" + ex.getMessage() + "\"}");
        }
    }

    private JSONObject _doRequest(JSONObject objMsg, String url, String path, Context ctx) throws Exception {
        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(url + path)
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(new DataObject(objMsg))
                .context(ctx)
                .send();

        try {
            ctx.logInfo(_position, String.format("request ParkingDiscount HTTP POST %s ;PARAM %s ;result %s", url + path, objMsg, response));
            return response.getBody().getJSONObject();
        } catch (Exception ex) {
            ctx.logError(_position, "Can not parse the response body: " + response.getBody().getString(), ex);
            return JSON.parseObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }

}
