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
     * extraInfo: EVChargingRecord's startTime, endTime
     * <p>
     * Output message format:
     * {
     * "carNo": String, // 车牌号
     * "stationId": String, // 停车场id，需要将充电平台方场站id发给道闸做关联，道闸提供关联后的停车场id
     * "stationName": String, // 充电站名称
     * "connectorId": String, // 充电桩编号
     * "connectorName": String, // 充电桩名称
     * "startTime": String, // 充电开始时间yyyy-MM-dd HH:mm:ss
     * "endTime": String, // 充电结束时间yyyy-MM-dd HH:mm:ss
     * "electricQuantity": Float, // 充电量
     * "electricFee": Integer, // 电费金额
     * "serviceFee": Integer, // 服务费金额
     * "totalFee": Integer, // 合计金额
     * }
     */
    public static JSONObject translateRequestMessage(JSONObject msg, String appId, String secret, String reqId) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();
        result.put("appId", appId);
        result.put("signType", "MD5");
        JSONObject data = new JSONObject();
        data.put("chargeOrderNo", msg.getString("requestId"));
        data.put("stationId", msg.getString("parkId"));
        data.put("stationName", "XCHARGE");
        data.put("connectorId", "678539775491710976");
        data.put("connectorName", "XC01");
        String carStr = msg.getString("carNo");
        String prefix = carStr.substring(0, 1);
        String suffix = carStr.substring(1);
        data.put("carNo", prefix + "-" + suffix);
        JSONObject extraInfo = msg.getJSONObject("extraInfo");
        data.put("startTime", extraInfo.getString("startTime"));
        data.put("endTime", extraInfo.getString("endTime"));
        data.put("electricQuantity", 5.5);
        data.put("electricFee", 100);
        data.put("serviceFee", 50);
        data.put("totalFee", 150);
        result.put("data", data);
        result.put("sign", _genSignWithMd5(data, secret));
        return result;
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
    public static JSONObject translateResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        String code = msg.getString("resultCode");
        result.put("code", "0".equals(code) ? "ok" : "" + code);

        result.put("desc", msg.getString("message"));

        return result;
    }

    private static String _genSignWithMd5(JSONObject msg, String secret) {
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

            sb.append("&").append(secret);

            return DigestUtils.md5Hex(sb.toString()).toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }
}
