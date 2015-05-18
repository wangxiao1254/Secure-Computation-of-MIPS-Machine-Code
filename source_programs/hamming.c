#include <stdio.h>

int sfe_main(int *a, int *b, int l) {
/*	 asm volatile("addi $9 , $5 , 0 ");
	 asm volatile("addi $2 , $0 , 0 ");
	 asm volatile("1:lw $7 , 0 ( $4 )");
	 asm volatile("addi $4 , $4 , 4");
	 asm volatile("lw $8, 0 ( $5 ) ");
	 asm volatile("addi $5 , $5 , 4");
	 asm volatile("bne $7 , $8 , 2f");
	 asm volatile("addi $2 , $2 , 1 ");
	 asm volatile("2: bne $4 , $9 , 1b");
*/
		  int *e=a+l;
		  l = 0;
		  while(a<e){ 
				if(*a++==*b++)++l;
		  }
		  return l;
}

/* Driver program to test above function */
int main()
{
		  int a[20];
		  int b[20];
		  int num1=20;
		  int total = sfe_main(a, b, num1);
		  return 0;
}

