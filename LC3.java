package com.chuckgreenman.lc3;

public class LC3 {

    private static final int MAX_ADDRESSABLE_MEMORY = 65536;
    private static final int REGISTER_COUNT = 10;
    private static final int PC_START = 0x3000;
    private static final int KEYBOARD_STATUS_REGISTER = 0xFE00;
    private static final int KEYBOARD_DATA_REGISTER = 0xFE02;

    private final int[] memory = new int[MAX_ADDRESSABLE_MEMORY];
    private final int[] registers = new int[REGISTER_COUNT];
    private boolean running;

    public static void main(String[] args) {
        LC3 vm = new LC3();
        vm.loadImages(args);
        vm.run();
    }

    public void loadImages(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException(
                "Usage: java com.chuckgreenman.lc3.LC3 [image file path]"
            );
        }
        for (String imagePath : args) {
            readImage(imagePath);
        }
    }

    public void readImage(String imagePath) {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(imagePath)) {
            int origin = read16(fis);
            int address = origin;
            int word;
            while ((word = read16(fis)) != -1) {
                memory[address++] = word;
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read image: " + imagePath, e);
        }
    }

    private int read16(java.io.FileInputStream fis) throws java.io.IOException {
        int high = fis.read();
        int low = fis.read();
        if (high == -1 || low == -1) {
            return -1;
        }
        return (high << 8) | low;
    }

    public int signExtend(int x, int bit_count) {
        if (((x >> (bit_count - 1)) & 1) != 0) {
            x |= (0xFFFF << bit_count);
        }
        return x;
    }

    public void updateFlags(int r) {
        if (registers[r] == 0) {
            registers[Register.COND.ordinal()] = Condition.FL_ZERO.getValue();
        } else if ((registers[r] >> 15) != 0) {
            registers[Register.COND.ordinal()] =
                Condition.FL_NEGATIVE.getValue();
        } else {
            registers[Register.COND.ordinal()] =
                Condition.FL_POSITIVE.getValue();
        }
    }

    public void add(int instruction) {
        int r0 = (instruction >> 9) & 0x7;
        int r1 = (instruction >> 6) & 0x7;
        int immediate_flag = (instruction >> 5) & 0x1;

        if (immediate_flag != 0) {
            int immediate_value = signExtend(instruction & 0x1F, 5);
            registers[r0] = (registers[r1] + immediate_value) & 0xFFFF;
        } else {
            int r2 = instruction & 0x7;
            registers[r0] = (registers[r1] + registers[r2]) & 0xFFFF;
        }

        updateFlags(r0);
    }

    public void and(int instruction) {
        int r0 = (instruction >> 9) & 0x7;
        int r1 = (instruction >> 6) & 0x7;
        int immediate_flag = (instruction >> 5) & 0x1;

        if (immediate_flag != 0) {
            int immediate_value = signExtend(instruction & 0x1F, 5);
            registers[r0] = (registers[r1] & immediate_value) & 0xFFFF;
        } else {
            int r2 = instruction & 0x7;
            registers[r0] = (registers[r1] & registers[r2]) & 0xFFFF;
        }

        updateFlags(r0);
    }

    public void not(int instruction) {
        int r0 = (instruction >> 9) & 0x7;
        int r1 = (instruction >> 6) & 0x7;

        registers[r0] = (~registers[r1]) & 0xFFFF;

        updateFlags(r0);
    }

    public void branch(int instruction) {
        int program_counter_offset = signExtend(instruction & 0x1FF, 9);
        int condition_flag = (instruction >> 9) & 0x7;

        if ((condition_flag & registers[Register.COND.ordinal()]) != 0) {
            registers[Register.PC.ordinal()] = (registers[Register.PC.ordinal()] + program_counter_offset) & 0xFFFF;
        }
    }

    public void jump(int instruction) {
        int r1 = (instruction >> 6) & 0x7;

        registers[Register.PC.ordinal()] = registers[r1] & 0xFFFF;
    }

    public void jumpRegister(int instruction) {
        int long_flag = (instruction >> 11) & 1;
        registers[Register.R7.ordinal()] = registers[Register.PC.ordinal()];

        if (long_flag != 0) {
            int long_program_counter_offset = signExtend(
                instruction & 0x7FF,
                11
            );
            registers[Register.PC.ordinal()] = (registers[Register.PC.ordinal()] + long_program_counter_offset) & 0xFFFF;
        } else {
            int r1 = (instruction >> 6) & 0x7;
            registers[Register.PC.ordinal()] = registers[r1] & 0xFFFF;
        }
    }

    public void load(int instruction) {
        int r0 = (instruction >> 9) & 0x7;
        int program_counter_offset = signExtend(instruction & 0x1FF, 9);

        registers[r0] = memoryRead(
            (registers[Register.PC.ordinal()] + program_counter_offset) & 0xFFFF
        );
        updateFlags(r0);
    }

    public void loadRegister(int instruction) {
        int r0 = (instruction >> 9) & 0x7;
        int r1 = (instruction >> 6) & 0x7;

        int offset = signExtend(instruction & 0x3F, 6);

        registers[r0] = memoryRead((registers[r1] + offset) & 0xFFFF);
        updateFlags(r0);
    }

    public void loadIndirect(int instruction) {
        int r0 = (instruction >> 9) & 0x7;
        int program_counter_offset = signExtend(instruction & 0x1FF, 9);

        int address = memoryRead((registers[Register.PC.ordinal()] + program_counter_offset) & 0xFFFF);
        registers[r0] = memoryRead(address & 0xFFFF);
        updateFlags(r0);
    }

    public void loadEffectiveAddress(int instruction) {
        int r0 = (instruction >> 9) & 0x7;
        int program_counter_offset = signExtend(instruction & 0x1FF, 9);

        registers[r0] =
            (registers[Register.PC.ordinal()] + program_counter_offset) & 0xFFFF;
        updateFlags(r0);
    }

    public void store(int instruction) {
        int r0 = (instruction >> 9) & 0x7;
        int program_counter_offset = signExtend(instruction & 0x1FF, 9);

        memoryWrite(
            (registers[Register.PC.ordinal()] + program_counter_offset) & 0xFFFF,
            registers[r0]
        );
    }

    public void storeIndirect(int instruction) {
        int r0 = (instruction >> 9) & 0x7;
        int program_counter_offset = signExtend(instruction & 0x1FF, 9);

        memoryWrite(
            memoryRead(
                (registers[Register.PC.ordinal()] + program_counter_offset) & 0xFFFF
            ) & 0xFFFF,
            registers[r0]
        );
    }

    public void storeRegister(int instruction) {
        int r0 = (instruction >> 9) & 0x7;
        int r1 = (instruction >> 6) & 0x7;
        int offset = signExtend(instruction & 0x3F, 6);

        memoryWrite((registers[r1] + offset) & 0xFFFF, registers[r0]);
    }

    public void memoryWrite(int address, int value) {
        memory[address] = value;
    }

    private boolean checkKey() {
        try {
            return System.in.available() > 0;
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getChar() {
        try {
            int c = System.in.read();
            if (c == -1) {
                running = false;
                return 0;
            }
            return c;
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int memoryRead(int address) {
        if (address == KEYBOARD_STATUS_REGISTER) {
            if (checkKey()) {
                memory[KEYBOARD_STATUS_REGISTER] = (1 << 15);
            } else {
                memory[KEYBOARD_STATUS_REGISTER] = 0;
            }
        } else if (address == KEYBOARD_DATA_REGISTER) {
            memory[KEYBOARD_STATUS_REGISTER] = 0;
            memory[KEYBOARD_DATA_REGISTER] = getChar();
        }
        return memory[address];
    }

    public void trap(int instruction) {
        int trapVector = instruction & 0xFF;
        Trap trap = Trap.fromInt(trapVector);

        switch (trap) {
            case TRAP_GETC:
                trapGetc();
                break;
            case TRAP_OUT:
                trapOut();
                break;
            case TRAP_PUTS:
                trapPuts();
                break;
            case TRAP_IN:
                trapIn();
                break;
            case TRAP_PUTSP:
                trapPutsp();
                break;
            case TRAP_HALT:
                running = false;
                break;
            default:
                break;
        }
    }

    public void trapGetc() {
        try {
            int c = System.in.read();
            if (c == -1) {
                running = false;
                return;
            }
            registers[Register.R0.ordinal()] = c;
            updateFlags(Register.R0.ordinal());
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void trapPuts() {
        int address = registers[Register.R0.ordinal()];
        int c;
        while ((c = memory[address]) != 0) {
            System.out.print((char) c);
            address++;
        }
        System.out.flush();
    }

    public void trapOut() {
        char c = (char) (registers[Register.R0.ordinal()] & 0xFF);
        System.out.print(c);
        System.out.flush();
    }

    public void trapIn() {
        System.out.println("Enter a character: ");

        try {
            int c = System.in.read();

            System.out.print((char) c);

            registers[Register.R0.ordinal()] = (int) c;
            updateFlags(Register.R0.ordinal());
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void trapPutsp() {
        int address = registers[Register.R0.ordinal()];
        int value;
        while ((value = memory[address]) != 0) {
            char c1 = (char) (value & 0xFF);
            System.out.print(c1);
            char c2 = (char) ((value >> 8) & 0xFF);
            if (c2 != 0) {
                System.out.print(c2);
            }
            address++;
        }
    }

    public void run() {
        running = true;
        registers[Register.PC.ordinal()] = (short) PC_START;

        while (running) {
            int instruction = memoryRead(registers[Register.PC.ordinal()] & 0xFFFF);
            registers[Register.PC.ordinal()] = (registers[Register.PC.ordinal()] + 1) & 0xFFFF;
            int opValue = instruction >> 12;
            Opcode op = Opcode.fromInt(opValue);
            switch (op) {
                case OP_BR:
                    branch(instruction);
                    break;
                case OP_ADD:
                    add(instruction);
                    break;
                case OP_LD:
                    load(instruction);
                    break;
                case OP_ST:
                    store(instruction);
                    break;
                case OP_JSR:
                    jumpRegister(instruction);
                    break;
                case OP_AND:
                    and(instruction);
                    break;
                case OP_LDR:
                    loadRegister(instruction);
                    break;
                case OP_STR:
                    storeRegister(instruction);
                    break;
                case OP_RTI:
                    // Unused
                    break;
                case OP_NOT:
                    not(instruction);
                    break;
                case OP_LDI:
                    loadIndirect(instruction);
                    break;
                case OP_STI:
                    storeIndirect(instruction);
                    break;
                case OP_JMP:
                    jump(instruction);
                    break;
                case OP_RES:
                    // Unused
                    break;
                case OP_LEA:
                    loadEffectiveAddress(instruction);
                    break;
                case OP_TRAP:
                    trap(instruction);
                    break;
                default:
                    System.out.println("Op Code Not Supported");
                    running = false;
                    break;
            }
        }
    }
}
