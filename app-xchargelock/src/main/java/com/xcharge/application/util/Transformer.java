package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Transformer {

    private static Map<String, String> actionMap = new HashMap<String, String>() {{
        put("ha", "unlock");
        put("hb", "lock");
        put("hk", "queryStatus");
    }};

    /**
     * Original message format:
     * {
     * "spaceId": String, // 地锁ID
     * "lockCode": String, // 动作：ha: unlock, hb: lock, hk: queryStatus
     * "sdkId": String, // 未使用
     * "token": String, // 未使用
     * }
     * output message format:
     * {
     * "lockId":String,
     * "action":String // unlock lock queryStatus
     * }
     */
    public static JSONObject translateAction(JSONObject msg) {
        JSONObject res = new JSONObject();
        res.put("joinId", msg.getString("joinId"));
        res.put("lockId", msg.getString("spaceId"));
        res.put("action", actionMap.get(msg.getString("lockCode")));
        return res;
    }

    /**
     * Original message format (info部分):
     * {
     * "lockId": String, // 地锁ID
     * "lockStatus": String, // 地锁工作状态 (normal, error, sleep, unknown)
     * "lockReleased": Boolean, // 升降锁状态 true (降锁), false (升起)
     * "carParking": String, // 停车位状态 true (有车), false (无车) unknown
     * "online":Boolean, // 是否在线 true (在线), false (离线)
     * "batteryPower":Int,// 电池电量 %
     * "errorInfo":String // 故障描述
     * }
     * output message format:
     * {
     * "errCode":Int, //错误码 0成功
     * "msg":String, //结果消息
     * "ResultData":{
     * "spaceId":String, // 地锁ID
     * "alarmStatus":Int, //报警状态
     * "lockStatus":Int, //地锁状态 0 下降  1升起 2故障 3carLocked 4休眠 5unknown 6离网+unknown
     * "battery":70,
     * "parkStatus":0
     * }
     * }
     */
    public static JSONObject translateResponseMessage(JSONObject msgObj) {
        JSONObject res = new JSONObject();
        res.put("errCode", "ok".equals(msgObj.getString("code")) ? 0 : 500);
        res.put("msg", msgObj.getString("desc"));
        if (msgObj.containsKey("info")) { // 没有info字段，说明是unlock/lock的回复；如果有，则是queryStatus的回复
            res.put("ResultData", translateStatus(msgObj.getJSONObject("info")));
        }
        return res;
    }

    public static JSONObject translateNotifyMessage(JSONObject msg) {
        JSONObject res = new JSONObject();
        res.put("errCode", 0);
        res.put("msg", "成功");
        res.put("ResultData", translateStatus(msg));
        return res;
    }

    public static JSONObject translateStatus(JSONObject msg) {
        JSONObject res = new JSONObject();
        int parkLockStatus = 0;
        int parkCarStatus = 1;
        int alarmStatus = 0;

        String strLockStatus = msg.getString("lockStatus");
        boolean lockReleased = msg.getBooleanValue("lockReleased");
        String strCarParking = msg.getString("carParking");

        switch (strLockStatus) {
            case "unknown":
                parkLockStatus = 6;
                break;
            case "error":
                parkLockStatus = 2;
                break;
            case "sleep":
                parkLockStatus = 4;
                break;
            case "normal":
                if (lockReleased) parkLockStatus = 0;
                else parkLockStatus = 1;
                break;
            default:
                if (!msg.getBooleanValue("online")) parkLockStatus = 5;
        }

        switch (strCarParking) {
            case "false":
                parkCarStatus = 0;
                break;
            case "true":
                parkCarStatus = 1;
                break;
            case "unknown":
                parkCarStatus = 2;
                break;
        }

        res.put("spaceId", msg.getString("lockId"));
        res.put("alarmStatus", alarmStatus);
        res.put("lockStatus", parkLockStatus);
        res.put("parkStatus", parkCarStatus);
        res.put("battery", msg.getIntValue("batteryPower"));
        return res;
    }
}
