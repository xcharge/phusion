package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;

import java.text.SimpleDateFormat;

/**
 * @author zhanghong
 */
public class Transformer {
    private final static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Original message format:
     * ParkingDiscount.requestParkingDiscount.inputMessage
     * extraInfo: EVChargingRecord's startTime endTime
     * <p>
     * Output message format:
     * {
     * "StartChargeSeq": String, // 订单编号
     * "StationID": String, // 电站编号
     * "StationName": String, // 电站名称
     * "ParkID": String, // 停车场编号
     * "ConnectorID": String, // 充电设备接口编码
     * "ConnectorName": String, // 充电设备接口名称
     * "PlateNum": String, // 车牌号
     * "StartTime": String, // 开始充电时间 格式“yyyy-MM-dd HH:mm:ss”
     * "CalculationMethod": Integer, // 1: 减免时长 2: 减免费用 3: 停车场决定
     * "FreeParkingTimes": Float // 减免时长，单位为分钟 非必填
     * "FreeParkingCost": Float, // 减免费用，单位元  非必填
     * }
     */
    public static JSONObject translateRequestMessage(JSONObject msg) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) {
            msg = new JSONObject();
        }

        result.put("StartChargeSeq", msg.getString("requestId"));
        result.put("ParkID", msg.getString("parkId"));
        result.put("PlateNum", msg.getString("carNo"));

        result.put("StationID", "678539775491710976");
        result.put("StationName", "XCHARGE");
        result.put("ConnectorID", "678539775491710976");
        result.put("ConnectorName", "XC01");

        JSONObject extraInfo = msg.getJSONObject("extraInfo");
        if (extraInfo != null) {
            result.put("StartTime", extraInfo.getString("startTime"));
            result.put("EndTime", extraInfo.getString("endTime"));
        }

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
        result.put("desc", ret != 0 ? msg.getString("Msg") : data.getString("PlateAutFailReason"));

        return result;
    }
}
