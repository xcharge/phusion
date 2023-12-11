package com.xcharge.application.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityUtils {
    private static Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    public static String decryptAndVerifyAndURLDecoder(String cipher,String tripleDESKey) {
        /**
         * 解密
         */
        try {
            // cipher = URLDecoder.decode(cipher, "UTF-8");
            String decryptText = TripleDES.decrypt(cipher, tripleDESKey, "UTF-8");
            return decryptText;
        } catch (Throwable e) {
            logger.error("密文数据=" + cipher + ",解密失败,异常信息:" + e);
           return null;
        }
    }


    public static JSONObject encryptAndSignAndURLEncoder(JSONObject param,String md5SaltKey,String tripleDESKey){
        JSONObject res = new JSONObject();
        try {
            /**
             * 加密
             */
            String encryptText = TripleDES.encrypt(param.toJSONString(), tripleDESKey, "UTF-8");
            res.put("cipherJson",encryptText);
        } catch (Throwable e) {
            logger.error("明文数据=" + param.toJSONString() + ",加签失败,异常信息:", e);
            return res;
        }
        /**
         * 验签
         */
        try {
            // json格式的text做asii码排序,然后通过&做拼接
            String strSignText = genQueryStringByJsonAsii(param.toJSONString());
            String sign = MD5.md5(strSignText, md5SaltKey);
            // sign = URLEncoder.encode(sign, "UTF-8");
            res.put("sign",sign);
        } catch (Exception e) {
            logger.error("明文数据=" + param.toJSONString() + ",加签失败,异常信息:", e);
            return res;
        }
        return res;
    }
    /**
     * 将json格式的字符串按照asii码排序,然后通过&拼接为字符串,类似:amount=1&merchantId=434343
     *
     * @param jsonStr
     * @return
     * @throws Exception
     */
    public static String genQueryStringByJsonAsii(String jsonStr) throws Exception {

        TreeMap<String, Object> requestStrTreeMap = JSON.parseObject(jsonStr, TreeMap.class);

        return genQueryString(requestStrTreeMap);
    }

    /**
     * 将按照asii码排序的map做字符串拼接
     *
     * @param messageMap
     * @return
     * @throws Exception
     */
    private static String genQueryString(TreeMap<String, Object> messageMap) throws Exception {
        if (null == messageMap && messageMap.size() == 0) {
            throw new Exception("messageMap数据为null,不能进行拼接生成字符串");
        }
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;
        int length = messageMap.size();
        for (Map.Entry<String, Object> entry : messageMap.entrySet()) {
            if (addQueryString(stringBuilder, entry.getKey(), String.valueOf(entry.getValue()))) {
                if (index != length - 1) {
                    stringBuilder.append("&");
                }
            }
            ++index;
        }
        return stringBuilder.toString();
    }

    private static boolean addQueryString(StringBuilder stringBuilder, String key, String value) {
        stringBuilder.append(key);
        stringBuilder.append("=");
        stringBuilder.append(value);
        return true;
    }

}
