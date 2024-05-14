package com.xcharge.application.util;

import cloud.phusion.Context;
import cloud.phusion.storage.FileStorage;
import com.alibaba.fastjson2.JSONObject;
import com.xcharge.application.JsLifeV3;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.DecimalFormat;
import java.util.*;

public class Transformer {

    private final static String _position = Transformer.class.getName();

    /**
     * Original message format:
     * ParkingDiscount.requestParkingDiscount.inputMessage
     * extraInfo: EVChargingRecord's startTime, endTime
     * <p>
     * Output message format:
     * {
     * "app_id": String,  //开放平台分配的应用id
     * "method": String,  //接口编码
     * "timestamp": Long,  //时间戳
     * "charset": "utf-8", //字符集
     * "format": "form",  //默认json,调用接口编码method以3C.xx开头的接口，需要传form
     * "projectCode": String, //项目编号
     * "abilityCode": String, //开放服务编码，在应用中，已开通服务的服务名下有显示
     * "sign": String,  //签名串
     * "biz_content": {   //具体接口请求参数，如果调接口编号以3C.xx开头的接口，需要添加三个参数cid、v、p到biz_content中 ，并将接口请求参数放入p参数中
     * "p": {     //具体接口请求参数
     * "requestType": "DATA", //固定值传入：DATA
     * "attributes": {
     * "carNo": "京-A00000",  //需关闭代扣的车牌号 车牌作为传参时统一格式，带横杠，例如 粤-B88888
     * "parkCode": "0000000001",  //车场编号
     * "couponType": 1,  //优惠类型，0：减免金额，1：减免时间，2：全免
     * "couponValue": 2.0, //1、优惠类型为金额时，单位为元； 2、优惠类型为时间时，单位为小时; 3、优惠类型为全免时，值为1，但无直接意义
     * "isRepeat": 1  //不填:默认车辆在同一次入场，可以进行多次打折减免 1: 车辆在同一次入场，可以进行多次打折减免 0: 车辆在同一次入场，只能减免一次
     * },
     * "serviceId": "3c.order.discount"   //服务标识 3c.order.discount
     * },
     * "v": "2",     //版本，默认传2
     * "cid": "000000008036417111" //商户号
     * }
     * }
     */
    public static JSONObject translateRequestMessage(JSONObject msg, String privateKey, String appId, String cid, String projectCode, String abilityCode) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();
        result.put("app_id", appId);
        result.put("method", "3c.order.discount");
        result.put("timestamp", System.currentTimeMillis());
        result.put("charset", "utf-8");
        result.put("format", "form");
        result.put("projectCode", projectCode);
        result.put("abilityCode", abilityCode);
        JSONObject p = new JSONObject();
        p.put("requestType", "DATA");
        p.put("serviceId", "3c.order.discount");
        JSONObject data = new JSONObject();
        data.put("parkCode", msg.getString("parkId"));
        String carStr = msg.getString("carNo");
        String prefix = carStr.substring(0, 1);
        String suffix = carStr.substring(1);
        data.put("carNo", prefix + "-" + suffix);
        int discountType = msg.getIntValue("type", 1);
        data.put("couponType", discountType);
        data.put("couponValue", discountType == 1 ? new DecimalFormat("#.00").format(msg.getDoubleValue("value") / 60) : new DecimalFormat("#.00").format(msg.getDoubleValue("value") / 100));
        p.put("attributes", data);
        JSONObject content = new JSONObject();
        content.put("p", p);
        content.put("v", "2");
        content.put("cid", cid);
        result.put("biz_content", content);
        result.put("sign", _genSignWithMd5(result, privateKey));
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

        String code = msg.getString("code");
        result.put("code", "0".equals(code) ? "ok" : "" + code);
        result.put("desc", msg.getString("msg"));
        if (msg.containsKey("biz_response")) {
            String subCode = msg.getJSONObject("biz_response").getString("resultCode");
            result.put("code", "0".equals(subCode) ? "ok" : "" + subCode);
            result.put("desc", msg.getJSONObject("biz_response").getString("message"));
        }
        return result;
    }

    private static String _genSignWithMd5(JSONObject msg, String privateKey) {
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
            return sign(sb.toString(), privateKey);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 加签
     *
     * @param data 需要签名字符串
     * @return 签名结果
     * @throws Exception 签名异常
     */
    private static String sign(String data, String privateKey) throws Exception {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] keyBytes = decoder.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(privateK);
        signature.update(data.getBytes());
        return Base64.getEncoder().encodeToString(signature.sign());
    }

}
