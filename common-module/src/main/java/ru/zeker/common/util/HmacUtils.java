package ru.zeker.common.util;

import lombok.experimental.UtilityClass;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@UtilityClass
public final class HmacUtils {

    public static String sign(String data, String secret, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        var secretKeySpec = new SecretKeySpec(secret.getBytes(), algorithm);
        var mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);
        var rawHmac = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    public static boolean verify(String data, String signature, String secret, String algorithm)
            throws InvalidKeyException, NoSuchAlgorithmException {

        var expectedSignature = sign(data, secret, algorithm);
        return expectedSignature.equals(signature);
    }
}
