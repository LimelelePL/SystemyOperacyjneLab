package Algorithms;

import Processor.Processor;
import Processor.Task;

import java.util.ArrayList;
import java.util.Random;

public class SecondStrategy extends TaskAllocationStrategy {

    // Konstruktor przyjmuje podstawowe parametry bez 'z', bo ta strategia nie ma z góry ustalonej liczby prób
    public SecondStrategy(ArrayList<Processor> processors, ArrayList<Task> tasks, int p) {
        super(processors, tasks, p);
    }

    @Override
    public void run() {
        int iterationCounter = 0;
        setCurrentTime(0);
        Random random = new Random();

        // Kontynuuję pętlę dopóki są zadania do przydzielenia lub procesory mają zadania
        while (!getTasks().isEmpty() || processorsHaveTasks()) {
            iterationCounter++;
            int timeElapsedThisCycle = 0;

            incrementCurrentTime(1);
            timeElapsedThisCycle++;
        // Symuluję upływ czasu, bo każde zadanie trwa co najmniej 1 jednostkę czasu

            if (!getTasks().isEmpty()) {
                int attempts = 0;
                Task currentTask = getTasks().getFirst();
                // Losuję procesor, bo w strategii 2 zadanie najpierw pojawia się na losowym procesorze
                Processor firstProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                setQueries(getQueries() + 1); // Zapytanie do pierwszego procesora

                // Sprawdzam, czy wylosowany procesor może przyjąć zadanie
                // bez przekroczenia progu P i bez przekroczenia 100% obciążenia
                if (firstProcessor.getLoad() < getUpperLimit() && firstProcessor.getLoad() + currentTask.getLoad() <= 100) {
                    // Przydzielam zadanie do pierwszego procesora, bo spełnia on warunki
                    firstProcessor.getTasks().add(currentTask);
                    firstProcessor.setLoad(firstProcessor.getLoad() + currentTask.getLoad());
                    getTasks().removeFirst();
                } else {
                    // Jeśli pierwszy procesor nie może przyjąć zadania, szukam innego
                    boolean taskAssigned = false;

                    // Próbuję logarytmiczną liczbę razy względem liczby procesorów
                    // bo taką heurystykę przyjąłem dla efektywnego wyszukiwania
                    while (!taskAssigned && attempts < Math.log(getProcessorsCount())) {
                        // Losuję procesor, bo szukamy dowolnego z obciążeniem < P
                        Processor currentProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                        setQueries(getQueries() + 1); // Zwiększam licznik zapytań

                        // Sprawdzam, czy procesor może przyjąć zadanie
                        if (currentProcessor.getLoad() < getUpperLimit() && currentProcessor.getLoad() + currentTask.getLoad() <= 100) {
                            // Przydzielam zadanie, bo znalazłem odpowiedni procesor
                            currentProcessor.getTasks().add(currentTask);
                            currentProcessor.setLoad(currentProcessor.getLoad() + currentTask.getLoad());
                            getTasks().removeFirst();
                            taskAssigned = true;

                            // Zwiększam licznik migracji, bo zadanie zostało przeniesione na inny procesor
                            setMigrations(getMigrations() + 1);
                        } else {
                            // Zwiększam licznik prób, bo ta próba się nie powiodła
                            attempts++;
                            incrementCurrentTime(1); // Symuluję upływ czasu, bo każde zapytanie trwa co najmniej 1 jednostkę czasu
                            timeElapsedThisCycle++;
                        }
                    }
                    if (!taskAssigned) {
                        incrementSuspendedTasks();
                        getTasks().removeFirst(); // Usuwamy zawieszone zadanie z kolejki
                    }
                }
            }


            // Aktualizuję pozostały czas wykonania dla wszystkich zadań na wszystkich procesorach
            // Symuluje to upływ czasu w systemie
            for (Processor processor : getProcessors()) {
                if (!processor.getTasks().isEmpty()) {
                    ArrayList<Task> tasksOnProcessor = processor.getTasks();

                    // Iteruję od końca, bo będę usuwać elementy z listy
                    for (int i = tasksOnProcessor.size() - 1; i >= 0; i--) {
                        Task task = tasksOnProcessor.get(i);
                        // Zmniejszam pozostały czas zadania o czas, który upłynął w tej iteracji
                        task.setRemainingTime(task.getRemainingTime() - timeElapsedThisCycle);

                        // Usuwam zakończone zadania, bo nie potrzebują już zasobów procesora
                        if (task.getRemainingTime() <= 0) {
                            processor.setLoad(processor.getLoad() - task.getLoad());
                            tasksOnProcessor.remove(i);
                        }
                    }
                }
            }
            getStatistics().collectStatistics(getProcessors());
        }

        // Zbieram końcowe statystyki i wypisuję wyniki symulacji
        getStatistics().collectStatistics(getProcessors());
        printStatistics();
        System.out.println("czas " + getCurrentTime() + " ms");
    }
}