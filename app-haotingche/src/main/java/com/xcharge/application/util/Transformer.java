package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;
import cloud.phusion.Context;

public class Transformer {

    public static JSONObject translateRequestMessage(JSONObject msg) throws Exception {
        JSONObject data = new JSONObject();
        data.put("ParkID", msg.getString("parkId"));
        data.put("PlateNum", msg.getString("carNo"));
        data.put("StartChargeSeq", msg.getString("requestId"));

        JSONObject extraInfo = msg.getJSONObject("extraInfo");
        if (extraInfo != null) {
            String startTime = extraInfo.getString("startTime");
            String endTime = extraInfo.getString("endTime");
            data.put("StartTime", startTime);
            data.put("EndTime", endTime);
        }
        int type = msg.getIntValue("type", 1);
        int value = msg.getIntValue("value", 0);

        if (type == 1) {
            data.put("FreeParkingTimes", value);
        } else {
            throw new Exception("好停车只支持时长减免(type=1)");
        }
        data.put("Power", 3.3);

        return data;
    }

    public static JSONObject translateResponseMessage(JSONObject objBody) {
        JSONObject result = new JSONObject();

        int ret = objBody.getIntValue("Ret", -1);
        String msg = objBody.getString("Msg");

        if (ret == 0) {
            result.put("code", "ok");
            result.put("desc", msg);
        } else {
            result.put("code", "" + ret);
            result.put("desc", msg);
        }

        return result;
    }

}
