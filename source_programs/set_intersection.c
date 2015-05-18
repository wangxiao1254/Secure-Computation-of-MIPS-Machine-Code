#include <stdio.h>
#ifndef SIZE
#define SIZE 50
#endif

int sfe_main(int arr1[], int arr2[], int m, int n)
{
  int i = 0, j = 0, total=0;
  while(i < m && j < n)
  {
    if(arr1[i] < arr2[j]){
      i++;
    }
    else if(arr2[j] < arr1[i]){
      j++;
    }
    else /* if arr1[i] == arr2[j] */
    {
      i++;
      total++;
    }
  }
  return total;
}
 
/* Driver program to test above function */
int main()
{
  int * arr1, *arr2;
  sfe_main(arr1, arr2, 10, 10);
  return 0;
}

