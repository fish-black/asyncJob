package com.fishblack.async.job;

public enum JobPriority {

    HIGH (1),
    MEDIUM (10),
    LOW (999);

    private final int value;

    JobPriority(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
