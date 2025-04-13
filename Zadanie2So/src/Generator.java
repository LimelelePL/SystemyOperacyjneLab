import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {

    public List<Process> generateRandom(int maxArrivalTime, int maxCylinder, int count, int maxDeadline) {
        List<Process> processes = new ArrayList<>();
        Random random = new Random();
        processes.add(new Process("P0", 0, 1, 20000, true));
        for (int i = 1; i < count; i++) {
            int arrivalTime = random.nextInt(maxArrivalTime);
            int cylinder = random.nextInt(maxCylinder);
            int deadline = random.nextInt(maxDeadline*200);
            String name = "P" + i;
            boolean isRealTime = random.nextBoolean();

            processes.add(new Process(name, arrivalTime, cylinder, deadline, isRealTime));
        }
        return processes;
    }

    public List<Process> generateInOneSide(int maxArrivalTime, int maxCylinder, int count, int maxDeadline) {
        List<Process> processes = new ArrayList<>();
        Random random = new Random();
        processes.add(new Process("P0", 0, 1, 20000, true));
        for (int i = 1; i < count; i++) {
            int arrivalTime = random.nextInt(maxArrivalTime);
            int cylinder = random.nextInt(maxCylinder - 20, maxCylinder);
            int deadline = random.nextInt(maxDeadline*200);
            String name = "P" + i;
            boolean isRealTime = random.nextBoolean();
            processes.add(new Process(name, arrivalTime, cylinder, deadline, isRealTime));
        }
        return processes;
    }

    public List<Process> generateInBothEdges(int maxArrivalTime, int maxCylinder, int count, int maxDeadline) {
        List<Process> processes = new ArrayList<>();
        Random random = new Random();
        int rightCount = (int) (count * 0.75);
        int leftCount = count - rightCount;
        processes.add(new Process("P0", 0, 1, 20000, true));
        for (int i = 1; i < leftCount; i++) {
            int arrivalTime = random.nextInt(maxArrivalTime);
            int cylinder = random.nextInt(0, 20);
            int deadline = random.nextInt(maxDeadline*200);
            String name = "P" + i;
            boolean isRealTime = random.nextBoolean();
            processes.add(new Process(name, arrivalTime, cylinder, deadline, isRealTime));
        }

        for (int i = 0; i < rightCount; i++) {
            int arrivalTime = random.nextInt(maxArrivalTime);
            int cylinder = random.nextInt(maxCylinder - 20, maxCylinder);
            int deadline = random.nextInt(maxDeadline*200);
            String name = "P" + (i + leftCount);
            boolean isRealTime = random.nextBoolean();
            processes.add(new Process(name, arrivalTime, cylinder, deadline, isRealTime));
        }
        return processes;
    }

    public List<Process> generateBehindHead(int maxArrivalTime, int maxCylinder, int count, int maxDeadline, int currentHead, boolean goingUp) {
        List<Process> processes = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            int arrivalTime = random.nextInt(maxArrivalTime);
            int cylinder;

            if (goingUp) {
                if (currentHead > 0) {
                    cylinder = random.nextInt(currentHead);
                } else {
                    cylinder = 0;
                }
            } else {
                if (currentHead < maxCylinder) {
                    cylinder = random.nextInt(maxCylinder - currentHead) + currentHead;
                } else {
                    cylinder = maxCylinder;
                }
            }

            int deadline = random.nextInt(maxDeadline * 200);
            String name = "P" + i;
            boolean isRealTime = random.nextBoolean();
            processes.add(new Process(name, arrivalTime, cylinder, deadline, isRealTime));
        }
        return processes;
    }
}

