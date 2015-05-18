CP="../FlexSC/bin:bin:lib/*"

mkdir -p bin
find . -name "*.java" | grep -v SimpleBinaryReader > source.txt;
javac -cp "$CP" -d bin @source.txt;
rm source.txt

cp src/com/appcomsci/mips/cpu/cpu.txt bin/com/appcomsci/mips/cpu/cpu.txt 
