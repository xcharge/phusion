package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author zhanghong
 */
public class Transformer {

    /**
     * Original message format:
     * ParkingDiscount.requestParkingDiscount.inputMessage
     * <p>
     * Output message format:
     * {
     * "parkCodes": String, // 停车场编号
     * "generateObj": String, // 赠送对象，1-赠送车牌 2-赠送手机号
     * "plateNo": String, // 车牌号码，赠送对象为 1 时必填
     * "couponType": String, // 优惠券类型：1 减免券；2 折扣券；3 全免券；4 减时券
     * "deductContent": String, // 优惠券类型为 1 时，单位为分；优惠券类型为 2，单位%；优惠券类型为 3，值为-1；优惠券类型为 4，单位分钟
     * "startTime": String, // 优惠券有效开始时间
     * "endTime": String, // 优惠券有效结束时间
     * "couponSource": String, // 优惠券来源 89001
     * }
     */
    public static JSONObject translateRequestMessage(JSONObject msg, String couponSource) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) {
            msg = new JSONObject();
        }

        result.put("parkCodes", msg.getString("parkId"));
        result.put("generateObj", 1);
        result.put("plateNo", msg.getString("carNo"));

        result.put("startTime", System.currentTimeMillis());
        result.put("endTime", System.currentTimeMillis() + 24 * 3600 * 1000);
        result.put("couponSource", couponSource);


        int discountType = msg.getIntValue("type", 1);
        if (discountType == 1) { // 按时长优惠
            result.put("couponType", 4);
            result.put("deductContent", msg.getIntValue("value", 0));
        } else if (discountType == 0) {
            // 按金额优惠
            result.put("couponType", 1);
            result.put("deductContent", msg.getIntValue("value", 0));
        } else {
            throw new Exception("不支持的优惠类型");
        }

        return result;
    }

    /**
     * Original message format:
     * {
     * "code": String // 200成功
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

        String code = msg.getString("code");
        result.put("code", "200".equals(code) ? "ok" : code);
        result.put("desc", msg.getString("msg"));

        return result;
    }

    public static String convertToQuery(JSONObject msg) {
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

            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
