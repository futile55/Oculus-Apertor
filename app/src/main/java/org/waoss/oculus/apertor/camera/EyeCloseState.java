package org.waoss.oculus.apertor.camera;

public class EyeCloseState {
    private boolean isClosed;
    private long time;

    public EyeCloseState(final boolean isClosed, final long time) {
        this.isClosed = isClosed;
        this.time = time;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(final boolean closed) {
        isClosed = closed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(final long time) {
        this.time = time;
    }
}
