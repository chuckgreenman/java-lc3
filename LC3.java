package com.chuckgreenman.lc3;

public class LC3 {

    private static final int MAX_ADDRESSABLE_MEMORY = 65536;
    private static final int REGISTER_COUNT = 10;
    private static final int PC_START = 0x3000;

    private final short[] memory = new short[MAX_ADDRESSABLE_MEMORY];
    private final short[] registers = new short[REGISTER_COUNT];

    public static void main(String[] args) {
        loadImages(args)

        LC3 vm = new LC3();
        vm.run();
    }

    public void loadImages(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException(
                "Usage: java lc3 [image file path]"
            );
        }
    }

    public void run() {
        boolean running = true;
        this.registers[Register.PC.ordinal()] = (short) PC_START;

        while (running) {
            int instruction = 0;
            int opValue = instruction >> 12;
            Opcode op = Opcode.fromInt(opValue);
            switch (op) {
                case OP_BR:
                    break;
                case OP_ADD:
                    break;
                case OP_LD:
                    break;
                case OP_ST:
                    break;
                case OP_JSR:
                    break;
                case OP_AND:
                    break;
                case OP_LDR:
                    break;
                case OP_STR:
                    break;
                case OP_RTI:
                    break;
                case OP_NOT:
                    break;
                case OP_LDI:
                    break;
                case OP_STI:
                    break;
                case OP_JMP:
                    break;
                case OP_RES:
                    break;
                case OP_LEA:
                    break;
                case OP_TRAP:
                    break;
                default:
                    System.out.println("Op Code Not Supported");
                    running = false;
                    break;
            }
            running = false;
        }
    }
}
