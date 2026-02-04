package kr.or.kimsn.radarsms.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
// import org.apache.commons.codec.binary.Base64;

import org.apache.tomcat.util.codec.binary.Base64;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CryptUtil {
  private static final int SALT_LEN = 8;
  private static final int USER_KEY_LEN = 16;
  private static final int SEED_BLOCK_LEN = 16;
  private static final int PBE_ITERATION = 100;
  private static final String MD5_SALT_PREFIX = "$1$";
  private static final String B64CHARS = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
  private static CryptUtil theInstance;
  private boolean isInitialized = false;
  private boolean isAllowed = true;
  private String errMessage = null;
  private MessageDigest md5;
  private MessageDigest sha1;
  private MessageDigest sha256;
  private SecureRandom srandom;
  private Base64 benc;
  private Base64 bdec;

  public static CryptUtil getInstance() {
    if (theInstance == null) {
      theInstance = new CryptUtil();
    }
    return theInstance;
  }

  private CryptUtil() {
    try {
      this.md5 = MessageDigest.getInstance("MD5");
      this.sha1 = MessageDigest.getInstance("SHA1");
      this.sha256 = MessageDigest.getInstance("SHA-256");

      this.srandom = new SecureRandom();
      this.benc = new Base64();
      this.bdec = new Base64();
      this.isInitialized = true;
    } catch (NoSuchAlgorithmException e) {
      this.errMessage = "CryptUtil initialization failure : " + e
          .getMessage();
      e.printStackTrace();
    }
  }

  public final String hashMD5(String plainText) {
    if (!this.isInitialized) {
      return this.errMessage;
    }
    return b64(this.md5.digest(string2Bytes(plainText)));
  }

  public final String hashSHA1(String plainText) {
    if (!this.isInitialized) {
      return this.errMessage;
    }
    return b64(this.sha1.digest(string2Bytes(plainText)));
  }

  private static byte[] string2Bytes(String in) {
    return in.getBytes();
  }

  private static void clearMem(byte[] buf) {
    if (buf != null) {
      for (int i = 0; i < buf.length; i++) {
        buf[i] = 0;
      }
    }
  }

  private static String long2String(long MAC) {
    if (MAC > 281474976710655L) {
      return "";
    }
    StringBuffer sb = new StringBuffer();

    return sb.toString();
  }

  private static boolean isAllowedHW(String localMac) {
    boolean retVal = false;
    String[] allowedMACs = { "00-07-E9-3F-6D-10", "00-07-E9-3F-6D-11",
        "00-60-B0-05-6E-DF", "00-C0-26-EF-93-DA" };
    for (int i = 0; i < allowedMACs.length; i++) {
      if (localMac.equals(allowedMACs[i])) {
        retVal = true;
        break;
      }
    }
    return retVal;
  }

  private static String gnuBase64Encode(byte[] rawBytes) {
    StringBuffer sb = new StringBuffer();
    b64_from_24bit(rawBytes[0], rawBytes[6], rawBytes[12], 4, sb);
    b64_from_24bit(rawBytes[1], rawBytes[7], rawBytes[13], 4, sb);
    b64_from_24bit(rawBytes[2], rawBytes[8], rawBytes[14], 4, sb);
    b64_from_24bit(rawBytes[3], rawBytes[9], rawBytes[15], 4, sb);
    b64_from_24bit(rawBytes[4], rawBytes[10], rawBytes[5], 4, sb);
    b64_from_24bit((byte) 0, (byte) 0, rawBytes[11], 2, sb);
    return sb.toString();
  }

  private static void b64_from_24bit(byte B2, byte B1, byte B0, int N, StringBuffer sb) {
    int i = (B2 & 0xFF) << 16 | (B1 & 0xFF) << 8 | B0 & 0xFF;
    while (N-- > 0) {
      sb.append("./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
          .charAt(i & 0x3F));
      i >>>= 6;
    }
  }

  public String b64(byte[] raw) {
    return this.benc.encodeToString(raw);
  }

  private byte[] doLoop(byte[] salt, String password) {
    byte[] tmpPW = string2Bytes(password);
    byte[] S = new byte[tmpPW.length + salt.length];
    System.arraycopy(tmpPW, 0, S, 0, tmpPW.length);
    System.arraycopy(salt, 0, S, tmpPW.length, salt.length);
    clearMem(tmpPW);
    tmpPW = (byte[]) null;
    for (int i = 0; i < 100; i++) {
      S = this.sha1.digest(S);
    }
    return S;
  }

  private SecretKeySpec getSecretKey(byte[] S) {
    byte[] hashKey = new byte[16];
    System.arraycopy(S, 0, hashKey, 0, hashKey.length);
    SecretKeySpec sKey = new SecretKeySpec(hashKey, "SEED");
    clearMem(hashKey);
    hashKey = (byte[]) null;
    return sKey;
  }

  private IvParameterSpec getIV(byte[] S) {
    byte[] tmpS = new byte[S.length - 16];
    System.arraycopy(S, 16, tmpS, 0, tmpS.length);
    clearMem(S);
    byte[] tmpIV = this.sha1.digest(tmpS);
    clearMem(tmpS);
    tmpS = (byte[]) null;
    byte[] iv = new byte[16];
    System.arraycopy(tmpIV, 0, iv, 0, iv.length);
    clearMem(tmpIV);
    tmpIV = (byte[]) null;

    IvParameterSpec ivSpec = new IvParameterSpec(iv);
    clearMem(iv);
    iv = (byte[]) null;
    return ivSpec;
  }

  private String getLocalMAC() {
    String retVal = "";
    try {
      Process p = Runtime.getRuntime().exec("ipconfig.exe /all");
      BufferedReader is = new BufferedReader(new InputStreamReader(
          p.getInputStream()));
      String aLine;
      while ((aLine = is.readLine()) != null) {
        aLine = aLine.trim();
        if (aLine.startsWith("Physical Address")) {
          retVal = aLine.substring(aLine.indexOf(':') + 1).trim();
          break;
        }
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return retVal;
  }

  public static String hasing(String passwd) {
    byte[] byteArray = passwd.getBytes();
    MessageDigest md = null;

    try {
      md = MessageDigest.getInstance("SHA1");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    if (md == null)
      return null;
    md.reset();

    byte[] digest = md.digest(byteArray);

    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < digest.length; i++) {
      buffer.append(Integer.toHexString((digest[i] & 0xF0) >> 4));
      buffer.append(Integer.toHexString(digest[i] & 0xF));
    }

    return buffer.toString();
  }

  public static String convert(String source) {
    if (source == null)
      return null;
    source = source.trim();
    if ("".equals(source))
      return "";
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");

      byte[] digest = md5.digest(source.getBytes());
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < digest.length; i++) {
        sb.append(Integer.toHexString((digest[i] & 0xF0) >> 4));
        sb.append(Integer.toHexString(digest[i] & 0xF));
      }
      return sb.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String hasingToB64(String passwd) {
    byte[] byteArray = passwd.getBytes();
    MessageDigest md = null;

    try {
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    if (md == null)
      return null;
    md.reset();

    byte[] digest = md.digest(byteArray);

    StringBuffer buffer = new StringBuffer();
    Base64 bs64 = new Base64();

    return bs64.encodeToString(digest);
  }

  public static void main(String[] args) throws Exception {
    CryptUtil cryptUtil = new CryptUtil();
    log.info("password : 12345 >>> ");

    log.info("++++++++++++++++++++++++++");

    log.info(hasing("1111"));

    log.info("++++++++++++++++++++++++++");

    log.info(hasingToB64("Kmmin67!!"));
  }
}