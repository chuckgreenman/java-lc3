package com.chuckgreenman.lc3;

public class LC3 {

    private static final int MAX_ADDRESSABLE_MEMORY = 65536;
    private static final int REGISTER_COUNT = 10;

    private final short[] memory = new short[MAX_ADDRESSABLE_MEMORY];
    private final short[] registers = new short[REGISTER_COUNT];

    public static void main(String[] args) {
        // Ensuring enums are usable.
        for (Register reg : Register.values()) {
            System.out.println(reg);
        }

        for (Opcode opc : Opcode.values()) {
            System.out.println(opc);
        }

        for (Condition cond : Condition.values()) {
            System.out.println(cond.getValue());
        }
    }
}
