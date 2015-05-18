import sys
content = '\n\
alice_input_file alice.txt\n\
bob_input_file bob.txt\n\
alice_input_size $1\n\
bob_input_size 1\n\
multiple.banks true\n\
mode REAL\n\
stack_frame_size 32\n\
binary.reader.path = /opt/mipsel/usr/bin/mipsel-linux-gnu-llvm-objdump\n\
entry.point = sfe_main\n\
function.load.list = sfe_main main\n\
emulator.server.dir = .\n\
emulator.client.dir = .\n\
Server.address localhost\n\
Server.port 54321\n\
'

print content.replace('$1', sys.argv[1])
