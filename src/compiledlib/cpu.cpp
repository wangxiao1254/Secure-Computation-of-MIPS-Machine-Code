//#include <cbmc-gc.h>
#define OP_CODE_R 1
#define OP_CODE_I 2
#define OP_CODE_OTHERS 3
#define true 1
#define false 0
#define PRINT 0

// OP_CODE_I
#define OP_ADDIU 9
#define OP_ANDI 12
#define OP_LUI 15
#define OP_SLTI 10

#define OP_BNE 5
#define OP_BEQ 4
#define OP_BAL 1
#define OP_JAL 3

//OP_CODE_R
#define FUNCT_SLL 0
#define FUNCT_SRL 2
#define FUNCT_SRA 3
#define FUNCT_SLLV 6
#define FUNCT_JR 8
#define FUNCT_JALR 9
#define FUNCT_ADD 20
#define FUNCT_ADDU 33
#define FUNCT_SUBU 35
#define FUNCT_OR 37
#define FUNCT_XOR 38
#define FUNCT_SLT 42

struct CPU{};
int32 SLL(int32 m, int32 n) = native intLib.leftPrivateShift;
int32 SRL(int32 m, int32 n) = native intLib.rightPrivateShift;
int32 SRA(int32 m, int32 n) = native intLib.SRA;
typedef Register = native Register;
int32 Register.read(int32 index, public int32 mask) = native read;
void Register.write(int32 index, int32 content, public int32 mask, int1 terminationBit) = native write;


//multi(x,y,0) is for unsigned mul, (x,y,1) is for signed.
void multi(int32 m, int32 n, int1 sign, int32 high, int32 low) = native intLib.multiplyMips;
// usage
//  int32 h,l;int1 sign = 1;//sign to indicate if you want to use Mult or MultiU
//  multi(rs,rt,sign, h, l);

int2 CPU.checkType(int32 opcode) {
   int2 ret;
   if (opcode == 0)
      ret = OP_CODE_R;
   else if (opcode ==  0x09|| opcode == 0x0C || opcode == 0x0f)
      ret = OP_CODE_I;
   else ret = OP_CODE_OTHERS;
   return ret;
}
int32 CPU.function(Register reg, secure int32 inst, secure int32 pc, int1 terminationBit, public int32[public 2]masks) {
public int32 ff = 0xffffffff;
   int32 op = inst >> 26;
   int32 rt = ((inst << 11)>>27);
   int32 rs = ((inst << 6) >> 27);
   int32 rd = ((inst << 16)>>27);
   int32 shamt = ((inst << 21)>>27);
   int32 reg_rs, reg_rt, reg_rd;
   int32 unsignExt = ((inst << 16)>>16);
   int32 zeroExt = unsignExt;
   int32 funct = (inst << 26) >> 26;

   if (unsignExt >> 15 != 0)
      unsignExt = unsignExt + 0xffff0000;

   //printf("op: %d, inst: %d", op, ((inst << 26)>>26));

  reg_rs = reg.read(rs,ff);
   reg_rt = reg.read(rt,ff);

   int2 op_type = this.checkType(op);
   if(op_type == OP_CODE_I) {
      if (op == OP_ADDIU) {
         reg_rt = reg_rs + (unsignExt);
      } else if (op == OP_ANDI) {
         reg_rt = reg_rs & zeroExt;
      } else if (op == OP_LUI) {
		reg_rt = (zeroExt << 16);
      } else if (op == OP_SLTI) {
    	  if (reg_rs < unsignExt)
    		  reg_rt = 1;
    	  else reg_rt = 0;
      }
      
   }
   else if (op_type == OP_CODE_R) {//R type
      if (funct == FUNCT_ADDU) {
         reg_rd = reg_rs + reg_rt;
      } else if (funct == FUNCT_XOR) {
         reg_rd = reg_rs ^ reg_rt;
      } else if (funct == FUNCT_SLT) {
         if (reg_rs < reg_rt) reg_rd = 1;
         else reg_rd = 0;
      } else if (funct == FUNCT_SUBU) {
         reg_rd = reg_rs - reg_rt;
      } else if (funct == FUNCT_SRL){
      reg_rd = SRL(reg_rt, shamt);//(reg_rt >> shamt);    
      } else if (funct == FUNCT_SRA){
    	  reg_rd = SRA(reg_rt, shamt);
	} else if (funct == FUNCT_SLL){
      reg_rd = SLL(reg_rt, shamt);//(reg_rt << shamt);    
      } else if (funct == FUNCT_OR){
         reg_rd = (reg_rt | reg_rs);    
      } else if (funct == FUNCT_JALR){
    	  reg_rd = pc + 4;
    	  	//pc = reg_rs;
      }
   }
   else {
int32 oldPC = pc;
      if (op == OP_JAL ) {
         pc = ((inst << 6) >> 6);
	 
      } 
      else if ( op == OP_BAL) {
         if (reg_rt >= 0)
            pc = pc + (unsignExt << 2);
         // Dnote: I think we don't want this line?  Is this an unconditional branch?
         //pc = pc + 4;
      }

      if(op == OP_JAL || op == OP_BAL) {
            	int32 n31 = 31;
      	int32 tmpdata = oldPC + 8;
         reg.write(n31, tmpdata, ff, terminationBit);
         }
   }


   // then process pc
   //   if ((op == 0 && funct == FUNCT_JR) || (op == 0 && funct == FUNCT_JALR)) {
   if (op == 0 && funct == FUNCT_JR) {
      pc = reg_rs;
   } else if (op == 3) { // OP_JAL
      pc = (inst << 6) >> 6;
   } else if ((op == OP_BNE && reg_rs != reg_rt) || (op == OP_BEQ && reg_rs == reg_rt)) { //OP_BNE and OP_BEQ
      pc = pc + 4 +(unsignExt << 2);
   } else
      pc = pc + 4;
      
	//only rt or rd will be update in one cycle
	int32 to_write_index = rt;
	int32 to_write_content = reg_rt;
    if(op_type == OP_CODE_R){
    	to_write_index = rd;
    	to_write_content = reg_rd;
    }
        reg.write(to_write_index, to_write_content, ff, terminationBit);
	//reg[rret] = reg_ret;		
    return pc;
}
