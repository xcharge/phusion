package com.xcharge.application.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.alibaba.fastjson2.JSONObject;

import java.util.*;

public class Signature {

    /**
     * Command line tool for generating XCharge signature
     */
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter JSON data (end with an empty line):");

        StringBuilder msg = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                break;
            }
            msg.append(line);
        }

        System.out.println("Enter Secret Key:");
        String secretKey = scanner.nextLine();
        System.out.println();

        JSONObject objMsg = JSONObject.parseObject(msg.toString());
        objMsg.remove("sign");
        StringBuilder data = coverJSONObject2String( objMsg );
        String hmacMd5Str = getHmacMd5Str(secretKey, data.toString());

        System.out.println("Signature: " + hmacMd5Str.toLowerCase() + " (timestamp: " + System.currentTimeMillis() + ")");
    }

    private static byte[] md5(byte[] str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str);
        return md.digest();
    }

    public static byte[] getHmacMd5Bytes(byte[] key, byte[] data) throws NoSuchAlgorithmException {
        int length = 64;
        byte[] ipad = new byte[length];
        byte[] opad = new byte[length];
        for (int i = 0; i < 64; i++) {
            ipad[i] = 0x36;
            opad[i] = 0x5C;
        }
        byte[] actualKey = key;
        byte[] keyArr = new byte[length];

        if (key.length > length) {
            actualKey = md5(key);
        }
        for (int i = 0; i < actualKey.length; i++) {
            keyArr[i] = actualKey[i];
        }

        if (actualKey.length < length) {
            for (int i = actualKey.length; i < keyArr.length; i++)
                keyArr[i] = 0x00;
        }

        byte[] kIpadXorResult = new byte[length];
        for (int i = 0; i < length; i++) {
            kIpadXorResult[i] = (byte) (keyArr[i] ^ ipad[i]);
        }

        byte[] firstAppendResult = new byte[kIpadXorResult.length + data.length];
        for (int i = 0; i < kIpadXorResult.length; i++) {
            firstAppendResult[i] = kIpadXorResult[i];
        }
        for (int i = 0; i < data.length; i++) {
            firstAppendResult[i + keyArr.length] = data[i];
        }

        byte[] firstHashResult = md5(firstAppendResult);

        byte[] kOpadXorResult = new byte[length];
        for (int i = 0; i < length; i++) {
            kOpadXorResult[i] = (byte) (keyArr[i] ^ opad[i]);
        }

        byte[] secondAppendResult = new byte[kOpadXorResult.length
                + firstHashResult.length];
        for (int i = 0; i < kOpadXorResult.length; i++) {
            secondAppendResult[i] = kOpadXorResult[i];
        }
        for (int i = 0; i < firstHashResult.length; i++) {
            secondAppendResult[i + keyArr.length] = firstHashResult[i];
        }

        byte[] hmacMd5Bytes = md5(secondAppendResult);
        return hmacMd5Bytes;
    }

    public static String getHmacMd5Str(String key, String data) throws Exception {
        String result = "";
        if (key==null || key.length()==0) return result;

        byte[] keyByte = key.getBytes("UTF-8");
        byte[] dataByte = data.getBytes("UTF-8");
        byte[] hmacMd5Byte = getHmacMd5Bytes(keyByte, dataByte);
        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < hmacMd5Byte.length; i++) {
            if (Integer.toHexString(0xFF & hmacMd5Byte[i]).length() == 1)
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & hmacMd5Byte[i]));
            else
                md5StrBuff.append(Integer
                        .toHexString(0xFF & hmacMd5Byte[i]));
        }
        result = md5StrBuff.toString().toUpperCase();

        return result;
    }

    public static StringBuilder coverJSONObject2String(JSONObject params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        int size = keys.size();

        for (int i = 0; i < size; ++i) {
            String key = keys.get(i);
            Object obj = params.get(key);
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
        return sb;
    }

    public static boolean verifySignature(JSONObject msg, String secretKey, long maxGap) throws Exception {
        if (msg==null || !msg.containsKey("sign")) return false;

        if (msg.containsKey("signType") && !"MD5".equalsIgnoreCase(msg.getString("signType"))) return false;

        if (!msg.containsKey("timestamp")) return false;

        long timestamp = msg.getLong("timestamp");
        long current = System.currentTimeMillis();
        if (Math.abs(timestamp - current) > maxGap ) return false;

        String sign = msg.getString("sign");
        msg.remove("sign"); // "sign" not participate in the verification of the signature
        StringBuilder data = Signature.coverJSONObject2String(msg);
        String hmacMd5Str = Signature.getHmacMd5Str(secretKey, data.toString());
        return hmacMd5Str.toLowerCase().equals(sign);
    }

}
