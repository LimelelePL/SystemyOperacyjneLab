import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {


    private Random random = new Random();

    // Metoda pomocnicza generująca czas według rozkładu wykładniczego
    private int getExponentialArrivalTime(double lambda) {
        return (int) Math.round(-Math.log(1 - random.nextDouble()) / lambda);
    }

    public List<Process> generateRandom(double lambda, int maxCylinder, int count, int maxDeadline) {
        List<Process> processes = new ArrayList<>();
        processes.add(new Process("P0", 0, 1, 20000, true));

        int currentArrivalTime = 0;
        for (int i = 1; i < count; i++) {
            currentArrivalTime += getExponentialArrivalTime(lambda);

            int cylinder = random.nextInt(maxCylinder);
            int deadline = random.nextInt(maxDeadline * 200);
            String name = "P" + i;
            boolean isRealTime = random.nextBoolean();

            processes.add(new Process(name, currentArrivalTime, cylinder, deadline, isRealTime));
        }
        return processes;
    }

    public List<Process> generateInOneSide(double lambda, int maxCylinder, int count, int maxDeadline) {
        List<Process> processes = new ArrayList<>();
        processes.add(new Process("P0", 0, 1, 20000, true));

        int currentArrivalTime = 0;
        for (int i = 1; i < count; i++) {
            currentArrivalTime += getExponentialArrivalTime(lambda);

            int cylinder = random.nextInt(maxCylinder - 20, maxCylinder);
            int deadline = random.nextInt(maxDeadline * 200);
            String name = "P" + i;
            boolean isRealTime = random.nextBoolean();

            processes.add(new Process(name, currentArrivalTime, cylinder, deadline, isRealTime));
        }
        return processes;
    }

    public List<Process> generateInBothEdges(double lambda, int maxCylinder, int count, int maxDeadline) {
        List<Process> processes = new ArrayList<>();
        int rightCount = (int) (count * 0.75);
        int leftCount = count - rightCount;

        processes.add(new Process("P0", 0, 1, 20000, true));

        int currentArrivalTime = 0;

        // Lewa strona
        for (int i = 1; i < leftCount; i++) {
            currentArrivalTime += getExponentialArrivalTime(lambda);

            int cylinder = random.nextInt(0, 20);
            int deadline = random.nextInt(maxDeadline * 200);
            String name = "P" + i;
            boolean isRealTime = random.nextBoolean();

            processes.add(new Process(name, currentArrivalTime, cylinder, deadline, isRealTime));
        }

        // Prawa strona
        for (int i = leftCount; i < count; i++) {
            currentArrivalTime += getExponentialArrivalTime(lambda);

            int cylinder = random.nextInt(maxCylinder - 20, maxCylinder);
            int deadline = random.nextInt(maxDeadline * 200);
            String name = "P" + i;
            boolean isRealTime = random.nextBoolean();

            processes.add(new Process(name, currentArrivalTime, cylinder, deadline, isRealTime));
        }

        return processes;
    }

    public List<Process> generateBehindHead(int maxCylinder, int count, int maxDeadline, int currentHead, boolean goingUp) {
        List<Process> processes = new ArrayList<>();
        Random random = new Random();

        int cylinder=0;
        int arrivalTime=0;
        int last=0;

        for (int i = 1; i < count/(2*maxCylinder); i++) {

        for (int j = 1; j < maxCylinder; j++) {

            int deadline = random.nextInt(maxDeadline * 200);
            String name = "P" + i+j;
            boolean isRealTime = random.nextBoolean();

            if (goingUp) {
                cylinder = random.nextInt(2);
                arrivalTime=j+last;
                processes.add(new Process(name, arrivalTime, cylinder, deadline, isRealTime));
                }
            }

        last+=maxCylinder;

        for (int j=maxCylinder; j>0; j--) {

            int deadline = random.nextInt(maxDeadline * 200);
            String name = "P" + i+j+last;
            boolean isRealTime = random.nextBoolean();

            cylinder = random.nextInt(maxCylinder - 2, maxCylinder);
            arrivalTime=j+last;
            processes.add(new Process(name, arrivalTime, cylinder, deadline, isRealTime));
        }

        }

        return processes;
    }

}

