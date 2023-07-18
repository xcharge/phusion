package com.xcharge.application.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class Transformer {
    private final static Map<String, JSONObject> ACTION_MAP = new HashMap<String, JSONObject>() {{
        put("unlock", new JSONObject() {{
            put("action", "lift");
            put("kind", 2);
        }}); //降锁
        put("lock", new JSONObject() {{
            put("action", "lift");
            put("kind", 1);
        }}); //升锁
        put("queryStatus", new JSONObject() {{
            put("action", "message");
        }}); //查询地锁状态
    }};

    /**
     * Original message format:
     * ParkingLock.performAction.inputMessage
     * <p>
     * Output message format:
     * {
     * "appId": String, // 应用id
     * "outDeviceNum": String, // 地锁编号
     * "timeStamp": String, // 时间戳
     * "nonceStr": String, // 随机字符串
     * "signType": String, // 签名类型默认 MD5
     * <p>
     * //以下参数只有升降锁操作才会存在
     * "outLiftNum": String, // 升降锁事务id（唯一字符串）
     * "liftKind": String, //
     * "ip": String, // ip地址
     * }
     */
    public static String translateRequestMessage(JSONObject msg, String appId, String secret, String reqId) throws Exception {
        JSONObject result = new JSONObject();
        JSONObject action = ACTION_MAP.get(msg.getString("action"));
        result.put("appId", appId);
        result.put("outDeviceNum", msg.getString("lockId"));
        result.put("timeStamp", getNow14());
        result.put("nonceStr", generateNonceStr(16));

        if ("lift".equals(action.getString("action"))) {
            result.put("outLiftNum", generateNonceStr(32));
            result.put("liftKind", action.getIntValue("kind"));
            result.put("ip", "39.107.66.2");
        }
        result.put("sign", _genSignWithMd5(result, secret));
        return convertToQuery(result);
    }

    /**
     * Original message format:
     * {
     * "resCode": Integer, // 0 表示处理成功，其它值表示处理失败
     * "resMsg": String // 处理失败时填写失败原因描述
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
            code = msg.getIntValue("errCode", -1);
        } catch (NumberFormatException e) {
            code = -1;
        }

        result.put("code", code == 0 ? "ok" : "" + code);
        result.put("desc", msg.getString("errMsg"));

        if (code == 0 && "message".equals(action.getString("action"))) {
            JSONObject info = translateStatus(msg);
            result.put("info", info);
        }
        return result;
    }

    /**
     * Original message format:
     * {
     * "appId": String, // 商户ID
     * "event": String,//通知事件
     * "lockState": String, // 地锁状态
     * "parkingState": String, // 车位状态
     * "carNumber": String, // 车牌号
     * "lockNum": String, // 地锁编号
     * "outDeviceNum": String, // 商户设备编号（枪编号），与系统地锁编号一一对应，需在调用前录入
     * "isOutLift": String, // 是否商户主动升降导致的信息通知 0-不是主动升降 1-是主动升降
     * "outLiftNum": String, // 商户升降锁编号，商户系统内唯一，当outLiftType=1时返回，升降锁编号与升降锁信息一一对应，可通过该编号获取升降信息
     * "liftTime": String, // 请求升降时间 格式：yyyyMMddHHmmss
     * "timeStamp": Numeric, // 请求发起时间戳 格式：yyyyMMddHHmmss
     * "nonceStr": Integer, // 随机字符串
     * "sign": Long, // 签名，详见签名生成算法
     * }
     * <p>
     * Output message format:
     * ParkingLock.statusNotification.outputMessage
     */
    public static JSONObject translateMessage(Map<String, String> msg) {
        JSONObject msgObj = new JSONObject(msg);
        JSONObject result = new JSONObject();
        result.put("joinId", msg.get("joinId"));
        result.putAll(translateStatus(msgObj));
        return result;
    }

    public static JSONObject getAction(JSONObject msg) {
        if (msg == null)
            return new JSONObject();
        JSONObject action = ACTION_MAP.getOrDefault(msg.getString("action"), null);
        if (action == null)
            return new JSONObject();
        return action;
    }

    private static JSONObject translateStatus(JSONObject msg) {
        JSONObject result = new JSONObject();
        String lockStatus = null;
        Boolean lockReleased = null;
        String carParking = null;
        Boolean online = null;
        Float batteryPower = 0f;
        String errorInfo = null;

        int lockState = msg.getIntValue("lockState");
        int parkingState = msg.getIntValue("parkingState");
        //已降下
        if (lockState == 2) {
            lockStatus = "normal";
            lockReleased = true;
        }
        //已升起
        else if (lockState == 1) {
            lockStatus = "normal";
            lockReleased = false;
        }
        //故障
        else if (lockState == 5) {
            lockStatus = "error";
        }

        //地锁摇臂锐角
//        else if (lockState == 3) {
//            lockStatus = "error";
//            errorInfo = "地锁摇臂锐角";
//        }
//        //地锁摇臂钝角
//        else if (lockState == 4) {
//            lockStatus = "error";
//            errorInfo = "地锁摇臂钝角";
//        }
//        //受控鸣叫
//        else if (lockState == 6) {
//            lockStatus = "error";
//            errorInfo = "受控鸣叫";
//        }

        //未知、离线
        else if (lockState == 0) {
            online = false;
            lockStatus = "unknown";
        }
        //休眠
        else if (lockState == 7) {
            lockStatus = "sleep";
        }

        if (parkingState == 0) {
            carParking = lockState == 2 ? "true" : "unknown";
        } else if (parkingState == 1) {
            carParking = "true";
        } else if (parkingState == 2) {
            carParking = "false";
        }

        batteryPower = msg.getFloatValue("electric");
        result.put("lockId", msg.getString("outDeviceNum"));
        result.put("lockStatus", lockStatus);
        result.put("carParking", carParking);
        result.put("online", online);
        result.put("errorInfo", errorInfo);
        result.put("lockReleased", lockReleased);
        result.put("batteryPower", batteryPower);
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
     * "errCode": int, // 0 表示处理成功，其它值表示处理失败
     * "errMsg": String // 处理失败时填写失败原因描述
     * }
     */
    public static JSONObject translatePCResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();
        String code = msg.getString("code");
        result.put("errCode", "ok".equals(code) ? 0 : 500);
        result.put("errMsg", msg.getString("desc"));
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

            sb.append("&key=").append(secret);

            return DigestUtils.md5Hex(sb.toString()).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

    private static String convertToQuery(JSONObject msg) {
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

    private static Object getNow14() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            return df.format(new Date());
        } catch (Exception e) {
        }
        return null;
    }

    private static String generateNonceStr(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return uuid.substring(0, Math.min(length, uuid.length()));
    }
}
