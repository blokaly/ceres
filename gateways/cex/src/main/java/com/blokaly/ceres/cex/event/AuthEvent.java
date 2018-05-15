package com.blokaly.ceres.cex.event;

public class AuthEvent extends AbstractEvent {

  private Auth auth;
  private String ok;
  private Data data;
  private long timestamp;

  public AuthEvent() {
    super(EventType.AUTH.getType());
  }

  public void setAuth(Auth auth) {
    this.auth = auth;
  }

  public boolean isOk() {
    return "ok".equals(ok);
  }

  public String getErrorMessage() {
    if (data != null) {
      return data.error;
    }
    return null;
  }

  public static class Auth {
    private String key;
    private String signature;
    private long timestamp;

    public Auth(String key, String signature, long timestamp) {
      this.key = key;
      this.signature = signature;
      this.timestamp = timestamp;
    }

    @Override
    public String toString() {
      return "{" +
          "key=" + key +
          ", signature=" + signature +
          ", timestamp=" + timestamp +
          '}';
    }
  }

  @Override
  public String toString() {
    return "AuthEvent{" +
        "e=" + e +
        ", auth=" + auth +
        '}';
  }

  public static class Data {
    private String ok;
    private String error;
  }
}
