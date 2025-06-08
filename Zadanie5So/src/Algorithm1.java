import java.util.*;

public class Algorithm1 extends Algorithm {
    private final int Z;
    private final Random random = new Random();

    public Algorithm1(ArrayList<CPU> cpus, int N, float P, int Z) {
        super(cpus, N, P);
        this.Z = Z;
    }

    @Override
    public void runAlgorithm(ArrayList<Task> tasks) {
        // Kolejka zadań posortowana po czasie przybycia
        PriorityQueue<Task> taskQueue = new PriorityQueue<>(Comparator.comparingInt(Task::getArrivalTime));
        taskQueue.addAll(tasks);

        int currentTime = 0;
        ArrayList<CPU> cpus = getCpus();

        while (!taskQueue.isEmpty() || cpus.stream().anyMatch(cpu -> !cpu.getCpuQueue().isEmpty())) {
            // Obsługa nowych zadań przychodzących w bieżącym momencie
            while (!taskQueue.isEmpty() && taskQueue.peek().getArrivalTime() <= currentTime) {
                Task task = taskQueue.poll();
                int sourceId = task.getIdOfCPU();
                CPU sourceCPU = cpus.get(sourceId);

                boolean taskMigrated = false;

                // 1. Próbujemy Z razy znaleźć CPU o obciążeniu < P i które może przyjąć zadanie
                List<Integer> triedCPUs = new ArrayList<>();
                for (int attempt = 0; attempt < Z; attempt++) {
                    // Losujemy CPU różne od źródłowego i od tych już sprawdzonych
                    int targetId;
                    do {
                        targetId = random.nextInt(cpus.size());
                    } while (targetId == sourceId || triedCPUs.contains(targetId));

                    triedCPUs.add(targetId);
                    CPU targetCPU = cpus.get(targetId);

                    // Zliczamy zapytanie do targetCPU o obciążenie
                    askForMigration(task, targetCPU);

                    // Sprawdzamy, czy docelowy CPU ma obecne obciążenie < P%
                    // i czy po dodaniu taska nadal nie przekroczy 100 %.
                    if (targetCPU.getCurrentLoad() * 100 < getP() && targetCPU.canHandleTask(task)) {
                        moveTaskToAnotherCPU(task, null, targetCPU);
                        taskMigrated = true;
                        break;
                    }
                }

                // 2. Jeśli nie udało się przenieść zadania po Z próbach, próbujemy dać je na źródłowy CPU,
                //    ale tylko jeśli on może je obsłużyć. Jeśli i on nie może, szukamy dowolnego CPU.
                if (!taskMigrated) {
                    if (sourceCPU.canHandleTask(task)) {
                        sourceCPU.handleTask(task);
                        taskMigrated = true;
                    } else {
                        // Szukamy jakiegokolwiek CPU w systemie, które może obsłużyć to zadanie
                        for (CPU cpu : cpus) {
                            if (cpu.canHandleTask(task)) {
                                moveTaskToAnotherCPU(task, null, cpu);
                                taskMigrated = true;
                                break;
                            }
                        }
                    }
                }

                // 3. Jeżeli nadal taskMigrated == false, oznacza to, że żaden CPU nie miał miejsca.
                //    W tym momencie uznajemy task za odrzucony i inkrementujemy licznik odrzuceń.
                if (!taskMigrated) {
                    incrementRejectedCount();
                }
            }

            // 4. Przetwarzamy zadania na wszystkich CPU przez jeden cykl czasu
            for (CPU cpu : cpus) {
                cpu.tick();
            }

            collectStatsThisTick();
            incrementTime();
            currentTime++;
        }

        calculateStats();
    }
}
