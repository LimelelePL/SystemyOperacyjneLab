package Algorithms;

import Processor.Processor;
import Processor.Task;

import java.util.ArrayList;
import java.util.Random;

public class FirstStrategy extends TaskAllocationStrategy {
    private int z = 0; // maksymalna liczba prób


    // Inicjalizuję z parametrem 'z', bo ta strategia wymaga określenia maksymalnej liczby prób
    // zanim podejmiemy decyzję o pozostawieniu zadania na procesorze źródłowym
    public FirstStrategy(ArrayList<Processor> processors, ArrayList<Task> tasks, int p, int z) {
        super(processors, tasks, p);

        this.z = z;
    }

    @Override
    public void run() {
        setCurrentTime(0);
        Random random = new Random();


        // Kontynuuję pętlę dopóki są zadania do przydzielenia, bo muszę obsłużyć wszystkie zadania w kolejce
        // lub procesory wciąż mają zadania do wykonania
        while (!getTasks().isEmpty() || processorsHaveTasks()) {

            incrementCurrentTime(1); // Symuluję upływ czasu, bo każde zadanie trwa co najmniej 1 jednostkę czasu
            getStatistics().collectStatistics(getProcessors(), 1); // Statystyki dla podstawowej jednostki czasu

            if (!getTasks().isEmpty()) {

                // Pętla obsługująca oczekiwanie na kolejne zadanie, aktualizująca statystyki co jednostkę czasu
                while (!getTasks().isEmpty() && getTasks().getFirst().getArrivalTime() > getCurrentTime()) {
                    incrementCurrentTime(1);
                    getStatistics().collectStatistics(getProcessors(), 1);
                    updateTasksAndLoadForAllProcessors(1);
                }

                // Sprawdzenie, czy po oczekiwaniu zadanie jest gotowe do przetworzenia
                if (!getTasks().isEmpty() && getTasks().getFirst().getArrivalTime() <= getCurrentTime()) {
                    Task currentTask = getTasks().getFirst();
                    // Wybieram losowy procesor jako źródłowy, bo zadanie najpierw pojawia się na losowym procesorze
                    Processor firstProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                    boolean taskAssigned = false;
                    int attempts = 0;

                    // Próbuję znaleźć procesor, który ma obciążenie mniejsze od progu P
                    // Robię to maksymalnie z razy, bo taki jest wymóg algorytmu 1
                    while (!taskAssigned && attempts < z) {
                        // Losuję procesor, bo szukam dowolnego procesora o obciążeniu < P
                        Processor currentProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                        setQueries(getQueries() + 1); // Zwiększam licznik zapytań

                        // Sprawdzam, czy procesor ma obciążenie mniejsze od progu P i czy może przyjąć nowe zadanie
                        // bez przekroczenia 100% swojego obciążenia
                        if (currentProcessor.getLoad() < getUpperLimit() && currentProcessor.getLoad() + currentTask.getLoad() <= 100) {
                            // Dodaję zadanie do wylosowanego procesora, bo spełnia on nasze kryteria
                            currentProcessor.getTasks().add(currentTask);
                            // Aktualizuję obciążenie procesora, bo dodaliśmy nowe zadanie
                            currentProcessor.setLoad(currentProcessor.getLoad() + currentTask.getLoad());
                            // Usuwam zadanie z listy zadań do przydzielenia, bo zostało już przydzielone
                            getTasks().remove(currentTask);
                            taskAssigned = true;

                            // Zwiększam licznik migracji, bo zadanie zostało przeniesione na inny procesor
                            setMigrations(getMigrations() + 1);

                        } else {
                            // Zwiększam licznik prób, bo ta próba się nie powiodła
                            attempts++;
                            incrementCurrentTime(1); // Dodatkowy czas na nieudaną próbę
                            updateTasksAndLoadForAllProcessors(1);
                            getStatistics().collectStatistics(getProcessors(), 1); // Statystyki dla czasu próby
                        }
                        // Symuluję upływ czasu, bo każde zapytanie trwa co najmniej 1 jednostkę czasu
                    }

                    // Jeśli po z próbach nie znalazłem odpowiedniego procesora, zadanie zostaje na źródłowym
                    // zgodnie z wymogiem algorytmu 1
                    if (!taskAssigned) {
                        // Sprawdzam, czy procesor źródłowy może obsłużyć zadanie, bo musi mieć dość zasobów
                        if (firstProcessor.getLoad() + currentTask.getLoad() <= 100) {
                            // Dodaję zadanie do procesora źródłowego, bo nie znaleziono lepszego
                            firstProcessor.getTasks().add(currentTask);
                            // Aktualizuję obciążenie procesora źródłowego, bo dodano nowe zadanie
                            firstProcessor.setLoad(firstProcessor.getLoad() + currentTask.getLoad());
                            // Usuwam zadanie z kolejki, bo zostało przydzielone
                            getTasks().remove(currentTask);
                        } else {
                            incrementSuspendedTasks();
                            getTasks().remove(currentTask);// Usuwamy zawieszone zadanie z kolejki
                            // Jeśli procesor źródłowy nie może obsłużyć zadania, zwiększam licznik zadań zawieszonych
                        }
                    }
                }
            }
            updateTasksAndLoadForAllProcessors(1);
        }

        // Zbieram końcowe statystyki po zakończeniu wszystkich zadań
        // i wypisuję je, bo to jest wymaganiem zadania
        printStatistics();
    }
}