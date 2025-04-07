import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Generator {

    public List<Process> generate(int maxArrivalTime, int maxCylinder, int count, int maxDeadline) {
        List<Process> processes = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            int arrivalTime = random.nextInt(maxArrivalTime);
            int cylinder = random.nextInt(maxCylinder);
            int deadline = random.nextInt(maxDeadline);
            String name = "P" + i;

            processes.add(new Process(name, arrivalTime, cylinder, deadline));
        }
        return processes;
    }
}
