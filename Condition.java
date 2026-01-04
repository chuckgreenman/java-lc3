package com.chuckgreenman.lc3;

public enum Condition {
    FL_POSITIVE(1 << 0),
    FL_ZERO(1 << 1),
    FL_NEGATIVE(1 << 2);

    private final int value;

    Condition(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
