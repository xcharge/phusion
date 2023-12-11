package com.xcharge.application.utils;

import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;
import java.util.*;

public class Transformer {
    private final static String API_VERSION = "v1.0.0";

    public static JSONObject translateQueryRequestMessage(JSONObject msg, String mchId, String appId, String salt, String key) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();
        result.put("carLicense", msg.getString("carNo"));
        JSONObject sign = SecurityUtils.encryptAndSignAndURLEncoder(result, salt, key);
        result.put("appId", appId);
        result.put("mchId", mchId);
        result.put("parkCode", msg.getString("parkId"));
        result.put("version", API_VERSION);
        result.put("sign", sign.getString("sign"));
        result.put("cipherJson", sign.getString("cipherJson"));
        return result;
    }

    /**
     * Original message format:
     * ParkingDiscount.requestParkingDiscount.inputMessage
     * <p>
     * Output message format:
     * {
     * "carLicense": String, // 车牌号
     * "parkingNo": String, // 停车编码
     * "couponType": Integer, // 券类型。见下
     * "number": Float, // 额度
     * }
     * <p>
     * 优惠券类型（couponType）:
     * 0: 金额。此时须填写 float
     * 1: 时长。此时须填写 int
     * 2: 全免
     * <p>
     */
    public static JSONObject translateDiscountRequestMessage(JSONObject msg, JSONObject queryResult, String mchId, String appId, String salt, String key) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();
        result.put("carLicense", queryResult.getString("carLicense"));
        int discountType = typeMap().get(msg.getString("type"));
        result.put("couponType", discountType);
        if (discountType == 1) {
            BigDecimal dividend = new BigDecimal(msg.getFloatValue("value"));
            BigDecimal divisor = new BigDecimal(100);
            result.put("number", dividend.divide(divisor, 2, BigDecimal.ROUND_HALF_UP));//减免金额由分转为元
        } else {
            result.put("number", msg.getIntValue("value") / 60); //减免时长由分钟转为小时
        }
        result.put("parkingNo", queryResult.getString("parkingNo"));
        JSONObject sign = SecurityUtils.encryptAndSignAndURLEncoder(_genSignWithMd5(result), salt, key);
        result.put("appId", appId);
        result.put("mchId", mchId);
        result.put("parkCode", msg.getString("parkId"));
        result.put("version", API_VERSION);
        result.put("sign", sign.getString("sign"));
        result.put("cipherJson", sign.getString("cipherJson"));
        return result;
    }

    /**
     * Original message format:
     * {
     * "errCode": String, // "000000" 请求成功
     * "errCodeDes": String, // "请求成功" 或错误信息
     * "state": Integer, // 0 业务处理成功，其它值表示失败
     * "message": String // 处理失败时填写失败原因描述
     * "serialCode": String // 与交易单号相对应的优惠ID
     * }
     * <p>
     * Output message format:
     * ParkingDiscount.requestParkingDiscount.outputMessage
     * data: carLicense, parkingNo
     */
    public static JSONObject translateResponseMessage(JSONObject msg, String key) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        String code = "000000".equals(msg.getString("errCode")) ? "ok" : msg.getString("errCode");
        String cipherJson = msg.getString("cipherJson");
        String desc = msg.getString("errCodeDes");
        if (cipherJson != null && cipherJson.length() > 0) {
            String decode = SecurityUtils.decryptAndVerifyAndURLDecoder(cipherJson, key);
            System.out.println("----------------------------------");
            System.out.println("cipherJson:" + decode);
            System.out.println("----------------------------------");
            JSONObject cipherObj = JSONObject.parseObject(decode);
            if (cipherObj.containsKey("state") && cipherObj.getIntValue("state") != 0) {
                code = "-1";
                desc = cipherObj.getString("message");
            } else {
                result.putAll(cipherObj);
            }
        }
        result.put("code", code);
        result.put("desc", desc);
        return result;
    }

    private static Map<String, Integer> typeMap() {
        Map<String, Integer> typeMap = new HashMap<>();
        typeMap.put("0", 1);
        typeMap.put("1", 0);
        typeMap.put("9", 2);
        return typeMap;
    }

    private static JSONObject _genSignWithMd5(JSONObject msg) {
        try {
            List<String> keys = new ArrayList<>(msg.keySet());
            Collections.sort(keys);
            int size = keys.size();
            JSONObject result = new JSONObject();
            for (int i = 0; i < size; ++i) {
                String key = keys.get(i);
                Object obj = msg.get(key);
                if (obj != null) {
                    String value = String.valueOf(obj);
                    result.put(key, value);
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
