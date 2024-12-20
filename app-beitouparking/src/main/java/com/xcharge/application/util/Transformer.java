package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class Transformer {
    private final static String timeFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * 名称  数据类型  说明            选填类型
     * key   String  唯一标识商场         必填
     * sign  String  拼接字符串的md5加密  必填
     * plateNo String  车牌号 必填
     * expectExitTime long 预计车辆离场时间戳 可选
     * expectDeductionMinute Int 预期抵减分钟数 可选
     * <p>
     * 返回值
     * 名称 数据类型 说明
     * resCode Bit 响应代码（true 或 false）
     * resMsg String 响应消息（错误信息）
     * enterTime long 车辆进场时间戳
     * exitTime long 车辆出场时间戳
     * consumeUUID String 车场消费的唯一标识
     * totalConsumption Double 总计消费金额（单位:分）
     * paidAmounts Double 已缴金额（打折前）（单位:分）
     * reducedAmount Double 已减免金额（单位:分）
     * expectDeductionMinute Int 本次期望抵减小时数
     * expectDeductionAmount Double 本次期望抵减时间对应的金额 （单位:分
     */
    public static JSONObject transformerParkingInfoMessage(String key, String secret, JSONObject data) {
        LinkedHashMap<String, Object> msg = new LinkedHashMap<>();
        msg.put("key", key);
        msg.put("secret", secret);
        msg.put("plateNo", data.getString("carNo"));
        msg.put("expectExitTime", 0);
        msg.put("expectDeductionMinute", 0);
        msg.put("sign", _genSignWithMd5(msg, secret));
        msg.remove("secret");
        return new JSONObject(msg);
    }

    /**
     * 名称 数据类型 说明 选填类型
     * Key String 唯一标识商场 必填
     * sign String 拼接字符串的md5 加密 必填
     * consumeUUID String 车场消 费的唯一标识 必填
     * orderNo String 商场订单编号 必填
     * orderTime long 商场订单时间戳 必填
     * expectExitTime long 预计离场时间戳 可选
     * deductionMinute Int 抵减分钟数（单位分钟）必填
     * deductionAmount Double 抵减金额（单位:分）必填
     * thirdTradeNo String 支付宝、微信交易编号（0 元支付为空）可选
     * thirdAmount Double 支付宝、微信 支付金额 可选
     * thirdPayType int 支付方式(0：无 ；1：支付宝；2：银联；3：微信等) 可选
     * memberInfo Object 会员信息， 可空 可选
     * deductionDetailsArray抵扣明细(对象集合) 可选
     * <p>
     * 返回值 名称 数据类型 说明 resCode Bit 响应代码（true 或 false） resMsg String 响应消息（错误信息） orderNo String 商场订单编
     */
    public static JSONObject transformerPayFeeMessage(String key, String secret, String consumeUUID, JSONObject data) {
        LinkedHashMap<String, Object> msg = new LinkedHashMap<>();
        msg.put("key", key);
        msg.put("secret", secret);
        msg.put("consumeUUID", consumeUUID);
        msg.put("orderNo", data.getString("requestId"));
        msg.put("orderTime", System.currentTimeMillis() / 1000);
        msg.put("expectExitTime", 0);
        int discountType = data.getIntValue("type", 1);
        // 按时长优惠
        msg.put("deductionMinute", discountType == 1 ? data.getIntValue("value", 0) : 0);
        // 按金额优惠
        msg.put("deductionAmount", discountType == 0 ? data.getIntValue("value", 0) : 0);
        msg.put("thirdTradeNo", "");
        msg.put("thirdAmount", 0);
        msg.put("thirdPayType", 0);
        msg.put("sign", _genSignWithMd5(msg, secret));
        msg.remove("secret");
        return new JSONObject(msg);
    }

    public static JSONObject translateResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        String resCode = msg.getString("resCode");
        result.put("code", resCode != null && "true".equals(resCode) ? "ok" : "" + resCode);
        result.put("desc", msg);

        return result;
    }

    private static String _genSignWithMd5(LinkedHashMap<String, Object> msg, String secret) {
        try {
            List<String> keys = new ArrayList<>(msg.keySet());
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

            return DigestUtils.md5Hex(sb.toString()).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

}
