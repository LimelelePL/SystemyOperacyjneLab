import java.util.ArrayList;
import java.util.List;

public abstract class Algorithm {
    private ArrayList<CPU> cpus;
    private int N;
    private float P; // Próg w procentach (0-100%)

    private int time;
    private final List<Float> averageLoadsOverTime = new ArrayList<>();
    private final List<Float> deviationsOverTime = new ArrayList<>();

    private double avgCPUsUtilization;
    private double standardDeviationCPUsUtilization;
    private int askForMigrationCount;
    private int migrationCount;
    private int rejectedTaskCount;   // nowy licznik odrzuconych zadań

    public Algorithm(ArrayList<CPU> cpus, int N, float P) {
        this.cpus = cpus;
        this.N = N;
        this.P = P;
        this.time = 0;

        this.avgCPUsUtilization = 0.0;
        this.askForMigrationCount = 0;
        this.migrationCount = 0;
        this.rejectedTaskCount = 0;
        this.standardDeviationCPUsUtilization = 0.0;
    }

    public void reset(){
        for (CPU cpu : cpus) {
            cpu.reset();
        }
        this.time = 0;
        avgCPUsUtilization = 0.0;
        askForMigrationCount = 0;
        migrationCount = 0;
        rejectedTaskCount = 0;
        standardDeviationCPUsUtilization = 0.0;
        averageLoadsOverTime.clear();
        deviationsOverTime.clear();
    }

    public void askForMigration(Task task, CPU cpu) {
        if (task != null && cpu != null) {
            askForMigrationCount++;
        }
    }

    public void moveTaskToAnotherCPU(Task task, CPU from, CPU to) {
        if (task != null && to != null) {
            if (from != null) {
                from.removeTask(task);
            }
            to.handleTask(task);
            migrationCount++;
        }
    }

    /** Wywoływana w Algorithm1, gdy żaden CPU nie może przyjąć zadania */
    public void incrementRejectedCount() {
        rejectedTaskCount++;
    }

    public void collectStatsThisTick() {
        float sum = 0;
        for (CPU cpu : cpus) {
            sum += cpu.getCurrentLoad();
        }
        float avg = sum / N;
        averageLoadsOverTime.add(avg);

        float devSum = 0;
        for (CPU cpu : cpus) {
            float load = cpu.getCurrentLoad();
            devSum += Math.pow(load - avg, 2);
        }
        float stdDev = (float) Math.sqrt(devSum / N);
        deviationsOverTime.add(stdDev);
    }

    public void calculateStats() {
        float sumLoad = 0;
        for (float avg : averageLoadsOverTime) {
            sumLoad += avg;
        }
        avgCPUsUtilization = (sumLoad / averageLoadsOverTime.size()) * 100;

        float sumDev = 0;
        for (float dev : deviationsOverTime) {
            sumDev += dev;
        }
        standardDeviationCPUsUtilization = (sumDev / deviationsOverTime.size()) * 100;

        System.out.println("\nWyniki symulacji:");
        System.out.printf("A. Średnie obciążenie procesorów: %.2f%%\n", avgCPUsUtilization);
        System.out.printf("B. Średnie odchylenie od wartości: %.2f%%\n", standardDeviationCPUsUtilization);
        System.out.printf("C. Ilość zapytań o obciążenie: %d\n", askForMigrationCount);
        System.out.printf("   Ilość migracji procesów: %d\n", migrationCount);
        System.out.printf("   Ilość odrzuconych zadań: %d\n", rejectedTaskCount);
    }

    public void doneTask(Task task, CPU cpu) {
        if(task != null && cpu != null && task.isFinished()) {
            cpu.removeTask(task);
        }
    }

    public void incrementTime(int increment) {
        this.time += increment;
    }

    public void incrementTime() {
        this.time++;
    }

    public int getTime() {
        return time;
    }

    public ArrayList<CPU> getCpus() {
        return cpus;
    }

    public int getN() {
        return N;
    }

    public float getP() {
        return P;
    }

    public void incrementMigrationCount() {
        this.migrationCount++;
    }

    /** (opcjonalnie) getter, jeśli gdzieś potrzebujesz odczytać liczbę odrzuconych */
    public int getRejectedTaskCount() {
        return rejectedTaskCount;
    }

    public abstract void runAlgorithm(ArrayList<Task> tasks);
}
