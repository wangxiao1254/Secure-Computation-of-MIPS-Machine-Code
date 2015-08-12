#include <stdio.h>
#define MAX 10
int sfe_main(int *a, int *b, int l1, int l2) {
   int vis[MAX];
   int dis[MAX];
   dis[b[0]] = 0;
   int * e = a + 1;
   int n = a[0];
   for(int i = 0; i < n; ++i) {
      int bestj = -1, bestdis = -1;
      for(int j=0; j<n; ++j) {
         if( (!vis[j]) & dis[j] < bestdis) {
            bestj = j;
            bestdis = dis[j];
         }
      }
      vis[bestj] = 1;
      for(int j=0; j<n; ++j) {
         int newDis = bestdis + e[bestj*n+j];
         if(newDis < dis[j])
            dis[j] = newDis;
      }
   }
   return dis[b[1]];
}

/* Driver program to test above function */
int main()
{
   int a[20];
   int b[20];
   int num1=20, num2=20;
   int total = sfe_main(a, b, num1, num2);
   return 0;
}

