# Java LC 3

LC 3 is an educational virtual machine architecture.  I implemented LC 3 in Java to get a better understanding of the language.

## Compiling and Running the Project

```sh
javac -d . *.java
java com.chuckgreenman.lc3.LC3 [image file path]
```

### Example

Play 2048 using WASD controls, object file from Justin Meiners.

```sh
java com.chuckgreenman.lc3.LC3 test_object_files/2048.obj
```

## Java Specific Notes

- I realized late in the process that Java promotes shorts to full ints when you do binary math on them.  I've added some more masking to account for this.
- Enums are objects.  In a lot of languages enums are int backed, unless you assign them a value, but in Java they are objects.  You can kind of fake int backed enums with `.ordinal()` but, that fully depends on an enum value's place in the enum, so it's not suitible for writing to a database.  In a virtual machine however, where we are more interested with relative position during a specific run, it works well.

## Registers
LC3 has ten registers, eight of them are general purpose registers, one stores the program counter and another sets the condition flag.

## Memory
LC3 is a 16 bit virtual machine, so the maximum size of the addressable memory is 16<sup>2</sup>.  I've defined both the registers and memory as arrays.

## Instructions

**Branch** - Branching is how we achieve control flow at the assembly level. The instruction contains a 9-bit PC offset and 3 condition bits (negative, zero, positive). The branch is taken if any specified condition matches the current condition flag.

**Add** - Adds two values and stores the result in a destination register. Supports two modes: register mode adds two registers, immediate mode adds a register and a 5-bit sign-extended constant.

**Load** - Loads a value from memory into a register. The memory address is computed by adding a 9-bit sign-extended offset to the program counter.

**Store** - Stores a register value into memory. The memory address is computed by adding a 9-bit sign-extended offset to the program counter.

**Jump Register** - Used for subroutine calls. Saves the current PC to R7 (return address), then jumps to an address. Supports two modes: register mode jumps to an address in a register, long mode adds an 11-bit offset to PC.

**And** - Performs bitwise AND on two values and stores the result in a destination register. Like Add, supports both register mode and immediate mode with a 5-bit constant.

**Load Register** - Loads a value from memory into a register. The memory address is computed by adding a 6-bit sign-extended offset to a base register.

**Store Register** - Stores a register value into memory. The memory address is computed by adding a 6-bit sign-extended offset to a base register.

**Not** - Performs bitwise NOT on a register value and stores the result in a destination register. This is the only bitwise operation that operates on a single operand.

**Load Indirect** - Loads a value from memory using indirect addressing. First reads an address from memory (PC + offset), then loads the value at that address into a register.

**Store Indirect** - Stores a register value to memory using indirect addressing. First reads an address from memory (PC + offset), then stores the value at that address.

**Jump** - Unconditionally jumps to the address contained in a register. Also used for subroutine returns (RET) when jumping to R7.

**Load Effective Address** - Computes an address (PC + offset) and stores it in a register. Does not access memory, just calculates the address.

**Trap** - Invokes a system call. The trap vector specifies which routine to execute: GETC (read character), OUT (write character), PUTS (write string), IN (prompt and read), PUTSP (write packed string), or HALT (stop execution).
