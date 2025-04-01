
import java.util.*;

public class GenerateTests {
        public static List<Process> generateTests(int n, double lambda, int maxBurstTime) {
            List<Process> processes = new ArrayList<>();
            Random rand = new Random();

            processes.add(new Process("Process0", 0, rand.nextInt(maxBurstTime) + 1));
            int arrivalTime=0;

            for (int i = 1; i < n; i++) {
                String name = "Process" + i;
                arrivalTime += (int) (-Math.log(1 - rand.nextDouble()) / lambda);
                int burstTime = rand.nextInt(maxBurstTime) + 1;
                processes.add(new Process(name, arrivalTime, burstTime));
            }
            return processes;
        }
    }

