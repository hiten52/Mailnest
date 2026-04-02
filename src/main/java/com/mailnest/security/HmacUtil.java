package com.mailnest.security;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HmacUtil {

  private HmacUtil() {}

  public static String sign(String data, String secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(result);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to sign HMAC", e);
    }
  }

  public static boolean verify(String data, String tag, String secret) {
    String expected = sign(data, secret);
    return expected.equals(tag);
  }
}
