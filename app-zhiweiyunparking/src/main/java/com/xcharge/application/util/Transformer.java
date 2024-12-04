package com.xcharge.application.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Transformer {

    /**
     * app_id   分配给开发者的应用ID
     * method   接口名称
     * charset  请求使用的编码格式
     * sign_type    签名算法类型
     * sign 签名串
     * timestamp    发送请求的时间
     * version  调用的接口版本
     * outDiscountNo   优惠编号
     * parkCode 停车场编号
     * orderNum 订单号
     * plateNum 车牌号
     * discountType 优惠类型; 1：全免,2：减免时间,3：减免金额,4：折扣
     * discountNumber   优惠数额，全免优惠时，此字段填-1；时间优惠时以分钟为单位 值为整数，金额优惠时以元为单位；折扣优惠时取值范围大于0且小于10，比如打99折时，值为9.9。
     * discountTime 发放时间
     */
    public static JSONObject translateRequestMessage(JSONObject msg, String appId, String appAuthToken) throws Exception {
        JSONObject request = new JSONObject();
        request.put("method", "p2c.notify.discount");
        request.put("format", "json");
        request.put("charset", "UTF-8");
        request.put("sign_type", "md5");
        request.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        request.put("version", "1.0");

        //配置信息
        request.put("app_id", appId);
        request.put("app_auth_token", appAuthToken);
        request.put("outDiscountNo", msg.getString("requestId"));
        request.put("parkCode", msg.getString("parkId"));
        //订单信息
        request.put("discountType", 2);
        request.put("discountNumber", "135");
        request.put("plateNum", msg.getString("carNo"));
        request.put("discountTime", System.currentTimeMillis() / 1000);

        String signContent = Security.getSignContent(request);
        String sign = Security.getSign(signContent, request.getString("app_auth_token"), request.getString("charset"), request.getString("sign_type"));
        request.put("sign", sign);
        return request;
    }


    /**
     * {
     * "p2c_notify_discount_response": {
     * "code": "200",
     * "data": {}
     * }
     * }
     * {
     * "error_response": {
     * "code": "20000",
     * "msg": "Service Currently Unavailable",
     * "sub_code": "isp.unknown-error",
     * "sub_msg": "系统繁忙"
     * },
     * "sign": "ERITJKEIJKJHKKKKKKKHJEREEEEEEEEEEE"
     * }
     */
    public static JSONObject translateResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();
        if (msg.containsKey("p2c_notify_discount_response")) {
            result.put("code", "ok");
            result.put("desc", msg.getString("p2c_notify_discount_response"));
        }
        if (msg.containsKey("error_response")) {
            JSONObject suc = msg.getJSONObject("error_response");
            Integer code = suc.getInteger("code");
            result.put("code", code != null && code == 200 ? "ok" : "" + code);
            result.put("desc", suc.getString("sub_msg"));
        }
        return result;
    }

}
