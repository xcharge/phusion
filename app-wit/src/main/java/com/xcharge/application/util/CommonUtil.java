package com.xcharge.application.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @author zh
 * @date 2023/6/1 16:17
 */
public class CommonUtil {

    //加密密钥
    public static final String KEY = "EGLErRcdtHsEgRqR";
    //向量iv
    public static final String IV = "StTB4MuxrZ50Qg6z";
    // 签名密钥
    public static final String SIG_SECRET = "BxmFTMr72SAql8XR";


    public static String encrypt(String content) throws Exception {
        if (content == null) {
            return "";
        }

        byte[] raw = KEY.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
        //使用CBC模式，需要一个向量iv，可增加加密算法的强度
        IvParameterSpec ips = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ips);
        byte[] encrypted = cipher.doFinal(content.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String sSrc) throws Exception {
        byte[] raw = KEY.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        byte[] encrypted1 = Base64.getDecoder().decode(sSrc);
        byte[] original = cipher.doFinal(encrypted1);
        return new String(original);
    }

    public static String getHmacMd5Str(String key, String data) throws Exception {
        byte[] keyByte = key.getBytes(StandardCharsets.UTF_8);
        byte[] dataByte = data.getBytes(StandardCharsets.UTF_8);
        byte[] hmacMd5Byte = getHmacMd5Bytes(keyByte, dataByte);
        StringBuilder md5StrBuff = new StringBuilder();
        for (byte b : hmacMd5Byte) {
            if (Integer.toHexString(0xFF & b).length() == 1) {
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & b));
            } else {
                md5StrBuff.append(Integer
                        .toHexString(0xFF & b));
            }
        }
        return md5StrBuff.toString().toUpperCase();
    }

    /**
     * 将待加密数据data，通过密钥key，使用hmac-md5算法进行加密，然后返回加密结果。 参照rfc2104 HMAC算法介绍实现。
     *
     * @param key  密钥
     * @param data 待加密数据
     * @return 加密结果
     */
    public static byte[] getHmacMd5Bytes(byte[] key, byte[] data)
            throws NoSuchAlgorithmException {
        /*
         * HmacMd5 calculation formula: H(K XOR opad, H(K XOR ipad, text))
         * HmacMd5 计算公式：H(K XOR opad, H(K XOR ipad, text))
         * H代表hash算法，本类中使用MD5算法，K代表密钥，text代表要加密的数据 ipad为0x36，opad为0x5C。
         */
        int length = 64;
        byte[] ipad = new byte[length];
        byte[] opad = new byte[length];
        for (int i = 0; i < 64; i++) {
            ipad[i] = 0x36;
            opad[i] = 0x5C;
        }
        byte[] actualKey = key; // Actual key.
        byte[] keyArr = new byte[length]; // Key bytes of 64 bytes length
        /*
         * If key's length is longer than 64,then use hash to digest it and use
         * the result as actual key. 如果密钥长度，大于64字节，就使用哈希算法，计算其摘要，作为真正的密钥。
         */
        if (key.length > length) {
            actualKey = md5(key);
        }
        System.arraycopy(actualKey, 0, keyArr, 0, actualKey.length);
        /*
         * append zeros to K 如果密钥长度不足64字节，就使用0x00补齐到64字节。
         */
        if (actualKey.length < length) {
            for (int i = actualKey.length; i < keyArr.length; i++) {
                keyArr[i] = 0x00;
            }
        }

        /*
         * calc K XOR ipad 使用密钥和ipad进行异或运算。
         */
        byte[] kIpadXorResult = new byte[length];
        for (int i = 0; i < length; i++) {
            kIpadXorResult[i] = (byte) (keyArr[i] ^ ipad[i]);
        }

        /*
         * append "text" to the end of "K XOR ipad" 将待加密数据追加到K XOR ipad计算结果后面。
         */
        byte[] firstAppendResult = new byte[kIpadXorResult.length + data.length];
        System.arraycopy(kIpadXorResult, 0, firstAppendResult, 0, kIpadXorResult.length);
        System.arraycopy(data, 0, firstAppendResult, keyArr.length, data.length);

        /*
         * calc H(K XOR ipad, text) 使用哈希算法计算上面结果的摘要。
         */
        byte[] firstHashResult = md5(firstAppendResult);

        /*
         * calc K XOR opad 使用密钥和opad进行异或运算。
         */
        byte[] kOpadXorResult = new byte[length];
        for (int i = 0; i < length; i++) {
            kOpadXorResult[i] = (byte) (keyArr[i] ^ opad[i]);
        }

        /*
         * append "H(K XOR ipad, text)" to the end of "K XOR opad" 将H(K XOR
         * ipad, text)结果追加到K XOR opad结果后面
         */
        byte[] secondAppendResult = new byte[kOpadXorResult.length
                + firstHashResult.length];
        System.arraycopy(kOpadXorResult, 0, secondAppendResult, 0, kOpadXorResult.length);
        System.arraycopy(firstHashResult, 0, secondAppendResult, keyArr.length, firstHashResult.length);

        /*
         * H(K XOR opad, H(K XOR ipad, text)) 对上面的数据进行哈希运算。
         */
        return md5(secondAppendResult);
    }

    /**
     * 计算参数的md5信息
     *
     * @param str 待处理的字节数组
     * @return md5摘要信息
     */
    private static byte[] md5(byte[] str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str);
        return md.digest();
    }

    public static String sign(String value) throws Exception {
        return getHmacMd5Str(SIG_SECRET, value);

    }


    public static void main(String[] args) throws Exception {
        System.out.println(encrypt("{\"OperatorID\":\"123456789\",\"OperatorSecret\":\"1234567890abcdef\"}"));
    }
}
