package com.xcharge.application.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Security {

    private static Logger logger = LoggerFactory.getLogger(Security.class);

    public static String getSign(String content, String secret, String charset, String signType) {
        try {
            return sign(content, secret, charset, signType);
        } catch (Exception e) {
            logger.error("", e);
        }
        return null;
    }

    public static String getSignContent(Map<String, Object> params) {
        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList(params.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); ++i) {
            String key = keys.get(i);
            Object value = params.get(key);
            content.append(key).append(value);
        }
        return content.toString();
    }

    public static String sign(String content, String secret, String charset, String signType) throws Exception {
        return encrypt(content, secret, charset);
    }

    private static String encrypt(String input, String secret, String charset) {
        String source = secret + input + secret;
        return encryptUpper(source.getBytes(StandardCharsets.UTF_8));
    }

    public static String encryptUpper(byte[] input) {
        return encrypt(input).toUpperCase();
    }

    public static String encrypt(byte[] input) {
        if (input != null && input.length != 0) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] buff = md.digest(input);
                return byte2hex(buff);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("The argument input can not be empty.");
        }
    }

    public static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();

        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 255);
            if (hex.length() == 1) {
                sign.append("0");
            }

            sign.append(hex);
        }

        return sign.toString();
    }
}
