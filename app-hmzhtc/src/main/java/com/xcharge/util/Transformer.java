package com.xcharge.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Transformer {

    /**
     * Original message format:
     *      ParkingDiscount.requestParkingDiscount.inputMessage
     *      extraInfo: EVChargingRecord's startTime, endTime
     *
     * Output message format:
     *      {
     *          "parkId": String, // 停车场 ID（道闸停车方提供）
     *          "orderNo": String, // 交易订单号
     *          "plateNo": String, // 车牌号
     *          "parkName": String, // 停车场名称（道闸停车方提供）
     *          "startTime": String, // 充电开始时间 yyyy-MM-dd HH:mm:ss
     *          "endTime": String, // 充电结束时间yyyy-MM-dd HH:mm:ss
     *          "appId": String, // APPID（道闸停车方提供）
     *          "totalMoney": Integer, // 合计金额
     *      }
     */
    public static JSONObject translateRequestMessage(JSONObject msg, String appId, String secret) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();
        result.put("appId", appId);
        result.put("parkName", msg.getString("parkName"));
        result.put("parkId", msg.getString("parkId"));
        result.put("orderNo", msg.getString("requestId"));
        result.put("plateNo", msg.getString("carNo"));

        JSONObject extraInfo = msg.getJSONObject("extraInfo");
        if (extraInfo != null) {
            result.put("startTime",extraInfo.getString("startTime"));
            result.put("endTime", extraInfo.getString("endTime"));
        }
        result.put("totalMoney", 150);
        result.put("sign", _genSignWithMd5(result, secret));
        return result;
    }

    /**
     * Original message format:
     *      {
     *          "result": Integer, // 0 表示处理成功，其它值表示处理失败
     *          "description": String // 处理失败时填写失败原因描述
     *      }
     *
     * Output message format:
     *      ParkingDiscount.requestParkingDiscount.outputMessage
     */
    public static JSONObject translateResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        int code = msg.getIntValue("result", -1);
        result.put("code", code==0 ? "ok" : ""+code);

        result.put("desc", msg.getString("description"));

        return result;
    }

    private static String _genSignWithMd5(JSONObject msg, String secret) {
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

            sb.append("&key=").append(secret);

            return DigestUtils.md5Hex(sb.toString()).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }
}
