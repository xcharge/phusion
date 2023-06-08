package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Transformer {

    /**
     * Original message format:
     * ParkingDiscount.requestParkingDiscount.inputMessage
     * <p>
     * Output message format:
     * {
     * "parkID": String, // 停车场编号
     * "StationID": String, // 停车场编号
     * "OrderNo": String, // 唯一编号-交易订单号
     * "PlateNum": String, // 车牌号
     * "PlateColor": String, // 车牌颜色
     *  以下内容会由慧停车给每个运营商配置好规则，推送内容不起效
     * "CalculationMethod": Integer, // 1: 减免时长 2: 减免费用
     * "FreeParkingTimes": Float // 减免时长，单位为分钟 非必填
     * "FreeParkingCost": Float, // 减免费用，单位元  非必填
     * }
     */
    public static JSONObject translateRequestMessage(JSONObject msg) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) {
            msg = new JSONObject();
        }

        result.put("ParkID", msg.getString("parkId"));
        result.put("StationID", msg.getString("parkId"));
        result.put("OrderNo", msg.getString("requestId"));
        result.put("PlateNum", msg.getString("carNo"));

        result.put("PlateColor", "01");

        int discountType = msg.getIntValue("type", 1);
        if (discountType == 1) { // 按时长优惠
            result.put("CalculationMethod", 1);
            result.put("FreeParkingTimes", msg.getIntValue("value", 0) * 1.0);
        } else if (discountType == 0) {
            // 按金额优惠
            result.put("CalculationMethod", 2);
            result.put("FreeParkingCost", msg.getIntValue("value", 0) * 1.0 / 100);
        } else {
            throw new Exception("不支持的优惠类型");
        }

        return result;
    }

    /**
     * Original message format:
     * {
     * "ConfirmResult": Integer // 0 表示处理成功，1 表示处理失败
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

        int ret = msg.getIntValue("Ret");
        JSONObject data = (JSONObject) msg.getOrDefault("Data", new JSONObject());
        int code = data.getIntValue("ConfirmResult", 1);
        result.put("code", ret != 0 ? String.valueOf(ret) : (code == 0 ? "ok" : String.valueOf(code)));
        result.put("desc", msg.getString("Msg"));

        return result;
    }
}
