./scripts/compileBinary.sh source_programs/$1 &&
./compile.sh source_programs/$1&&
./run.sh source_programs/$1 gen & ./run.sh source_programs/$1 eva
