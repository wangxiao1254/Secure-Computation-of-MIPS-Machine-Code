#define OP_LW 35
#define OP_SW 43
#define OP_LB 32
typedef OMap = native SecureMap;
int32 OMap.read(int32 id) = native read;

typedef Register = native Register;
int32 Register.read(int32 index, public int32 mask) = native read;
void Register.write(int32 index, int32 data, public int32 mask, int1 terminationBit) = native write;

typedef Memory = native Memory;
int32 Memory.read(int32 index, public int32 operationMask) = native read;
void Memory.write(int32 index, int32 data, int1 terminationBit, public int32 operationMask) = native conditionalWrite;


struct MEM{};

int32 MEM.getInst(OMap instBank, int32 pc, public int32 pcOffset) {
	//int32 index = (pc-pcOffset) >> 2;
	int32 newInst = instBank.read(pc);
	return newInst;
}

void MEM.func(Register reg,
	  Memory mem,
      int32 inst,
      public int32 dataOffset, int1 terminationBit, public int32 operationMask, public int32 masks0, public int32 masks1) {
   //int32 index = (pc-pcOffset) >> 2;
   //int32 newInst = mem.read(index);

   int32 rt = (inst << 11)>>27;
   int32 rs = (inst << 6) >> 27;
   int32 unsignExt = ((inst << 16)>>16);
   if (unsignExt >> 15 == 1)
      unsignExt = unsignExt + 0xffff0000;
   int32 op = (inst >> 26);

   int32 tmpAddress = reg.read(rs, masks0) + unsignExt - dataOffset;
   int32 tmpindex = (tmpAddress)>>2;
   int32 mem_tmp_r = mem.read(tmpindex, operationMask);
   int32 reg_rt_r = reg.read(rt, masks1);
   int32 mem_tmp_w = mem_tmp_r;
   int32 reg_rt_w = reg_rt_r;
   	   int32 tempRT = mem_tmp_r;
	   int32 byteShiftTwo = ((tmpAddress << 30) >> 31);
	   int32 byteShiftOne = ((tmpAddress << 31) >> 31);
   
   if(op == OP_LW)
      reg_rt_w = mem_tmp_r;
   else if(op == OP_SW){
      mem_tmp_w = reg_rt_r;
   } else if(op == OP_LB) {
	   if (byteShiftTwo != 0 && byteShiftOne != 0)
		   tempRT = ((tempRT << 24) >> 24);
	   else if (byteShiftTwo != 0 && byteShiftOne == 0)
		   tempRT = ((tempRT << 16) >> 24);
	   else if (byteShiftTwo == 0 && byteShiftOne != 0)
	   		   tempRT = ((tempRT << 8) >> 24);
	   else if (byteShiftTwo == 0 && byteShiftOne == 0)
	   		   tempRT = (tempRT >> 24);
	   reg_rt_w = tempRT;
   }
   reg.write(rt, reg_rt_w, masks1, terminationBit);
   mem.write(tmpindex, mem_tmp_w, terminationBit, operationMask);
   //return newInst;
}
