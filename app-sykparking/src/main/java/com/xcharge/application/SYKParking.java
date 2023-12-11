package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.utils.Transformer;

public class SYKParking extends HttpBaseApplication {
    private final static String _position = SYKParking.class.getName();

    @OutboundEndpoint
    public DataObject requestParkingDiscount(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        JSONObject config = getConnectionConfig(connectionId);
        String appId = config.getString("appId");
        String mchId = config.getString("mchId");
        String salt = config.getString("saltMd5Key");
        String key = config.getString("desKey");
        String serviceUrl = getApplicationConfig().getString("serviceUrl");

        JSONObject result = null;

        // Step 1: 查询车辆入场记录
        result = _doRequest("query", msg, result, serviceUrl + "getParkingInfo", mchId, appId, salt, key, ctx);
        if (!"ok".equals(result.getString("code"))) return new DataObject(result); // 如果请求不成功，直接返回

        // Step 2: 请求发放优惠券
        result = _doRequest("discount", msg, result, serviceUrl + "bindCoupon", mchId, appId, salt, key, ctx);
        return new DataObject(result);
    }

    private JSONObject _doRequest(String action, DataObject msg, JSONObject lastResult, String serviceUrl, String mchId, String appId, String salt, String key, Context ctx) throws Exception {
        HttpClient http = ctx.getEngine().createHttpClient();
        JSONObject objMsg;

        try {
            if (action.equals("query"))
                objMsg = Transformer.translateQueryRequestMessage(msg.getJSONObject(), mchId, appId, salt, key);
            else
                objMsg = Transformer.translateDiscountRequestMessage(msg.getJSONObject(), lastResult, mchId, appId, salt, key);
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
            return Transformer.translateResponseMessage(objResponse, key);
        } catch (Exception ex) {
            ctx.logError(_position, "Can not parse the response body: " + response.getBody().getString(), ex);
            return JSON.parseObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }
}
