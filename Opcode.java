package com.chuckgreenman.lc3;

public enum Opcode {
    OP_BR,
    OP_ADD,
    OP_LD,
    OP_ST,
    OP_JSR,
    OP_AND,
    OP_LDR,
    OP_STR,
    OP_RTI,
    OP_NOT,
    OP_LDI,
    OP_STI,
    OP_JMP,
    OP_RES,
    OP_LEA,
    OP_TRAP;

    public static Opcode fromInt(int value) {
        if (value >= 0 && value < values().length) {
            return values()[value];
        }
        return null;
    }
}
