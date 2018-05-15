package com.blokaly.ceres.common;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GzipUtils {


  public static String compress(String str) throws IOException {
    if (str == null || str.length() == 0) {
      return str;
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream(out);
    gzip.write(str.getBytes());
    gzip.close();
    return out.toString("ISO-8859-1");
  }


  public static String decompress(String str) throws IOException {
    if (str == null || str.length() == 0) {
      return str;
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
    GZIPInputStream gunzip = new GZIPInputStream(in);
    byte[] buffer = new byte[256];
    int n;
    while ((n = gunzip.read(buffer)) >= 0) {
      out.write(buffer, 0, n);
    }
    return out.toString();
  }


  public static String  decompressURI(String jsUriStr) throws IOException {
    String decodeJSUri=URLDecoder.decode(jsUriStr,"UTF-8");
    return decompress(decodeJSUri);
  }

  public static String  compress2URI(String strData) throws IOException {
    String encodeGzip=compress(strData);
    return URLEncoder.encode(encodeGzip,"UTF-8");
  }

  public static byte[] deflate(String data) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
    GZIPOutputStream gzip = new GZIPOutputStream(bos);
    gzip.write(data.getBytes());
    gzip.close();
    byte[] compressed = bos.toByteArray();
    bos.close();
    return compressed;
  }

  public static String inflate(InputStream in) throws IOException {
    GZIPInputStream gis = new GZIPInputStream(in);
    byte[] bytes = toByteArray(gis);
    return new String(bytes, UTF_8);
  }

  public static String inflate(byte[] compressed) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
    return inflate(bis);
  }

  // Adapted from Apache Commons
  public static byte[] toByteArray(InputStream input) throws IOException {
    try (ByteArrayOutputStream output = new ByteArrayOutputStream()){
      copy(input, output);
      return output.toByteArray();
    }
  }

  public static int copy(InputStream input, OutputStream output) throws IOException {
    long count = copyLarge(input, output);
    return count > 2147483647L ? -1 : (int)count;
  }

  public static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
    return copyLarge(input, output, new byte[bufferSize]);
  }

  public static long copyLarge(InputStream input, OutputStream output) throws IOException {
    return copy(input, output, 4096);
  }

  public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
    long count;
    int n;
    for(count = 0L; -1 != (n = input.read(buffer)); count += (long)n) {
      output.write(buffer, 0, n);
    }

    return count;
  }
}
