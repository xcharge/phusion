package com.xcharge.application.util;

import cloud.phusion.Context;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.binary.Base64;

import java.security.Key;
import java.util.LinkedHashMap;

public class Transformer {

    public static String transformerDownloadThirdReductionServiceMessage(JSONObject msg, String key, String _position, Context ctx) {
        try {
            JSONObject request = new JSONObject();
            request.put("carCode", msg.getString("carNo"));
            JSONObject reduction = new JSONObject();
            reduction.put("reductionName", "车联网充电减免");
            reduction.put("reductionIdent", msg.getString("requestId"));
            reduction.put("action", 0);
            int discountType = msg.getIntValue("type", 1);
            if (discountType == 1) {
                // 按时长优惠
                reduction.put("reductionType", 2);
            } else if (discountType == 0) {
                // 按金额优惠
                reduction.put("reductionType", 4);
            } else {
                throw new Exception("不支持的优惠类型");
            }
            reduction.put("reductionValue", msg.getIntValue("value", 0));
            reduction.put("source", 1);
            request.put("reductionList", new JSONArray().fluentAdd(reduction));
            if (key == null || key.isEmpty()) {
                return request.toString();
            } else {
                return encrypt(request, key);
            }
        } catch (Exception e) {
            ctx.logError(_position, "LiFang transformerDownloadThirdReductionServiceMessage error: " + msg.toString(), e);
            return null;
        }
    }

    /**
     * 参数名 类型 必选 说明
     * resCode int Y 返回值，0 为成功，其他为失败
     * resMsg String N 返回说明
     */
    public static JSONObject translateResponseMessage(String msg, String key, String _position, Context ctx) {
        try {
            JSONObject result = new JSONObject();
            if (msg == null) return result;
            JSONObject data = new JSONObject();
            if (key == null || key.isEmpty()) {
                data = JSONObject.parseObject(msg);
            } else {
                String decryptStr = decrypt(msg, key);
                if (decryptStr == null) return result;
                data = JSONObject.parseObject(decryptStr);
            }
            Integer code = data.getInteger("resCode");
            result.put("code", code != null && code == 0 ? "ok" : "" + code);
            result.put("desc", data.getString("resMsg"));
            return result;
        } catch (Exception e) {
            ctx.logError(_position, "LiFang translateResponseMessage error: " + msg, e);
            return null;
        }
    }

    private static String encrypt(JSONObject obj, String key) throws Exception {
        Key k = AESUtils.toKey(Base64.decodeBase64(key));
        byte[] encryptData = AESUtils.encrypt(obj.toString().getBytes("utf-8"), k);
        String encryptStr = AESUtils.parseByte2HexStr(encryptData);
        return encryptStr;
    }

    private static String decrypt(String encryptStr, String key) throws Exception {
        Key k = AESUtils.toKey(Base64.decodeBase64(key));
        byte[] decryptData = AESUtils.decrypt(AESUtils.parseHexStr2Byte(encryptStr), k);
        return new String(decryptData, "utf-8");
    }

}
