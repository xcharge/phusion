package com.xcharge.application.impl;

import cloud.phusion.Context;
import cloud.phusion.Engine;
import cloud.phusion.protocol.http.HttpClient;
import cloud.phusion.protocol.http.HttpResponse;
import cloud.phusion.storage.FileStorage;
import cloud.phusion.storage.KVStorage;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.util.SignUtil;

import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SiwoService {
    private final String _position = SiwoService.class.getName();
    private String applicationId;
    private String serviceUrl;
    private int intervalTokenExpire;

    private Map<String, PrivateKey> keys = new HashMap<>();

    public SiwoService(String applicationId, String serviceUrl, int intervalTokenExpire) {
        super();
        this.applicationId = applicationId;
        this.serviceUrl = serviceUrl;
        this.intervalTokenExpire = intervalTokenExpire * 1000;
    }

    public String retrieveToken(String parkId, String keyPath, Context ctx) throws Exception {
        return retrieveToken(parkId, keyPath, false, ctx);
    }

    public String retrieveToken(String parkId, String keyPath, boolean forceRefreshing, Context ctx) throws Exception {
        String tokenKey = "Token-" + parkId; // 每个停车场只能对接给一个客户，因此只保存一个 Token
        Engine engine = ctx.getEngine();
        KVStorage kvstorage = engine.getKVStorageForApplication(applicationId);

        // Firstly, try to fetch token from KV storage
        // Before expiring, the token will always be available (not invalidated by others), so do not need to refresh

        if (!forceRefreshing) {
            try {
                String token = (String) kvstorage.get(tokenKey);
                if (token != null) return token;
            } catch (Exception ex) {
                ctx.logError(_position, "Failed to retrieve token from storage", ex);
                return null;
            }
        }

        // If not in the storage, fetch token from web service

        PrivateKey key = _getPrivateKey(keyPath, ctx);
        if (key == null) return null;

        String token = null;

        ctx.setContextInfo("parkId", parkId);

        try {
            HttpClient http = engine.createHttpClient();

            JSONObject params = new JSONObject();
            params.put("action", "login");
            params.put("parkid", parkId);
            StringBuilder body = SignUtil.coverJSONObject2String(params);
            String sign = SignUtil.signWithPrvateKey(body.toString(), key);
            body.append("&sign=").append(sign);

            HttpResponse response = http.post(serviceUrl)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(body.toString())
                    .context(ctx)
                    .send();

            if (response.getStatusCode() < 300) {
                JSONObject objBody = response.getBody().getJSONObject();
                if (objBody!=null && objBody.getBooleanValue("success") && objBody.containsKey("data")) {
                    JSONObject data = objBody.getJSONArray("data").getJSONObject(0);
                    token = data.getString("token");

                    kvstorage.put(tokenKey, token, intervalTokenExpire, ctx);
                }
                else
                    ctx.logError(_position, "Token not returned properly", "body="+response.getBody().getString());
            } else {
                ctx.logError(_position, "Token not returned properly", "httpCode="+response.getStatusCode());
            }
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to query token", ex);
        }

        ctx.removeContextInfo("parkId");
        return token;
    }

    /**
     * Return {"success":true, "message":"", "discountid":"28083"}
     */
    public JSONObject issueCoupon(int discountType, double discountValue, String validDate, String plateNum,
                                  String parkId, String keyPath, Context ctx) throws Exception {
        String token = retrieveToken(parkId, keyPath, ctx);
        if (token == null) return null;

        PrivateKey key = _getPrivateKey(keyPath, ctx);
        if (key == null) return null;

        JSONObject params = new JSONObject();
        params.put("action", "DiscountAdd");
        params.put("token", token);
        params.put("distype", discountType);
        params.put("verifyvalid", 1);
        params.put("validdate", validDate);
        params.put("plate", plateNum);
        params.put("parkid", parkId);

        switch (discountType) {
            case 0:
                params.put("disvalue", discountValue);
                params.put("disfrom", "减免" + discountValue + "元停车费");
                break;
            case 1:
                params.put("disvalue", (int) discountValue);
                params.put("disfrom", "减免" + ((int) discountValue) + "分钟停车费");
                break;
            case 2:
                params.put("disvalue", (int) discountValue);
                params.put("disfrom", "减免" + ((int) discountValue) + "%停车费");
                break;
            case 3:
                params.put("disfrom", "减免全部停车费");
                break;
        }

        StringBuilder body = SignUtil.coverJSONObject2String(params);
        String sign = SignUtil.signWithPrvateKey(body.toString(), key);
        body.append("&sign=").append(sign);

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(serviceUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(body.toString())
                .context(ctx)
                .send();

        JSONObject objBody = response.getBody().getJSONObject();
        JSONObject result = new JSONObject();

        result.put("code", objBody.getBooleanValue("success",true) ? "ok" : "500" );

        String desc = objBody.getString("message");
        if (desc!=null && desc.length()>0) result.put("desc", desc);

        JSONArray data = objBody.getJSONArray("data");
        if (data!=null && data.size()>0) {
            String discountId = data.getJSONObject(0).getString("discountid");
            if (discountId!=null && discountId.length()>0) {
                JSONObject resultData = new JSONObject();
                resultData.put("discountId", discountId);
                result.put("data", resultData);
            }
        }

        return result;
    }

    /**
     * Return only valid coupons and whether they are used: {"<id>": true}
     */
    public JSONObject queryValidCoupons(String plateNum, String parkId, String keyPath, Context ctx) throws Exception {
        String token = retrieveToken(parkId, keyPath, ctx);
        if (token == null) return null;

        PrivateKey key = _getPrivateKey(keyPath, ctx);
        if (key == null) return null;

        JSONObject params = new JSONObject();
        params.put("action", "DiscountQuery");
        params.put("token", token);
        params.put("plate", plateNum);
        params.put("parkid", parkId);

        StringBuilder body = SignUtil.coverJSONObject2String(params);
        String sign = SignUtil.signWithPrvateKey(body.toString(), key);
        body.append("&sign=").append(sign);

        HttpClient http = ctx.getEngine().createHttpClient();
        HttpResponse response = http.post(serviceUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(body.toString())
                .context(ctx)
                .send();

        JSONObject objBody = response.getBody().getJSONObject();

        if (objBody.getBoolean("success") && objBody.getJSONArray("data")!=null) {
            JSONArray data = objBody.getJSONArray("data");
            JSONObject result = new JSONObject();

            for (int i = 0; i < data.size(); i++) {
                JSONObject coupon = data.getJSONObject(i);
                if (coupon.getBooleanValue("Valid", false) &&
                    !coupon.getBooleanValue("Canceled", false) &&
                    coupon.getString("ValidDate")!=null &&
                    !_datePassed(coupon.getString("ValidDate"))) {
                    result.put(coupon.getString("ID"), coupon.getBooleanValue("Used", false));
                }
            }

            return result;
        }
        else
            return null;
    }

    public void cancelCoupon(String couponId, String parkId, String keyPath, Context ctx) throws Exception {
        String token = retrieveToken(parkId, keyPath, ctx);
        if (token == null) return;

        PrivateKey key = _getPrivateKey(keyPath, ctx);
        if (key == null) return;

        JSONObject params = new JSONObject();
        params.put("action", "DiscountCancel");
        params.put("token", token);
        params.put("discountid", couponId);
        params.put("parkid", parkId);

        StringBuilder body = SignUtil.coverJSONObject2String(params);
        String sign = SignUtil.signWithPrvateKey(body.toString(), key);
        body.append("&sign=").append(sign);

        HttpClient http = ctx.getEngine().createHttpClient();
        http.post(serviceUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(body.toString())
                .context(ctx)
                .send();
    }

    private boolean _datePassed(String d) throws Exception {
        if (d.indexOf(' ') < 0) d += " 24:00:00";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = df.parse(d);
        return date.before(new Date());
    }

    private PrivateKey _getPrivateKey(String keyPath, Context ctx) {
        PrivateKey key = null;

        try {
            key = keys.get(keyPath);
            if (key == null) {
                FileStorage fstorage = ctx.getEngine().getFileStorageForApplication(applicationId);
                key = SignUtil.convertBytesToKey( fstorage.readAllFromFile(keyPath, ctx) );
                keys.put(keyPath, key);
            }
        } catch (Exception ex) {
            ctx.logError(_position, "Failed to retrive private key", ex);
        }

        return key;
    }

}
