#!/bin/bash
CP='./bin:./lib/*:../FlexSC/bin/:../FlexSC/lib/*:./dist/*'
   java  -Xmx2g -cp $CP -Demulator.properties=emulator.properties mips.MipsEmulatorImpl  $1  $2
