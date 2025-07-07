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
     * "pid": "123456", 平台统一分配的pid
     * "serviceName": "sendPlateTicket", 固定为 sendPlateTicket
     * "sign": "3451D248F11F8DC8E880906F003C9509", 请求参数的签名串
     * "timestamp": "1536547465", 发送请求的时间（Unix时间戳）
     * "bizContent": {
     * "parkCode": "TEST/1P1", 停车场编号，由平台统一分配
     * "plateNum": "津L62538", 车牌号
     * 优惠类型:
     * 1-减金额 (元)
     * 2-减时长(分钟)
     * 3-百分比(%)
     * 4-全免券(0)
     * 5-全额免(0))
     * "ticketType": 1,
     * 优惠值，示例:80,说明
     * ticketType=1,优惠80元
     * ticketType=2,优惠80分钟
     * ticketType=3,打八折
     * ticketType=4,全免券
     * ticketType=5,全额免
     * "ticketValue": 10
     * }
     * }
     */
    public static JSONObject translateRequestMessage(JSONObject msg, String pid, String serviceName, String key) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();
        JSONObject bizContent = new JSONObject();
        bizContent.put("parkCode", msg.getString("parkId"));
        bizContent.put("plateNum", msg.getString("carNo"));
        String type = msg.getString("type");
        switch (type) {
            case "0":
                bizContent.put("ticketType", 1);
                bizContent.put("ticketValue", msg.getIntValue("value", 0) * 1.0 / 100);
                break;
            case "1":
                bizContent.put("ticketType", 2);
                bizContent.put("ticketValue", msg.get("value"));
                break;
            case "2":
                bizContent.put("ticketType", 3);
                bizContent.put("ticketValue", msg.get("value"));
                break;
            case "9":
                bizContent.put("ticketType", 5);
                break;
        }
        result.put("pid", pid);
        result.put("serviceName", serviceName);
        result.put("timestamp", System.currentTimeMillis());
        result.put("bizContent", bizContent);
        result.put("sign", _genSignWithMd5(result, key));
        return result;
    }

    /**
     * Original message format:
     * 返回代码	信息描述	建议
     * 200	成功	无
     * 400	缺失参数	检查必填参数
     * 401	认证失败	检查签名方式
     * 402	非法参数	检查参数格式或不在数据定义范围
     * 404	请求资源不存在	检查关键参数是否有效
     * 405	请求重复	检查必要的关键参数唯一性
     * 500	服务器异常	服务器出现异常，联系运维人员
     * 4021	JSON格式错误	检查参数
     * 6001	车场无响应	检查车场是否在线
     * 6003	未找到车辆入场记录	检查车场是否入场或参数是否正确
     * 6004	获取缴费金额失败	获取缴费金额失败
     * 6005	支付通知失败	支付通知失败
     * 6006	支付重复通	支付重复通
     * 6007	优惠券下发失败	优惠券下发失败
     * <p>
     * Output message format:
     * ParkingDiscount.requestParkingDiscount.outputMessage
     */
    public static JSONObject translateResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        int code = msg.getIntValue("code");
        result.put("code", code == 200 ? "ok" : "" + code);

        result.put("desc", msg.getString("msg"));

        return result;
    }

    public static String _genSignWithMd5(JSONObject msg, String secretKey) {
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
            sb.append("&key=").append(secretKey);
            return DigestUtils.md5Hex(sb.toString()).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

}
