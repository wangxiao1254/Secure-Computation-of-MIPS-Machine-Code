#include <stdio.h>
int sfe_main(int *a, int *b, int l, int l2) {
   int key = b[0], imin = 0, imax = l-1;
   while (imax >= imin) {
      int imid = (imin+ imax)/2;
      if (a[imid] >= key)
         imax = imid - 1;
      else
         imin = imid + 1;
   }
   return imin;
}

/* Driver program to test above function */
int main()
{
		  int i;
		  for(i = 0; i < 18; ++i) {
					 int a[] = {1,3,5,7,9,11,13,15}, b[]={i};
					 int la, lb;
					 int total = sfe_main(a, b, 8, lb);
					 printf("%d,%d\n", i, total);
		  }
		  return 0;
}

