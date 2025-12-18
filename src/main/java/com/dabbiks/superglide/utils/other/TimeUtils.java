package com.dabbiks.superglide.utils.other;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

    private long time = 0;

    public long getTime() {
        return time;
    }

    public void setTime(long amount) {
        time = amount;
    }

    public void incrementTime() {
        time++;
    }

    public String getFormattedTime() {
        long minutes = time / 60;
        long seconds = time % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

}
