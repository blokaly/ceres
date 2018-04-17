package com.blokaly.ceres.bitfinex.event;

public class InfoEvent extends AbstractEvent {

    public enum Status {
        WEB_SOCKET_RESTART(20051), PAUSE(20060), RESUME(20061), UNKNOWN(0);
        private final int statusCode;

        Status(int statusCode) {
            this.statusCode = statusCode;
        }
    }

    private String version;
    private String code;
    private String msg;

    public String getVersion() {
        return version;
    }

    public Status getStatus() {
        if (code == null || code.isEmpty()) {
            return Status.UNKNOWN;
        }

        switch (Integer.parseInt(code)) {
            case 20051: return Status.WEB_SOCKET_RESTART;
            case 20060: return Status.PAUSE;
            case 20061: return Status.RESUME;
            default: return Status.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return "InfoEvent{" +
                ", version=" + version +
                ", code=" + code +
                ", msg=" + msg +
                '}';
    }
}
