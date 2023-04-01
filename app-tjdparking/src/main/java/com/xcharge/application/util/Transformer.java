package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Transformer {
    private final static String API_VERSION = "1.0";
    private final static String SERVICE_QUERY = "parkhub.order.infoForFreeMins";
    private final static String SERVICE_COUPON = "parkhub.order.deductionForDetail";
    private final static String PREPAY_TYPE = "20";

    public static JSONObject translateQueryRequestMessage(JSONObject msg, String partner, String secret) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        result.put("carNum", msg.getString("carNo"));
        result.put("service", SERVICE_QUERY);
        result.put("freeMins", "0");
        result.put("version", API_VERSION);
        result.put("partner", partner);
        result.put("charset", "utf-8");
        result.put("timestamp", _getCurrentTimeStr());

        result.put("sign", _genSignWithMd5(result, secret));
        result.put("signType", "md5");

        return result;
    }

    /**
     * Original message format:
     *      ParkingDiscount.requestParkingDiscount.inputMessage
     *      extraInfo: channel
     *
     * Output message format:
     *      {
     *          "carNum": String, // 车牌号
     *          "outTradeNo": String, // 交易单号
     *          "type": Integer, // 优惠类型，见下
     *          "couponType": Integer, // 券类型。见下
     *          "channel": Integer, // 支付渠道。见下
     *          "minutes": Integer, // 优惠时长。见下
     *          "amount": Float, // 优惠金额，单位为元。见下
     *          "startDt": String, // 开始时间，格式为 yyyyMMddhhmmss。见下
     *          "endDt": String, // 结束时间，格式为 yyyyMMddhhmmss。见下
     *          "expireDt": String, // 过期时间，格式为 yyyyMMddhhmmss。见下
     *      }
     *
     *      优惠类型（type）:
     *          0: 抵扣。此时须填写 channel、amount
     *          1: 优惠券。此时须填写 couponType、channel、expireDt。expireDt 如不填则永不过期
     *
     *      优惠券类型（couponType）:
     *          0: 金额。此时须填写 amount
     *          1: 时长。此时须填写 minutes
     *          2: 全免
     *          3: 时间区间。此时须填写 startDt、endDt
     *
     *      支付渠道（channel）:
     *          5013: 微信
     *          5014: 支付宝
     *          5015: 抵扣其他
     *          2202: 会员等级
     *          2203: 会员积分
     *          2204: 会员卡券
     *          2205: 会员补贴
     *          2206: 会员其他
     */
    public static JSONObject translateDiscountRequestMessage(JSONObject msg, JSONObject queryResult, String partner, String secret) throws Exception {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        result.put("service", SERVICE_COUPON);
        result.put("version", API_VERSION);
        result.put("partner", partner);
        result.put("charset", "utf-8");
        result.put("timestamp", _getCurrentTimeStr());
        result.put("tradeId", queryResult.getString("tradeId"));
        result.put("accountId", queryResult.getString("accountId"));
        result.put("prePayType", PREPAY_TYPE);

        JSONObject extraInfo = msg.getJSONObject("extraInfo");
        Integer channel = extraInfo==null ? null : extraInfo.getInteger("channel");

        JSONObject detail = new JSONObject();
        detail.put("outTradeNo", msg.getString("requestId"));

        int discountType = msg.getIntValue("type", 1);
        switch (discountType) {
            case 0:
                detail.put("type", 1);
                detail.put("couponType", 0);
                detail.put("channel", channel);
                detail.put("amount", msg.getIntValue("value", 0) / 100.0);
                detail.put("expireDt", _translateTimeFormat(msg.getString("expireTime")));
                break;
            case 1:
                detail.put("type", 1);
                detail.put("couponType", 1);
                detail.put("channel", channel);
                detail.put("minutes", msg.getIntValue("value", 0));
                detail.put("expireDt", _translateTimeFormat(msg.getString("expireTime")));
                break;
            case 9:
                detail.put("type", 1);
                detail.put("couponType", 2);
                detail.put("channel", channel);
                detail.put("expireDt", _translateTimeFormat(msg.getString("expireTime")));
                break;
            default:
                throw new Exception("不支持该优惠类型");
        }

        result.put("detailList", _encodeDetail(detail));

        result.put("sign", _genSignWithMd5(result, secret));
        result.put("signType", "md5");
        return result;
    }

    /**
     * Original message format:
     *      {
     *          "returnCode": String, // "T" 请求成功，"F" 失败（通常为请求格式错误）
     *          "returnMsg": String, // "OK" 或错误信息
     *          "isSuccess": Integer, // 0 业务处理成功，其它值表示失败
     *          "errorMSG": String // 处理失败时填写失败原因描述
     *          "deductionId": String // 与交易单号相对应的优惠ID
     *      }
     *
     * Output message format:
     *      ParkingDiscount.requestParkingDiscount.outputMessage
     *      data: discountId, tradeId, accountId
     */
    public static JSONObject translateResponseMessage(JSONObject msg) {
        JSONObject result = new JSONObject();
        if (msg == null) msg = new JSONObject();

        String code;
        String desc;

        code = msg.getString("returnCode");
        if ("T".equals(code)) {
            int iCode = msg.getIntValue("isSuccess", -1);
            if (iCode == 0) {
                code = "ok";
                desc = msg.getString("returnMsg");
            } else {
                code = ""+iCode;
                desc = msg.getString("errorMSG");
            }
        } else {
            code = "F";
            desc = msg.getString("returnMsg");
        }

        result.put("code", code);
        result.put("desc", desc);
        result.put("discountId", msg.getString("deductionId"));
        result.put("tradeId", msg.getString("tradeId"));
        result.put("accountId", msg.getString("accountId"));

        return result;
    }

    private static String _getCurrentTimeStr() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(System.currentTimeMillis());
    }

    private static String _translateTimeFormat(String time) {
        // From "yyyy-MM-dd HH:mm:ss" to "yyyyMMddHHmmss"
        return time.replaceAll("[-\\s:]", "");
    }

    private static String _encodeDetail(JSONObject params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        int size = keys.size();

        StringBuilder result = new StringBuilder();
        result.append("[{");

        for (int i = 0; i < size; ++i) {
            String key = keys.get(i);
            Object obj = params.get(key);
            if (obj != null) {
                String value = String.valueOf(obj);
                result.append("\"").append(key).append("\"").append(":").append("\"").append(value).append("\"");
                if (i != size - 1) {
                    result.append(",");
                }
            }
        }

        result.append("}]");
        return result.toString();
    }

    private static String _genSignWithMd5(JSONObject msg, String secret) {
        try {
            List<String> keys = new ArrayList<>(msg.keySet());
            Collections.sort(keys);
            int size = keys.size();

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < size; ++i) {
                String key = keys.get(i);
                Object obj = msg.get(key);
                if (obj != null) {
                    if (obj instanceof Object[]) {
                        result.append(key).append("=").append(Arrays.toString((Object[]) obj));
                    } else {
                        String value = String.valueOf(obj);
                        result.append(key).append("=").append(value);
                    }
                    if (i != size - 1) {
                        result.append("&");
                    }
                }
            }

            result.append(secret);

            return DigestUtils.md5Hex(result.toString()).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }

}
