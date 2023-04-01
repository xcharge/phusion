package com.xcharge.application.util;

import com.alibaba.fastjson2.JSONObject;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Base64;

public class SignUtil {

    public static StringBuilder coverJSONObject2String(JSONObject params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        int size = keys.size();

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < size; ++i) {
            String key = keys.get(i);
            Object obj = params.get(key);
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

        return result;
    }

    public static PrivateKey convertBytesToKey(byte[] pem) throws Exception {
        String strPem = new String(pem);

        strPem = strPem.replace("-----BEGIN PRIVATE KEY-----", "");
        strPem = strPem.replace("-----END PRIVATE KEY-----", "");
        strPem = strPem.replaceAll("\n", "");

        byte[] decoded = Base64.getDecoder().decode(strPem);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static String signWithPrvateKey(String param, PrivateKey key) throws Exception {
        java.security.Signature sign = java.security.Signature.getInstance("MD5withRSA");
        sign.initSign(key);
        sign.update(param.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(sign.sign());
    }

}
