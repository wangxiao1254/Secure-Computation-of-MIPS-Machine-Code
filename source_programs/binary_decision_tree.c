#include <stdio.h>

int sfe_main(int *a, int *b, int l1, int l2) {
   int depth = a[0];
   int * tree = a+1;
   int index = 0;
   for(int i = 1; i < depth; ++i) {
      int right_branch = tree[index*2] <= b[tree[index*2+1]];
      index = 2*index+1;
      index += right_branch;
   }
   return tree[index*2]; 
}

/* Driver program to test above function */
int main()
{
   int a[] = {3, 1,0, 2,1, 3,2, 4,-1, 5,-1 ,6,-1, 7,-1};
   int b[] = {0, 2, 3};
   int num1=13, num2=3;
   int total = sfe_main(a, b, num1, num2);
   printf("%d", total);
   return 0;
}

