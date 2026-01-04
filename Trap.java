package com.chuckgreenman.lc3;

public enum Trap {
    TRAP_GETC(0x20),
    TRAP_OUT(0x21),
    TRAP_PUTS(0x22),
    TRAP_IN(0x23),
    TRAP_PUTSP(0x24),
    TRAP_HALT(0x25);

    private final int value;

    Trap(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Trap fromInt(int value) {
        for (Trap trap : values()) {
            if (trap.value == value) {
                return trap;
            }
        }
        return null;
    }
}
