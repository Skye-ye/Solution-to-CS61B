public class BreakContinue {
  public static void windowPosSum(int[] a, int n) {
    for (int i = 0, upper = a.length; i < upper; i++){
      if (a[i] < 0){
        continue;
      } else {
        for (int j = 1; j <= n; j++){
          if (i + j >= upper){
            break;
          } else {
            a[i] += a[i + j];
          }
        }
      }
    }
  }

  public static void main(String[] args) {
    int[] a = {1, 2, -3, 4, 5, 4};
    int n = 3;
    windowPosSum(a, n);

    // Should print 4, 8, -3, 13, 9, 4
    System.out.println(java.util.Arrays.toString(a));
  }
}