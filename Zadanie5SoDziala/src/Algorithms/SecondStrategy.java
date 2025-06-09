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
        setCurrentTime(0);
        Random random = new Random();

        // Kontynuuję pętlę dopóki są zadania do przydzielenia lub procesory mają zadania
        while (!getTasks().isEmpty() || processorsHaveTasks()) {

            incrementCurrentTime(1);
            getStatistics().collectStatistics(getProcessors(), 1); // Statystyki dla podstawowej jednostki czasu
            // Symuluję upływ czasu, bo każde zadanie trwa co najmniej 1 jednostkę czasu

            if (!getTasks().isEmpty()) {

                // Pętla obsługująca oczekiwanie na kolejne zadanie, aktualizująca statystyki co jednostkę czasu
                while (!getTasks().isEmpty() && getTasks().getFirst().getArrivalTime() > getCurrentTime()) {
                    incrementCurrentTime(1);
                    updateTasksAndLoadForAllProcessors(1);
                    getStatistics().collectStatistics(getProcessors(), 1);
                }
                
                if (!getTasks().isEmpty() && getTasks().getFirst().getArrivalTime() <= getCurrentTime()) {
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
                                updateTasksAndLoadForAllProcessors(1);
                                getStatistics().collectStatistics(getProcessors(), 1); // Statystyki dla czasu próby
                            }
                        }
                        if (!taskAssigned) {
                            incrementSuspendedTasks();
                            getTasks().removeFirst(); // Usuwamy zawieszone zadanie z kolejki
                        }
                    }
                }
            }


            updateTasksAndLoadForAllProcessors(1);
            // Usunięto końcowe zbieranie statystyk dla timeElapsedThisCycle
            // getStatistics().collectStatistics(getProcessors(), timeElapsedThisCycle);
        }

        // Zbieram końcowe statystyki i wypisuję wyniki symulacji
        // Usunięto getStatistics().collectStatistics(getProcessors());
        printStatistics();
        // Usunięto System.out.println("czas " + getCurrentTime() + " ms");
    }
}