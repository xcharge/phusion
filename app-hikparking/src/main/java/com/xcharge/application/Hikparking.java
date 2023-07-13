package com.xcharge.application;

import cloud.phusion.Context;
import cloud.phusion.DataObject;
import cloud.phusion.ScheduledTask;
import cloud.phusion.application.HttpBaseApplication;
import cloud.phusion.application.OutboundEndpoint;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import cloud.phusion.storage.KVStorage;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.util.Transformer;


/**
 * 海康云停车
 * Protocol: ParkingDiscount
 */
public class Hikparking extends HttpBaseApplication implements ScheduledTask {
    private final static String POSITION = Hikparking.class.getName();

    @Override
    public void onStart(Context ctx) throws Exception {
        String taskId = getId() + "TokenRefreshingTask";
        // Run every 10 minutes
        int interval = 600;

        // Run forever
        int times = 0;

        ctx.getEngine().scheduleTask(taskId, this, interval, times, ctx);
    }

    @Override
    public void onStop(Context ctx) throws Exception {
        String taskId = getId() + "TokenRefreshingTask";

        ctx.getEngine().removeScheduledTask(taskId, ctx);
    }

    @Override
    public void onConnect(String connectionId, JSONObject config, Context ctx) {
        refreshToken(connectionId, ctx);
    }

    @Override
    public void run(String taskId, Context ctx) {
        refreshAllTokens(ctx);
    }

    @OutboundEndpoint
    public DataObject requestParkingDiscount(DataObject msg, String integrationId, String connectionId, Context ctx) throws Exception {
        String serviceUrl = getApplicationConfig().getString("serviceUrl") + "/api/v1/sendCoupon";
        JSONObject config = getConnectionConfig(connectionId);

        JSONObject objMsg;
        try {
            objMsg = Transformer.translateRequestMessage(msg.getJSONObject(), config == null ? null : config.getString("couponSource"));
            ctx.logInfo(POSITION, String.format("request ParkingDiscount HTTP POST %s  param %s", serviceUrl, objMsg));
        } catch (Exception ex) {
            ctx.logError(POSITION, "Failed to translate the message", ex);
            return new DataObject("{\"code\":\"400\", \"desc\":\"" + ex.getMessage() + "\"}");
        }

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(serviceUrl)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("access_token", getToken(connectionId, ctx))
                .body(objMsg.toString())
                .context(ctx)
                .send();

        try {
            JSONObject objResponse = response.getBody().getJSONObject();
            ctx.logInfo(POSITION, String.format("request ParkingDiscount HTTP POST %s  result %s", serviceUrl, objResponse));

            return new DataObject(Transformer.translateResponseMessage(objResponse));
        } catch (Exception ex) {
            ctx.logError(POSITION, "Can not parse the response: " + response.getBody().getString(), ex);
            return new DataObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }

    private String getToken(String connectionId, Context ctx) {
        try {
            KVStorage storage = ctx.getEngine().getKVStorageForApplication(getId());
            String token = (String) storage.get("Token-" + connectionId);
            if (token != null) {
                return token;
            }
        } catch (Exception ex) {
            ctx.logError(POSITION, "Failed to retrieve token from storage", ex);
            return null;
        }

        return refreshToken(connectionId, ctx);
    }

    private void refreshAllTokens(Context ctx) {
        String[] connIds = getConnectionIds(true);
        if (connIds == null || connIds.length == 0) {
            return;
        }

        for (String connId : connIds) {
            refreshToken(connId, ctx);
        }
    }

    private String refreshToken(String connectionId, Context ctx) {
        JSONObject config = getConnectionConfig(connectionId);
        String url = getApplicationConfig().getString("serviceUrl") + "/oauth/token";

        String clientId = config.getString("clientId");
        String clientSecret = config.getString("clientSecret");
        String token = null;

        try {
            JSONObject param = new JSONObject() {{
                put("client_id", clientId);
                put("client_secret", clientSecret);
            }};

            ctx.logInfo(POSITION, String.format("Refreshing token: HTTP POST %s  param %s", url, param));

            HttpClient http = ctx.getEngine().createHttpClient();
            HttpResponse response = http.post(url)
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .body(Transformer.convertToQuery(param)).context(ctx).send();
            JSONObject resBody = response.getBody().getJSONObject();

            ctx.logInfo(POSITION, String.format("Refreshing token: HTTP POST %s  result %s", url, resBody));

            token = resBody == null ? null : resBody.getString("access_token");

            if (token != null && token.length() > 0) {
                KVStorage storage = ctx.getEngine().getKVStorageForApplication(getId());
                storage.put("Token-" + connectionId, token, ctx);
            }
        } catch (Exception ex) {
            ctx.logError(POSITION, "Failed to refresh token", ex);
        }

        return token;
    }
}
