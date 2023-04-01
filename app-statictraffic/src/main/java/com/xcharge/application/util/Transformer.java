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
     *      ParkingDiscount.requestParkingDiscount.inputMessage
     *      extraInfo: EVChargingRecord's startTime, endTime
     *
     * Output message format:
     *      {
     *          "bizId": String, // 交易标识
     *          "merchId": String, // 停车场编号
     *          "plateNo": String, // 车牌号，例如：京A80001
     *          "startChargingTime": "yyyy-MM-dd HH:mm:ss", // 充电开始时间
     *          "stopChargingTime": "yyyy-MM-dd HH:mm:ss", // 充电结束时间
     *          "duration": Integer // 减免时长，单位为分钟
     *      }
     */
    public static JSONObject translateRequestMessage(JSONObject msg, String key) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        result.put("bizId", msg.getString("requestId"));
        result.put("merchId", msg.getString("parkId"));
        result.put("plateNo", msg.getString("carNo"));

        JSONObject extraInfo = msg.getJSONObject("extraInfo");
        if (extraInfo != null) {
            result.put("startChargingTime", extraInfo.getString("startTime"));
            result.put("stopChargingTime", extraInfo.getString("endTime"));
        }

        int discountType = msg.getIntValue("type", 1);
        if (discountType == 1) // 按时长优惠
            result.put("duration", msg.get("value"));
        else
            throw new Exception("只支持按时长优惠");

        result.put("sign", _signMessage(result, key));

        return result;
    }

    /**
     * Original message format:
     *      {
     *          "code": Integer, // 0 表示处理成功，其它值表示处理失败
     *          "msg": String // 处理失败时填写失败原因描述
     *      }
     *
     * Output message format:
     *      ParkingDiscount.requestParkingDiscount.outputMessage
     */
    public static JSONObject translateResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        int code = msg.getIntValue("code", -1);
        result.put("code", code==0 ? "ok" : ""+code);

        result.put("desc", msg.getString("msg"));

        return result;
    }

    private static String _signMessage(JSONObject msg, String signKey) {
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

            sb.append("&key=").append(DigestUtils.md5Hex(signKey));

            return DigestUtils.md5Hex(sb.toString()).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

}
