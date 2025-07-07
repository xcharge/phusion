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
     * 字段	类型	含义	字段限制说明
     * OperatorID	String	运营商标识	必填
     * parkId	String	停车场id 车场平台提供	必填0-30字符
     * orderNo	String	订单流水号	必填0-30字符
     * plateNo	String	车牌号	必填0-30字符
     * startTime	String	充电开始时间	必填yyyy-MM-dd HH:mm:SS
     * endTime	String	充电结束时间	必填yyyy-MM-dd HH:mm:ss
     */
    public static JSONObject translateRequestMessage(JSONObject msg, String operatorId) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();
        result.put("parkId", msg.getString("parkId"));
        result.put("OperatorID", operatorId);
        result.put("orderNo", msg.getString("requestId"));
        result.put("plateNo", msg.getString("carNo"));
        JSONObject extraInfo = msg.getJSONObject("extraInfo");
        result.put("startTime", extraInfo.getString("startTime"));
        result.put("endTime", extraInfo.getString("endTime"));
        return result;
    }


    /**
     * 参数名称	定义	参数类型	描述
     * 状态	SuccStat	整型	0:成功；1:失败
     * 原因	FailReason	字符串	成功或失败原因
     */
    public static JSONObject translateResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        Integer code = msg.getInteger("SuccStat");
        result.put("code", code != null && code == 0 ? "ok" : "" + code);
        result.put("desc", msg.getString("FailReason"));

        return result;
    }

}
