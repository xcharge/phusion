package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

public class Transformer {

    /**
     * Original message format:
     *      CarWashDiscount.requestCarWashDiscount.inputMessage
     *      extraInfo: washMode
     *
     * Output message format:
     *      {
     *          "CouponId": String, // 券唯一标识
     *          "EffectiveTime": String, // 券生效时间，例如：2021-08-27
     *          "VipTel": String, // 用户手机号
     *          "WashMode": String, // 洗车模式，见下
     *          "CouponType": Integer, // 券类型，见下
     *          "CouponPrice": Numeric // 券面额，单位为元
     *      }
     *
     *      洗车模式：
     *          washMode1: 快洗模式
     *          washMode2: 普洗模式
     *          washMode3: 精洗模式
     *
     *      券类型：
     *          1: 支付券
     *          2: 抵扣券
     */
    public static JSONObject translateRequestMessage(JSONObject msg, String key, String secret) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        result.put("CouponId", msg.getString("requestId"));
        result.put("VipTel", msg.getString("mobile"));

        String expireTime = msg.getString("expireTime");
        if (expireTime != null) result.put("EffectiveTime", expireTime.substring(0,10));

        int washMode = msg.getIntValue("washMode", -1);
        switch (washMode) {
            case 0:
                result.put("WashMode", "washMode1");
                break;
            case 1:
                result.put("WashMode", "washMode2");
                break;
            case 2:
                result.put("WashMode", "washMode3");
                break;
            default:
                throw new Exception("不支持的洗车模式");
        }

        int discountType = msg.getIntValue("type", 0);
        if (discountType == 0) { // 按金额优惠
            result.put("CouponType", 2);
            result.put("CouponPrice", msg.getIntValue("value", 0) / 100.0);
        }
        else
            throw new Exception("只支持按金额优惠");

        String sign = _signMessage(result, key);

        result.put("AppKey", key);
        result.put("Sign", sign);

        return result;
    }

    /**
     * Original message format:
     *      {
     *          "Code": Integer,
     *          "IsSuss": Boolean,
     *          "Msg": String
     *      }
     *
     * Output message format:
     *      CarWashDiscount.requestCarWashDiscount.outputMessage
     */
    public static JSONObject translateResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        boolean susses = msg.getBooleanValue("IsSuss", false);
        int code = msg.getIntValue("Code", -1);
        result.put("code", susses ? "ok" : ""+code);

        result.put("desc", msg.getString("Msg"));

        return result;
    }

    private static String _signMessage(JSONObject msg, String key) throws Exception {
        StringBuilder txt = new StringBuilder();
        txt.append(key)
                .append(msg.getString("CouponId"))
                .append(msg.getString("EffectiveTime"))
                .append(msg.getString("VipTel"))
                .append(msg.getString("WashMode"))
                .append(msg.getString("CouponType"))
                .append(msg.getFloat("CouponPrice"));

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(txt.toString().getBytes());
        byte[] digest = md.digest();

        return DatatypeConverter.printHexBinary(digest).toLowerCase();
    }

}
