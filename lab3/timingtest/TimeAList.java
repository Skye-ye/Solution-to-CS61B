package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.print("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        AList<Integer> Ns = new AList<>();
        AList<Double> times = new AList<>();
        AList<Integer> opCounts = new AList<>();
        int[] N = {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000};
        for (int k : N) {
            Ns.addLast(k);
        }
        for (int i = 0; i < Ns.size(); i++) {
            Stopwatch sw = new Stopwatch();
            AList<Integer> test = new AList<>();
            for (int j = 0; j < Ns.get(i); j++) {
                test.addLast(j);
            }
            double timeInSeconds = sw.elapsedTime();
            times.addLast(timeInSeconds);
            opCounts.addLast(N[i]);
        }
        printTimingTable(Ns, times, opCounts);
    }
}
