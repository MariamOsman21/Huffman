import java.util.Arrays;
import java.util.Comparator;

public class Solution {

    static class Activity {
        int st;
        int end;
        int weight;
        public Activity(int s, int e, int w) {
            st = s;
            end = e;
            weight = w;
        }
    }
    public int findMaxActivityWeightRecursive(Activity[] a, int i, int j , int dp[][]){
        if(dp[i][j] != -1)
            return dp[i][j];
        boolean isEmpty = true;
        for(int k = i+1; k < j; k++){
            if(a[k].st >= a[i].end && a[k].end <= a[j].st){
                isEmpty = false;
                int q = findMaxActivityWeightRecursive(a, i, k, dp) + findMaxActivityWeightRecursive(a, k, j, dp) + a[k].weight;
                dp[i][j] = Math.max(dp[i][j],q);
            }
        }
        if(isEmpty)
            dp[i][j] = 0;
        return dp[i][j];
    }

    public void findMaxActivityWeight(Activity[] a){
        int n = a.length;
        Arrays.sort(a, Comparator.comparingDouble((Activity p) -> p.end).thenComparingDouble(p -> p.st));
        int[][] dp = new int[n][n];
        for (int i = 0; i <n; i++){
            for (int j = 0; j < n ; j++){
                dp[i][j] = -1;
            }
        }
        findMaxActivityWeightRecursive(a, 0, a.length-1, dp);

        System.out.println(dp[0][n-1]);

    }


    public void findMaxActivityWeight2(Activity[] a){
        Arrays.sort(a, Comparator.comparingDouble((Activity p) -> p.end).thenComparingDouble(p -> p.st));
        int[] dp = new int[a.length];
        dp[0] = 0;

        for(int i = 1; i < a.length; i++){

            int indx = binarySearch(a, 0, i , a[i].st);
            int q = 0;
            if(indx != -1){
                q = dp[indx];
                //System.out.println("dp "+ dp[i] + " " + i);
            }
            dp[i] = Math.max(dp[i-1], q+ a[i].weight);
            System.out.println("dp "+ dp[i] + " " + i);

        }

        System.out.println(dp[a.length-1]);

    }

    public int binarySearch(Activity[] a, int st, int end, int key){

        if (st >= end)
            return -1;

        int mid = (st + end)/2;

        if (a[mid].end <= key)
            return mid;

        else if (a[mid].end > key)
            return binarySearch(a, st, mid-1, key);

        else
            return -1;

    }

    public static void main(String[] args) {
        Solution sol = new Solution();
        Activity a0 = new Activity(0,0,0);
        Activity a1 = new Activity(0, 2, 55);
        Activity a3 = new Activity(1,4, 1);
        Activity a2 = new Activity(2 ,5, 2);
        Activity a5= new Activity(4, 5, -500000000);
        Activity a4 = new Activity(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        Activity[] act = {a0,  a1, a2, a3, a5, a4};

        sol.findMaxActivityWeight2(act);


    }
}
