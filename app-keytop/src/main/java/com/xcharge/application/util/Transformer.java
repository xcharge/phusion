package com.xcharge.application.util;

import cloud.phusion.Context;
import cloud.phusion.protocol.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Transformer {
    private final static String timeFormat = "yyyy-MM-dd HH:mm:ss";
    private final static String SERVICE_CODE = "syncChargePilePay";

    /**
     * Original message format:
     *      ParkingDiscount.requestParkingDiscount.inputMessage
     *      extraInfo: EVChargingRecord's startTime, endTime
     *
     * Output message format:
     *      {
     *          "parkId": String, // 停车场编号
     *          "orderNo": String, // 交易订单号
     *          "plateNo": String, // 车牌号
     *          "stationId": String, // 充电站ID
     *          "stationName": String, // 充电站名称
     *          "deviceId": String, // 充电桩编号
     *          "deviceName": String, // 充电桩名称
     *          "spaceNo": String, // 车位号
     *          "startTime": Long, // 充电开始时间的时间戳
     *          "endTime": Long, // 充电结束时间的时间戳
     *          "power": Float, // 充电量
     *          "elecMoney": Integer, // 电费金额
     *          "seviceMoney": Integer, // 服务费金额
     *          "totalMoney": Integer, // 合计金额
     *          "freeType": Integer, // 0: 减免时长
     *          "freeTime": Integer // 减免时长，单位为秒
     *      }
     */
    public static JSONObject translateRequestMessage(JSONObject msg, String appId, String secret, String reqId) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        result.put("serviceCode", SERVICE_CODE);
        result.put("ts", System.currentTimeMillis());
        result.put("reqId", reqId);
        result.put("parkId", msg.getString("parkId"));
        result.put("orderNo", msg.getString("requestId"));
        result.put("plateNo", msg.getString("carNo"));

        JSONObject extraInfo = msg.getJSONObject("extraInfo");
        if (extraInfo != null) {
            result.put("startTime", _parseTimestamp(extraInfo.getString("startTime")));
            result.put("endTime", _parseTimestamp(extraInfo.getString("endTime")));
        }

        result.put("stationId", "678539775491710976");
        result.put("stationName", "XCHARGE");
        result.put("deviceId", "678539775491710976");
        result.put("deviceName", "XC01");
        result.put("spaceNo", "A101");
        result.put("power", 5.5);
        result.put("elecMoney", 100);
        result.put("seviceMoney", 50);
        result.put("totalMoney", 150);

        int discountType = msg.getIntValue("type", 1);
        if (discountType == 1) { // 按时长优惠
            result.put("freeType", 0);
            result.put("freeTime", msg.getIntValue("value", 0) * 60);
        }
        else
            throw new Exception("只支持按时长优惠");

        result.put("key", _genSignWithMd5(result, secret));
        result.put("appId", appId);

        return result;
    }

    private static long _parseTimestamp(String str) {
        if (str==null || str.length()==0) return 0;

        SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
        try {
            return formatter.parse(str).getTime();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Original message format:
     *      {
     *          "resCode": Integer, // 0 表示处理成功，其它值表示处理失败
     *          "resMsg": String // 处理失败时填写失败原因描述
     *      }
     *
     * Output message format:
     *      ParkingDiscount.requestParkingDiscount.outputMessage
     */
    public static JSONObject translateResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        int code = msg.getIntValue("resCode", -1);
        result.put("code", code==0 ? "ok" : ""+code);

        result.put("desc", msg.getString("resMsg"));

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

            sb.append("&").append(secret);

            return DigestUtils.md5Hex(sb.toString()).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

}
