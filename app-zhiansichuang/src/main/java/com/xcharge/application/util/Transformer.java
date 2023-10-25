package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;

public class Transformer {
    /**
     * Original message format:
     * ParkingDiscount.requestParkingDiscount.inputMessage
     * extraInfo: EVChargingRecord's startTime, endTime
     * <p>
     * Output message format:
     * {
     * "start_charge_seq":String,//充电订单号
     * "car_number": String, // 车牌号
     * "comid": String, // 停车场id，需要将充电平台方场站id发给道闸做关联，道闸提供关联后的停车场id
     * "union_id": String, // 厂商编号
     * "mitigate_type": Number, // 减免类型0:全免 ,1:时长减免,  2：金额减免,  3：停车场决定
     * "duration": Long, // 减免时长（优惠小时数），单位：分钟，整数
     * "free_money": Float, // 减免金额，单位：元，小数点后 2 位
     * }
     */
    public static String translateRequestMessage(JSONObject msg, String unionId, String secret, String reqId) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("start_charge_seq", msg.getString("requestId"));
        data.put("comid", msg.getString("parkId"));
        data.put("car_number", msg.getString("carNo"));
        data.put("union_id", unionId);
        data.put("mitigate_type", msg.getIntValue("type"));
        if (msg.getIntValue("type") == 1) data.put("duration", msg.getIntValue("value"));
        else if (msg.getIntValue("type") == 2) data.put("free_money", msg.getFloatValue("value"));
        result.put("data", data);
        result.put("sign", _genSignWithMd5(data, secret));
        return Base64.getEncoder().encodeToString(result.toJSONString().getBytes());
    }

    /**
     * Original message format:
     * {
     * "resultCode": String, // 0 表示处理成功，其它值表示处理失败
     * "message": String // 处理失败时填写失败原因描述
     * }
     * <p>
     * Output message format:
     * ParkingDiscount.requestParkingDiscount.outputMessage
     */
    public static JSONObject translateResponseMessage(String msg) {
        try {
            JSONObject msgObj = JSONObject.parseObject(new String(Base64.getDecoder().decode(msg)));
            JSONObject result = new JSONObject();
            JSONObject responRes = new JSONObject();
            if (msgObj != null && msgObj.containsKey("data")) responRes = msgObj.getJSONObject("data");

            Integer code = responRes.getIntValue("state");
            result.put("code", code == 1 ? "ok" : "" + code);
            result.put("desc", responRes.getString("message"));
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private static String _genSignWithMd5(JSONObject msg, String secret) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(msg.toJSONString());
            sb.append("key=").append(secret);
            return DigestUtils.md5Hex(sb.toString()).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }
}
