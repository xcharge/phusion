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
import com.xcharge.application.util.CiphertextTransformer;
import com.xcharge.application.util.Transformer;


/**
 * 绿城产服停车
 * Protocol: ParkingDiscount
 */
public class GreenTown extends HttpBaseApplication implements ScheduledTask {
    private final static String POSITION = GreenTown.class.getName();

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
        String serviceUrl = getApplicationConfig().getString("serviceUrl") + "/notification_charge_end_order_info";
        JSONObject config = getConnectionConfig(connectionId);

        JSONObject body;
        try {
            JSONObject objMsg = Transformer.translateRequestMessage(msg.getJSONObject());

            ctx.logInfo(POSITION, String.format("request ParkingDiscount HTTP POST %s  param %s", serviceUrl, objMsg));

            body = translateParam(objMsg, config == null ? null : config.getString("OperatorID"),
                    config == null ? null : config.getString("SecretKey"),
                    config == null ? null : config.getString("SecretIv"),
                    config == null ? null : config.getString("SigSecret"), ctx);
        } catch (Exception ex) {
            ctx.logError(POSITION, "Failed to translate the message", ex);
            return new DataObject("{\"code\":\"400\", \"desc\":\"" + ex.getMessage() + "\"}");
        }

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(serviceUrl)
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Authorization", "Bearer " + getToken(connectionId, ctx))
                .body(body.toString())
                .context(ctx)
                .send();

        try {
            JSONObject objResponse = response.getBody().getJSONObject();
            JSONObject objBody = translateResponseData(objResponse, config == null ? null : config.getString("SecretKey"),
                    config == null ? null : config.getString("SecretIv"));

            ctx.logInfo(POSITION, String.format("request ParkingDiscount HTTP POST %s  result %s", serviceUrl, objBody));

            return new DataObject(Transformer.translateResponseMessage(objBody));
        } catch (Exception ex) {
            ctx.logError(POSITION, "Can not parse the response: " + response.getBody().getString(), ex);
            return new DataObject("{\"code\":\"500\", \"desc\":\"响应格式错误\"}");
        }
    }

    private String getToken(String connectionId, Context ctx) {
        try {
            KVStorage storage = ctx.getEngine().getKVStorageForApplication(getId());
            String token = (String) storage.get("Token-" + connectionId, ctx);
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
            ctx.logInfo(POSITION, "refresh token connId:" + connId);
            refreshToken(connId, ctx);
        }
    }

    private String refreshToken(String connectionId, Context ctx) {
        JSONObject config = getConnectionConfig(connectionId);
        String url = getApplicationConfig().getString("serviceUrl") + "/query_token";

        String operatorId = config.getString("OperatorID");
        String operatorSecret = config.getString("OperatorSecret");
        String token = null;

        try {
            JSONObject param = new JSONObject() {{
                put("OperatorID", operatorId);
                put("OperatorSecret", operatorSecret);
            }};

            ctx.logInfo(POSITION, String.format("Refreshing token: HTTP POST %s  param %s", url, param));

            JSONObject body = translateParam(param, operatorId, config.getString("SecretKey"),
                    config.getString("SecretIv"),
                    config.getString("SigSecret"), ctx);
            HttpClient http = ctx.getEngine().createHttpClient();
            HttpResponse response = http.post(url)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(body.toString()).context(ctx).send();
            JSONObject resBody = response.getBody().getJSONObject();
            JSONObject objBody = translateResponseData(resBody, config.getString("SecretKey"), config.getString("SecretIv"));

            ctx.logInfo(POSITION, String.format("Refreshing token: HTTP POST %s  result %s", url, objBody));

            JSONObject data = (JSONObject) objBody.getOrDefault("Data", new JSONObject());
            token = data.getString("AccessToken");

            if (token != null && token.length() > 0) {
                KVStorage storage = ctx.getEngine().getKVStorageForApplication(getId());
                storage.put("Token-" + connectionId, token, ctx);
            }
        } catch (Exception ex) {
            ctx.logError(POSITION, "Failed to refresh token", ex);
        }

        return token;
    }

    private JSONObject translateParam(JSONObject data, String operatorId, String secretKey, String secretIv, String sigSecret, Context ctx) {
        try {
            return CiphertextTransformer.translateRequestData(data.toString(), operatorId, secretKey, secretIv, sigSecret);
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
