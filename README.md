# Java LC 3

LC 3 is an educational virtual machine architecture.

## Registers
LC3 has ten registers, eight of them are general purpose registers, one stores the program counter and another sets the condition flag.

## Memory
LC3 is a 16 bit virtual machine, so the maximum size of the addressable memory is 16<sup>2</sup>.  I've defined both the registers and memory as arrays.

## Compiling and Running the Project

```sh
javac -d out *.java
java -cp out com.chuckgreenman.lc3.LC3
```
