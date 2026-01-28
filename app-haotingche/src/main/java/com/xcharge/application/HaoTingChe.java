package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import cloud.phusion.storage.KVStorage;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.util.CiphertextTransformer;
import com.xcharge.application.util.Transformer;


/**
 * 好停车
 * Protocol: ParkingDiscount
 */
public class HaoTingChe extends HttpBaseApplication {
    private final static String POSITION = HaoTingChe.class.getName();
    private static final String TOKEN_CACHE_KEY = "Token-";
    private static final String TOKEN_EXPIRE_CACHE_KEY = "TokenExpire-";
    private static final long TOKEN_EXPIRE_BUFFER_SECONDS = 300;

    @OutboundEndpoint
    public DataObject requestParkingDiscount(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String serviceUrl = getApplicationConfig().getString("serviceUrl") + "/platform/v1/api/tld/notification_charge_end_order_info";
        JSONObject config = getConnectionConfig(connectionId);

        JSONObject body;
        try {
            JSONObject objMsg = Transformer.translateRequestMessage(msg.getJSONObject());

            ctx.logInfo(POSITION, String.format("request ParkingDiscount HTTP POST %s  param %s", serviceUrl, objMsg));

            body = translateParam(objMsg, config == null ? null : config.getString("OperatorID"),
                    config == null ? null : config.getString("AES_KEY"),
                    config == null ? null : config.getString("AES_IV"),
                    config == null ? null : config.getString("SIGN_KEY"), ctx);
        } catch (Exception ex) {
            ctx.logError(POSITION, "Failed to translate the message", ex);
            return new DataObject("{\"code\":\"400\", \"desc\":\"" + ex.getMessage() + "\"}");
        }

        String token = getValidToken(connectionId, ctx);
        if (token == null || token.isEmpty()) {
            ctx.logError(POSITION, "Failed to get valid token");
            return new DataObject("{\"code\":\"500\", \"desc\":\"获取token失败\"}");
        }

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(serviceUrl)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Authorization", "Bearer " + token)
                .body(body.toString())
                .context(ctx)
                .send();

        try {
            JSONObject objResponse = response.getBody().getJSONObject();
            JSONObject objBody = translateResponseData(objResponse, config == null ? null : config.getString("AES_KEY"),
                    config == null ? null : config.getString("AES_IV"));

            ctx.logInfo(POSITION, String.format("request ParkingDiscount HTTP POST %s  result %s", serviceUrl, objBody));

            return new DataObject(Transformer.translateResponseMessage(objBody));
        } catch (Exception ex) {
            ctx.logError(POSITION, "Can not parse the response: " + response.getBody().getString(), ex);
            return new DataObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }

    private String getValidToken(String connectionId, Context ctx) {
        try {
            KVStorage storage = ctx.getEngine().getKVStorageForApplication(getId());
            String token = (String) storage.get(TOKEN_CACHE_KEY + connectionId, ctx);
            Long tokenExpireTime = (Long) storage.get(TOKEN_EXPIRE_CACHE_KEY + connectionId, ctx);

            if (token != null && !token.isEmpty()) {
                long currentTime = System.currentTimeMillis() / 1000;
                if (tokenExpireTime != null && tokenExpireTime > currentTime + TOKEN_EXPIRE_BUFFER_SECONDS) {
                    ctx.logInfo(POSITION, "Using cached token for connection: " + connectionId);
                    return token;
                }
            }
        } catch (Exception ex) {
            ctx.logError(POSITION, "Failed to retrieve token from storage", ex);
        }

        return refreshToken(connectionId, ctx);
    }

    private String refreshToken(String connectionId, Context ctx) {
        JSONObject config = getConnectionConfig(connectionId);
        String url = getApplicationConfig().getString("serviceUrl") + "/platform/v1/api/tld/query_token";

        String operatorId = config.getString("OperatorID");
        String operatorSecret = config.getString("OperatorSecret");
        String token = null;

        try {
            JSONObject param = new JSONObject() {{
                put("OperatorSecret", operatorSecret);
            }};

            ctx.logInfo(POSITION, String.format("Refreshing token: HTTP POST %s  param %s", url, param));

            JSONObject body = translateParam(param, operatorId, config.getString("AES_KEY"),
                    config.getString("AES_IV"),
                    config.getString("SIGN_KEY"), ctx);
            HttpClient http = ctx.getEngine().createHttpClient();
            HttpResponse response = http.post(url)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(body.toString()).context(ctx).send();
            JSONObject resBody = response.getBody().getJSONObject();
            JSONObject objBody = translateResponseData(resBody, config.getString("AES_KEY"), config.getString("AES_IV"));

            ctx.logInfo(POSITION, String.format("Refreshing token: HTTP POST %s  result %s", url, objBody));

            int ret = objBody.getIntValue("Ret", -1);
            if (ret == 0) {
                JSONObject data = (JSONObject) objBody.getOrDefault("Data", new JSONObject());
                token = data.getString("AccessToken");
                int tokenAvailableTime = data.getIntValue("TokenAvailableTime", 7200);

                if (token != null && token.length() > 0) {
                    KVStorage storage = ctx.getEngine().getKVStorageForApplication(getId());
                    storage.put(TOKEN_CACHE_KEY + connectionId, token, ctx);
                    long expireTime = System.currentTimeMillis() / 1000 + tokenAvailableTime;
                    storage.put(TOKEN_EXPIRE_CACHE_KEY + connectionId, expireTime, ctx);
                    ctx.logInfo(POSITION, "Token refreshed successfully, will expire in " + tokenAvailableTime + " seconds");
                }
            } else {
                ctx.logError(POSITION, "Failed to refresh token, Ret: " + ret + ", Msg: " + objBody.getString("Msg"));
            }
        } catch (Exception ex) {
            ctx.logError(POSITION, "Failed to refresh token", ex);
        }

        return token;
    }

    private JSONObject translateParam(JSONObject data, String operatorId, String secretKey, String secretIv, String sigSecret, Context ctx) {
        try {
            return CiphertextTransformer.translateRequestData(JSONObject.toJSONString(data), operatorId, secretKey, secretIv, sigSecret);
        } catch (Exception e) {
            ctx.logError(POSITION, "Failed to wrapParam ", e);
        }

        return new JSONObject();
    }

    private JSONObject translateResponseData(JSONObject data, String secretKey, String secretIv) {
        if (data != null && data.containsKey("Data")) {
            data.put("Data", CiphertextTransformer.translateResponseData(data.getString("Data"), secretKey, secretIv));
        }

        return data;
    }
}
