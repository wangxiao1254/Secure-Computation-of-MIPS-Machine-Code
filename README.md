installation:

1. install java 8 if needed: 
`sudo ./script/install_java.sh`
2. install mipsel:
`sudo ./script/install_mipsel.sh`


Compilation:
1. Compile the c code to binary:
`./script/compileBinary.sh code.c`

2. Compile the code to CPUs:
`./compile.sh [binary]`

Run:
1. put input to alice.txt and bob.txt, and put the length of the input to emulator.properties

2. edit the server address to the correct address

3. for garbler:  ./run.sh [binary] gen
   for evalutor: ./run.sh [binary] eva
