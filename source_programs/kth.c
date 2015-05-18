#include <stdio.h>
int sfe_main(int *a, int *b, int l, int l2) {
		  int i;
		  for(i = 0; i < l; ++i)
					 a[i] +=b[i];
		  int left = 0;
		  int right = l-1;
		  int n = 3;
		  if (left == right)
					 return a[left];
		  while(1) {
					 int  pi = left;
					 int pv = a[pi];
					 int tmp = a[pi];
					 a[pi] = a[right];
					 a[right]=tmp;
					 int si = left;
					 int i;
					 for(i = left; i <= right-1; ++i){
								if (a[i] < pv) {
										  int tmp = a[si];
										  a[si] = a[i];
										  a[i]=tmp;
										  si++;
								}
					 }
					 tmp = a[pi];
					 a[pi] = a[right];
					 a[right]=tmp;
					 pi=si;
					 if(n == pi)
								return a[pi];
					 else if(n < pi)
								right = pi-1;
					 else left = pi+1;
		  }
}

/* Driver program to test above function */
int main()
{
		  int a[] = {4, 1, 9, 0, 3, 2, 8, 7, 6, 5};
		  int b[] = {0,0,0,0,0,0,0,0,0,0};
		  int total = sfe_main(a, b, 10, 10);
		  printf("%d", total);
		  return 0;
}

