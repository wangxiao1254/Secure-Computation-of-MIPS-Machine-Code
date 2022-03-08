FROM debian:stretch
RUN apt-get update
RUN apt-get install -y build-essential openjdk-8-jdk git gcc-mipsel-linux-gnu g++-mips-linux-gnu llvm 
RUN mkdir -p /opt/mipsel/usr/bin/
RUN ln -s /usr/bin/mipsel-linux-gnu-gcc /opt/mipsel/usr/bin/mipsel-linux-gnu-gcc
RUN ln -s /usr/bin/llvm-objdump /opt/mipsel/usr/bin/mipsel-linux-gnu-llvm-objdump
RUN git clone https://github.com/wangxiao1254/Secure-Computation-of-MIPS-Machine-Code.git
WORKDIR "/Secure-Computation-of-MIPS-Machine-Code"
RUN ./scripts/compileBinary.sh ./source_programs/set_intersection.c
RUN ./scripts/compileJava.sh
RUN rm -f src/compiledlib/dov/Cpu_*
RUN cp src/com/appcomsci/mips/cpu/cpu.txt bin/com/appcomsci/mips/cpu/cpu.txt
RUN ./scripts/cpuFactory a.out
RUN ./run.sh a.out gen & ./run.sh a.out eva
