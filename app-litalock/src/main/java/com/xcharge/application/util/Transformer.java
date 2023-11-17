package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Transformer {
    private final static Map<String, JSONObject> ACTION_MAP = new HashMap<String, JSONObject>() {{
        put("unlock", new JSONObject() {{
            put("uri", "operatorLock");
            put("type", 1);
        }}); //降锁
        put("lock", new JSONObject() {{
            put("uri", "operatorLock");
            put("type", 2);
        }}); //升锁
        put("queryStatus", new JSONObject() {{
            put("uri", "getLockInfo");
        }}); //查询地锁状态
    }};

    public static JSONObject getAction(JSONObject msg) {
        if (msg == null)
            return new JSONObject();
        JSONObject action = ACTION_MAP.getOrDefault(msg.getString("action"), null);
        if (action == null)
            return new JSONObject();
        return action;
    }

    /**
     * Original message format:
     * ParkingLock.performAction.inputMessage
     * <p>
     * Output message format:
     * {
     * "appId": String, // 应用id
     * "macno": String, // 地锁编号
     * "type": int, // 操作类型 1'开锁', 2'关锁', 3'保持开', 4'保持关', 5'恢复', 6'重启' ,7 开锁(预约) 8 关锁(预约) 必填
     * "sign": String, // 参数签名
     * <p>
     * }
     */
    public static String translateRequestMessage(JSONObject msg, String appId, String secret, String reqId) throws Exception {
        JSONObject result = new JSONObject();
        JSONObject action = ACTION_MAP.get(msg.getString("action"));
        result.put("appid", appId);
        if(!"queryStatus".equals(msg.getString("action"))) {
            result.put("macno", msg.getString("lockId"));
            result.put("type", action.getIntValue("type"));
        }else{
            result.put("macnos", msg.getString("lockId"));
        }
        result.put("sign", _genSignWithMd5(result, secret));
        return result.toJSONString();
    }

    /**
     * Original message format:
     * {
     * "code": Integer, // 0 表示处理成功，其它值表示处理失败
     * "msg": String // 处理失败时填写失败原因描述
     * }
     * <p>
     * Output message format:
     * ParkingLock.performAction.outputMessage
     */
    public static JSONObject translateResponseMessage(JSONObject msg, JSONObject action) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        int code;
        try {
            code = msg.getIntValue("code", -1);
        } catch (NumberFormatException e) {
            code = -1;
        }

        result.put("code", code == 0 ? "ok" : "" + code);
        result.put("desc", msg.getString("msg"));

        if (msg.containsKey("data") && msg.getJSONObject("data") != null
                && msg.getJSONObject("data").containsKey("isonline")
                && msg.getJSONObject("data").getIntValue("isonline") == 0) {
            result.put("code", -1);
            result.put("desc", "isonline false");
        }

        if (code == 0 && "getLockInfo".equals(action.getString("uri"))) {
            JSONObject info = translateStatus(msg.getJSONArray("data").getJSONObject(0));
            result.put("info", info);
        }
        return result;
    }

    /**
     * Original message format:
     * {
     * "notifytype": String, // 通知类型 1 心跳数据  2 常降锁确认订单 3 常立锁结束订单 只处理1，其他类型抛弃
     * "macno": String,//设备号，唯一标识
     * "type": String, // 锁类型 2：常降车位锁，1：常升车位锁
     * "battery": String, // 电磁电量百分比 0-100的数字
     * "lockStatus": String, // 锁状态 0：上电初始化;1：车位锁上升到位;2：车位锁下降到位;3：车位锁上升错误;4：车位锁下降错误;5：车位锁正在动作，还未到位;6：车位锁上升开关错误；7：车位锁下降开关错误 98 保存开 99 保持关
     * "hasCar": String, // hasCar
     * "errortext": String, // 错误信息（如果有错误信息将在此提示）
     * }
     * <p>
     * Output message format:
     * ParkingLock.statusNotification.outputMessage
     */
    public static JSONObject translateMessage(JSONObject msgObj) {
        JSONObject result = new JSONObject();
        result.put("joinId", msgObj.get("joinId"));
        result.putAll(translateStatus(msgObj));
        return result;
    }


    private static JSONObject translateStatus(JSONObject msg) {
        JSONObject result = new JSONObject();
        String lockStatus = null;
        Boolean lockReleased = null;
        String carParking = null;
        boolean online = true;
        String errorInfo = null;
        long beatTime = System.currentTimeMillis();
        long now = System.currentTimeMillis();
        String errortext = msg.getString("errortext");

        int lockState = msg.getIntValue("lock_status");
        int parkingState = msg.getIntValue("has_car");

        if (msg.containsKey("lockStatus")) {
            lockState = msg.getIntValue("lockStatus");
        }
        if (msg.containsKey("hasCar")) {
            parkingState = msg.getIntValue("hasCar");
        }

        if (msg.containsKey("beattime") && !msg.getString("beattime").isEmpty()) {
            beatTime = Objects.requireNonNull(time192Date(msg.getString("beattime"))).getTime();
        }
        if (now - beatTime > 10 * 60 * 1000) {
            online = false;
        }
        switch (lockState) {
            case 99:
            case 1:
                lockStatus = "normal";
                lockReleased = false;
                break;
            case 98:
            case 2:
                lockStatus = "normal";
                lockReleased = true;
                break;
            case 3:
                lockStatus = "error";
                errorInfo = "车位锁上升错误";
                break;
            case 4:
                lockStatus = "error";
                errorInfo = "车位锁下降错误";
                break;
            case 6:
                lockStatus = "error";
                errorInfo = "车位锁上升开关错误";
                break;
            case 7:
                lockStatus = "error";
                errorInfo = "车位锁下降开关错误";
                break;
            default:
                lockStatus = "unknown";
        }

        if (parkingState == 0) {
            carParking = "unknown";
        } else if (parkingState == 1) {
            carParking = "true";
        } else if (parkingState == 2) {
            carParking = "false";
        }
        if (errortext != null && !errortext.isEmpty()) {
            errorInfo = errortext;
        }
        result.put("lockId", msg.getString("macno"));
        result.put("lockStatus", lockStatus);
        result.put("carParking", carParking);
        result.put("online", online);
        result.put("errorInfo", errorInfo);
        result.put("lockReleased", lockReleased);
        result.put("batteryPower", msg.getFloatValue("battery"));
        return result;
    }

    /**
     * Original message format:
     * {
     * "code": String, // ok 表示处理成功，其它值表示处理失败
     * "desc": String // 处理失败时填写失败原因描述
     * }
     * <p>
     * Output message format:
     * {
     * "code": int, // 0 表示处理成功，其它值表示处理失败
     * "message": String // 处理失败时填写失败原因描述
     * }
     */
    public static JSONObject translatePCResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();
        String code = msg.getString("code");
        result.put("code", "ok".equals(code) ? 0 : 500);
        result.put("message", msg.getString("desc"));
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
                        sb.append(key).append(Arrays.toString((Object[]) obj));
                    } else {
                        String value = String.valueOf(obj);
                        sb.append(key).append(value);
                    }
                }
            }
            sb.append(secret);
            return DigestUtils.md5Hex(sb.toString()).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

    public static Date time192Date(String date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
