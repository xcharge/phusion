package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.util.AESUtils;
import com.xcharge.application.util.Transformer;
import org.apache.commons.codec.binary.Base64;

import java.security.Key;

public class LiFang extends HttpBaseApplication {
    private final static String _position = LiFang.class.getName();

    @OutboundEndpoint
    public DataObject requestParkingDiscount(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String serviceUrl = getApplicationConfig().getString("serviceUrl");
        JSONObject config = getConnectionConfig(connectionId);
        String key = config.getString("key");
        return new DataObject(_doRequest(msg, key, serviceUrl, ctx));
    }

    private JSONObject _doRequest(DataObject msg, String key, String url, Context ctx) throws Exception {
        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(url)
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(Transformer.transformerDownloadThirdReductionServiceMessage(msg.getJSONObject(), key, _position, ctx))
                .context(ctx)
                .send();

        try {
            ctx.logInfo(_position, String.format("request LiFang HTTP POST %s ;PARAM %s ;result %s", url, msg, response));
            return Transformer.translateResponseMessage(response.getBody().getString(), key, _position, ctx);
        } catch (Exception ex) {
            ctx.logError(_position, "Can not parse the response body: " + response.getBody().getString(), ex);
            return JSON.parseObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }
}
