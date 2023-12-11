package com.xcharge.application.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class TripleDES {
    /**
     * 初始化密钥
     *
     * @return byte[] 密钥
     * @throws Exception
     */
    public static byte[] initSecretKey() throws Exception {
        // 返回生成指定算法的秘密密钥的 KeyGenerator 对象
        KeyGenerator kg = KeyGenerator.getInstance("DESede");
        // 初始化此密钥生成器，使其具有确定的密钥大小
        kg.init(168);
        // 生成一个密钥
        SecretKey secretKey = kg.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * <ul>
     * <li>1、开发日期：2014-4-17</li>
     * <li>2、开发时间：上午10:33:39</li>
     * <li>4、返回类型：SecretKey</li>
     * <li>5、方法含义：</li>
     * <li>6、方法说明：</li>
     * </ul>
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static SecretKey getKey(final String key, final String charset) throws Exception {

        final DESedeKeySpec dks = new DESedeKeySpec(BASE64.decode(key));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        final SecretKey securekey = keyFactory.generateSecret(dks);
        return securekey;

    }

    /**
     * 加密方法
     *
     * @param text    明文
     * @param key     密钥 BASE64
     * @param charset 字符集
     * @return 密文
     * @throws Exception
     */
    public static String encrypt(final String text, final String key, final String charset) throws Exception {

        final Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        SecretKey secKey = getKey(key, charset);
        cipher.init(Cipher.ENCRYPT_MODE, secKey);
        byte[] textBytes = text.getBytes(charset);
        byte[] bytes = cipher.doFinal(textBytes);
        String encryptBase64EncodeString = parseByte2HexStr(bytes);
        return encryptBase64EncodeString == null ? "" : encryptBase64EncodeString.trim();

    }

    /**
     * 解密方法
     *
     * @param text    密文
     * @param key     密钥 BASE64
     * @param charset 字符集
     * @return 明文
     * @throws Exception
     */
    public static String decrypt(final String text, final String key, final String charset) throws Exception {

        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, getKey(key, charset));
        byte[] textBytes = parseHexStr2Byte(text);
        byte[] bytes = cipher.doFinal(textBytes);
        String decryptString = new String(bytes, charset);
        return decryptString == null ? "" : decryptString.trim();
    }

    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }
}
