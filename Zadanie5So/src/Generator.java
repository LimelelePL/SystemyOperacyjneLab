import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Generator {
    Random random = new Random();
    
    public ArrayList<Task> generateTasks(int numberOfTasks, int maxArrivalTime, int maxDuration, int numberOfCPUs, float maxDemand) {
        ArrayList<Task> tasks = new ArrayList<>();

        // Generujemy zadania z RÓŻNYMI częstotliwościami i wymaganiami
        for (int i = 0; i < numberOfTasks; i++) {
            int arrivalTime = random.nextInt(maxArrivalTime);
            int duration = random.nextInt(maxDuration) + 5; // Minimum 5 jednostek czasu
            int cpuId = random.nextInt(numberOfCPUs);
            
            // Generujemy różne wymagania - od 1% do 10% mocy obliczeniowej procesora
            float demand = random.nextFloat() * (maxDemand - 1) + 1; // od 1% do maxDemand%

            Task task = new Task(arrivalTime, duration, cpuId, demand);
            tasks.add(task);
        }
        return tasks;
    }

    public ArrayList<CPU> generateCPUs(int numberOfCPUs) {
        ArrayList<CPU> cpus = new ArrayList<>();

        for (int i = 0; i < numberOfCPUs; i++) {
            cpus.add(new CPU(new LinkedList<>(), i));
        }
        return cpus;
    }
}
