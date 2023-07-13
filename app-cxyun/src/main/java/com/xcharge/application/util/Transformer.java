package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Transformer {

    /**
     * Original message format:
     * ParkingDiscount.requestParkingDiscount.inputMessage
     * <p>
     * Output message format:
     * {
     * "parkingId": String, // 车场编号
     * "plateNumber": String, // 车牌号
     * "favourableDuration": int, // 优惠时长
     * "timestamp": String, // 时间戳
     * "appKey": String, // appKey(MD5加密32位大写)
     * "sign": String, // sign
     * }
     */
    public static JSONObject translateRequestMessage(JSONObject msg, String appKey) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) {
            msg = new JSONObject();
        }

        result.put("timestamp", System.currentTimeMillis());
        result.put("parkingId", msg.getString("parkId"));
        result.put("plateNumber", msg.getString("carNo"));

        int discountType = msg.getIntValue("type", 1);
        if (discountType == 1) { // 按时长优惠
            result.put("favourableDuration", msg.getIntValue("value", 0));
        } else {
            throw new Exception("只支持按时长优惠");
        }

        String appKeyMd5 = DigestUtils.md5Hex(appKey).toUpperCase();
        result.put("sign", genSignWithMd5(result, appKeyMd5));
        result.put("appKey", appKeyMd5);

        return result;
    }

    /**
     * Original message format:
     * {
     * "status": Integer, // 1 表示处理成功，其它值表示处理失败
     * "message": String // 处理失败时填写失败原因描述
     * }
     * <p>
     * Output message format:
     * ParkingDiscount.requestParkingDiscount.outputMessage
     */
    public static JSONObject translateResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) {
            msg = new JSONObject();
        }

        int code = msg.getIntValue("status", -1);
        result.put("code", code == 1 ? "ok" : code);

        result.put("desc", msg.getString("message"));

        return result;
    }

    private static String genSignWithMd5(JSONObject msg, String secret) {
        try {
            List<String> keys = new ArrayList<>(msg.keySet());
            Collections.sort(keys);
            int size = keys.size();

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < size; ++i) {
                String key = keys.get(i);
                Object obj = msg.get(key);
                if (obj != null) {
                    if (obj instanceof Object[]) {
                        sb.append(key).append("=").append(Arrays.toString((Object[]) obj));
                    } else {
                        String value = String.valueOf(obj);
                        sb.append(key).append("=").append(value);
                    }
                    if (i != size - 1) {
                        sb.append("&");
                    }
                }
            }

            sb.append(secret);

            return DigestUtils.md5Hex(sb.toString()).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

}
