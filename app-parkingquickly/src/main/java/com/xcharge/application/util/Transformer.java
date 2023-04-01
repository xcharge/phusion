package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

public class Transformer {
    private final static String COUPON_COMMAND = "chargediscount";

    /**
     * Original message format:
     *      ParkingDiscount.requestParkingDiscount.inputMessage
     *      extraInfo: EVChargingRecord's startTime, endTime
     *
     * Output message format:
     *      {
     *          "msgid": String, // 请求消息唯一标识
     *          "pid": String, // 停车场编号
     *          "plate": String, // 车牌号，例如：京A80001
     *          "dismode": String, // 优惠模式，见下
     *          "disvalue": Integer, // 见下
     *          "maxdisamount": Integer // 最大优惠限额，单位为分，默认为 0
     *      }
     *
     *      优惠模式：
     *          duration: 按时长优惠，此时 disvalue 的单位是分钟
     *          ratio: 折扣比例，此时 disvalue 的单位是 %，例如 90 表示9折，0 表示全免
     *          cash: 减免金额，此时 disvalue 的单位是分
     */
    public static JSONObject translateRequestMessage(JSONObject msg) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        result.put("msgid", msg.getString("requestId"));
        result.put("pid", msg.getString("parkId"));
        result.put("plate", msg.getString("carNo"));
        result.put("maxdisamount", 0);

        int discountType = msg.getIntValue("type", 1);
        switch (discountType) {
            case 0:
                result.put("dismode", "cash");
                result.put("disvalue", msg.getIntValue("value",0));
                break;
            case 1:
                result.put("dismode", "duration");
                result.put("disvalue", msg.getIntValue("value",0));
                break;
            case 2:
                result.put("dismode", "ratio");
                result.put("disvalue", msg.getIntValue("value",0));
                break;
            case 9:
                result.put("dismode", "ratio");
                result.put("disvalue", 0);
                break;
            default:
                throw new Exception("不支持该优惠类型");
        }

        return result;
    }

    public static String getURLFromMessage(JSONObject msg, String channel, String secret) throws Exception {
        String sign = _signMessage(msg, channel, secret);

        StringBuilder result = new StringBuilder();

        result.append("/")
                .append(channel)
                .append("?channelid=")
                .append(channel)
                .append("&msg=")
                .append(COUPON_COMMAND)
                .append("&msgid=")
                .append(msg.getString("msgid"))
                .append("&sign=")
                .append(sign);

        return result.toString();
    }

    /**
     * Original message format:
     *      {
     *          "result": Integer, // 0 表示处理成功，其它值表示处理失败
     *          "reason": String // 处理失败时填写失败原因描述
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

        result.put("desc", msg.getString("reason"));

        return result;
    }

    private static String _signMessage(JSONObject msg, String channel, String secret) throws Exception {
        StringBuilder txt = new StringBuilder();
        txt.append("channelid=")
                .append(channel)
                .append("&msg=")
                .append(COUPON_COMMAND)
                .append("&msgid=")
                .append(msg.getString("msgid"))
                .append("&secret=")
                .append(secret)
                .append("&data=")
                .append(msg.toJSONString());

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(txt.toString().getBytes());
        byte[] digest = md.digest();

        return DatatypeConverter.printHexBinary(digest).toLowerCase();
    }

}
