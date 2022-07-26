package com.jtyjy.finance.manager.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author User
 */
@Slf4j
public class AesUtil {

    /**
     * 密钥
     */
    public static String key = "AD42F6697B035B75";

    /**
     * 编码方式
     */
    private static final String CHARSET = "utf-8";

    /**
     * 加密器类型:加密算法为AES,加密模式为ECB,补码方式为PKCS5Padding
     */
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * 算法类型：用于指定生成AES的密钥
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加密
     */
    public static String encrypt(String content) {
        return encrypt(content, key);
    }

    /**
     * 解密
     */
    public static String decrypt(String content) {
        return decrypt(content, key);
    }

    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @param key     加密密码
     * @return 秘钥
     */
    public static String encrypt(String content, String key) {
        try {
            // 构造密钥
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
            // 创建AES加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] byteContent = content.getBytes(CHARSET);
            // 使用加密器的加密模式
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            // 加密
            byte[] result = cipher.doFinal(byteContent);
            // 使用BASE64对加密后的二进制数组进行编码
            return Base64.encodeBase64String(result);
        } catch (Exception e) {
            log.error("加密异常：", e);
        }
        return null;
    }

    /**
     * AES（256）解密
     *
     * @param content 待解密内容
     * @param key     解密密钥
     * @return 解密之后
     */
    public static String decrypt(String content, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            // 解密时使用加密器的解密模式
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] result = cipher.doFinal(Base64.decodeBase64(content));
            return new String(result, CHARSET);
        } catch (Exception e) {
            log.error("解密异常：", e);
        }
        return null;
    }

    public static void main(String[] args) {
        String s = "123456";
        String encryptResultStr = encrypt(s);
        // 加密
        System.out.println("加密前：" + s);
        System.out.println("加密后：" + encryptResultStr);
        // 解密
        System.out.println("解密后：" + decrypt(encryptResultStr));
    }
}
