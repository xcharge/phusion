package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Transformer {

    /**
     * Original message format:
     *      ParkingDiscount.requestParkingDiscount.inputMessage
     *      extraInfo: EVChargingRecord's startTime, endTime
     *
     * Output message format:
     *      {
     *          "parkId": String, // 停车场编号
     *          "tradeNo": String, // 交易订单号
     *          "plate": String, // 车牌号
     *          "validDate": String, // 优惠券有效日期（持续到该日的 24 时），格式为 yyyy-MM-dd
     *          "discountType": Integer, // 减免方式，见下
     *          "discountValue": Integer|Float // 减免值，见下
     *      }
     *
     *      减免方式：
     *          0：金额，此时 discountValue 为具体的减免金额，单位为元
     *          1：时间，此时 discountValue 为具体的减免时长，单位为分钟
     *          2：折扣，此时 discountValue 为具体的减免幅度，单位为%
     *          3：全免
     *
     * Original response message format:
     *      {
     *          "success": Boolean,
     *          "message": String, // 处理失败时填写失败原因描述
     *          "data": [{"discountid": String}] // 券ID
     *      }
     */
    public static JSONObject translateRequestMessage(JSONObject msg) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        result.put("tradeNo", msg.getString("requestId"));
        result.put("parkId", msg.getString("parkId"));
        result.put("plate", msg.getString("carNo"));

        String expireTime = msg.getString("expireTime");
        if (expireTime != null) {
            result.put("validDate", expireTime.substring(0,10));
        }

        JSONObject extraInfo = msg.getJSONObject("extraInfo");
        if (extraInfo != null) {
            result.put("hoursToRediscount", extraInfo.getInteger("hoursToRediscount"));
        }

        int discountType = msg.getIntValue("type", 1);
        switch (discountType) {
            case 0:
                result.put("discountType", 0);
                result.put("discountValue", msg.getIntValue("value", 0) / 100.0);
                break;
            case 1:
                result.put("discountType", 1);
                result.put("discountValue", msg.getIntValue("value", 0));
                break;
            case 2:
                result.put("discountType", 2);
                result.put("discountValue", msg.getIntValue("value", 0));
                break;
            case 9:
                result.put("discountType", 3);
                break;
            default:
                throw new Exception("不支持该优惠类型");
        }

        return result;
    }

}
