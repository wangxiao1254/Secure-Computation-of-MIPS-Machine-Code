rm -f src/compiledlib/dov/Cpu_*&&
cp src/com/appcomsci/mips/cpu/cpu.txt bin/com/appcomsci/mips/cpu/cpu.txt&&

./scripts/cpuFactory $1 &&
./scripts/compileJava.sh

