package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;

import java.text.SimpleDateFormat;

public class Transformer {
    private final static String timeFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * Original message format:
     *      {
     *          "tenantId": String, // 商户ID
     *          "tradeNo": String, // 充电订单号
     *          "outsideTradeNo": String, // 第三方订单号
     *          "parkId": String, // 停车场ID
     *          "deviceId": String, // 充电桩编号
     *          "deviceType": String, // fastCharger/slowCharger
     *          "carNo": String, // 车牌号
     *          "mobile": String, // 用户手机号
     *          "openId": String, // 微信ID
     *          "powerCharged": Numeric, // 充电电量，单位为度
     *          "duration": Integer, // 充电时长，单位为分钟
     *          "startTime": Long, // 充电开始时刻的时间戳（北京时间 1970-01-01 00:00:00 开始至今的13位毫秒数）
     *          "endTime": Long // 充电完成（拔枪生成账单）时刻的时间戳
     *      }
     *
     * Output message format:
     *      EVChargingNotification.chargingNotification.outputMessage
     */
    public static JSONObject translateMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        result.put("stationOwner", msg.getString("tenantId"));
        result.put("stationId", msg.getString("parkId"));
        result.put("chargingId", msg.getString("tradeNo"));
        result.put("chargingAuxId", msg.getString("outsideTradeNo"));
        result.put("deviceId", msg.getString("deviceId"));
        result.put("deviceType", msg.getString("deviceType"));
        result.put("carNo", msg.getString("carNo"));
        result.put("mobile", msg.getString("mobile"));
        result.put("userId", msg.getString("openId"));
        result.put("powerCharged", msg.getFloat("powerCharged"));
        result.put("duration", msg.getInteger("duration"));

        if (msg.containsKey("openId")) result.put("userType", "wechat");

        if (msg.containsKey("startTime")) {
            long startTime = msg.getLong("startTime");
            result.put("startTime", _formatTime(startTime));
        }

        if (msg.containsKey("endTime")) {
            long endTime = msg.getLong("endTime");
            result.put("endTime", _formatTime(endTime));
        }

        return result;
    }

    private static String _formatTime(long time) {
        SimpleDateFormat f = new SimpleDateFormat(timeFormat);
        return f.format(time);
    }

}
