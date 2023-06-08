package com.xcharge.application.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zh
 * @date 2023/6/6 14:51
 */
public class CiphertextTransformer {
    private static final Map<String, String> map = new ConcurrentHashMap<String, String>(3);


    public static JSONObject translateRequestData(String data, String operatorId) throws Exception {
        JSONObject param = new JSONObject();

        param.put("OperatorID", operatorId);

        String encrypt = CommonUtil.encrypt(data);
        param.put("Data", encrypt);

        String timeStamp = getNow14();
        param.put("TimeStamp", timeStamp);

        String seq = getSeq(timeStamp);
        param.put("Seq", seq);

        String value = operatorId + encrypt + timeStamp + seq;
        String hmacMd5Str = CommonUtil.sign(value);
        param.put("Sig", hmacMd5Str);

        return param;
    }

    public static JSONObject translateResponseData(String data) {
        JSONObject decData = null;
        try {
            decData = JSONObject.parseObject(CommonUtil.decrypt(data));
        } catch (Exception e) {
        }
        return decData == null ? new JSONObject() : decData;
    }

    private static String getNow14() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        return df.format(new Date());
    }

    public static String getSeq(String timeStamp) {
        map.keySet().removeIf(key -> !key.equals(timeStamp));

        if (map.containsKey(timeStamp)) {
            Integer count = Integer.parseInt(map.get(timeStamp)) + 1;
            switch (count.toString().length()) {
                case 1:
                    map.put(timeStamp, "000" + count);
                    break;
                case 2:
                    map.put(timeStamp, "00" + count);
                    break;
                case 3:
                    map.put(timeStamp, "0" + count);
                    break;
                case 4:
                    map.put(timeStamp, "" + count);
                    break;
                default:
                    map.put(timeStamp, "" + count);
                    break;
            }
            return map.get(timeStamp);
        } else {
            map.put(timeStamp, "0001");
            return "0001";
        }
    }
}
