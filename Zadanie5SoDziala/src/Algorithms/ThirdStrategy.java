package Algorithms;

import Processor.Processor;
import Processor.Task;

import java.util.ArrayList;
import java.util.Random;

public class ThirdStrategy extends TaskAllocationStrategy {
    // Definiuję dolną granicę obciążenia, bo w algorytmie 3 procesory o obciążeniu
    // mniejszym od minimalnego progu r aktywnie pytają inne procesory
    private int lowerLimit = 0;

    // Inicjalizuję z dodatkowym parametrem r, bo strategia 3 wymaga progu minimalnego obciążenia
    public ThirdStrategy(ArrayList<Processor> processors, ArrayList<Task> tasks, int p, int r) {
        super(processors, tasks, p);

        this.lowerLimit = r;
    }

    @Override
    public void run() {
        setCurrentTime(0); // Resetuję czas symulacji na początku
        Random random = new Random();

        // Kontynuuję pętlę dopóki są zadania do przydzielenia lub procesory mają zadania
        while (!getTasks().isEmpty() || processorsHaveTasks()) {

            incrementCurrentTime(1); // Symuluję upływ czasu, bo każde zadanie trwa co najmniej 1 jednostkę czasu
            getStatistics().collectStatistics(getProcessors(), 1); // Statystyki dla podstawowej jednostki czasu

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
                    // Losuję pierwszy procesor, bo zadanie pojawia się na losowym procesorze
                    Processor firstProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                    setQueries(getQueries() + 1); // Zapytanie do pierwszego procesora

                    // Sprawdzam, czy pierwszy procesor może przyjąć zadanie
                    if (firstProcessor.getLoad() < getUpperLimit() && firstProcessor.getLoad() + currentTask.getLoad() <= 100) {
                        // Przydzielam zadanie do pierwszego procesora
                        firstProcessor.getTasks().add(currentTask);
                        firstProcessor.setLoad(firstProcessor.getLoad() + currentTask.getLoad());
                        getTasks().removeFirst();
                    } else {
                        // Jeśli pierwszy procesor nie może przyjąć zadania, szukam innego
                        boolean taskAssigned = false;

                        // Próbuję logarytmiczną liczbę razy, bo to efektywna heurystyka
                        while (!taskAssigned && attempts < Math.log(getProcessorsCount())) {
                            // Losuję procesor, bo szukamy dowolnego z obciążeniem < P
                            Processor currentProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                            setQueries(getQueries() + 1);

                            // Sprawdzam, czy procesor może przyjąć zadanie
                            if (currentProcessor.getLoad() < getUpperLimit() && currentProcessor.getLoad() + currentTask.getLoad() <= 100) {
                                // Przydzielam zadanie, bo znalazłem odpowiedni procesor
                                currentProcessor.getTasks().add(currentTask);
                                currentProcessor.setLoad(currentProcessor.getLoad() + currentTask.getLoad());
                                getTasks().removeFirst();
                                taskAssigned = true;

                                // Zwiększam licznik migracji, bo zadanie zostało przeniesione
                                setMigrations(getMigrations() + 1);
                            } else {
                                // Zwiększam licznik prób, bo ta próba się nie powiodła
                                attempts++;
                                incrementCurrentTime(1);
                                updateTasksAndLoadForAllProcessors(1);// Symuluję upływ czasu, bo każde zapytanie trwa co najmniej 1 jednostkę czasu
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

            // W trzeciej strategii procesory o małym obciążeniu pytają inne o możliwość przejęcia zadań
            // Implementuję to, bo to część algorytmu 3
            for (Processor processor : getProcessors()) {
                // Sprawdzam, czy procesor jest mało obciążony (poniżej progu r)
                if (processor.getLoad() < lowerLimit) {
                    // Losuję procesor do zapytania, bo dowolny procesor może mieć nadmiar zadań
                    Processor newProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                    setQueries(getQueries() + 1);
                    // Jeśli ta operacja zapytania/przeniesienia również kosztuje czas,
                    // należałoby tu dodać:
                    // incrementCurrentTime(1); timeElapsedThisCycle++; getStatistics().collectStatistics(getProcessors(), 1);
                    // Na razie zakładam, że ta część nie zużywa dodatkowego czasu poza podstawowym cyklem.

                    // Jeśli zapytany procesor jest przeciążony, to przenosimy z niego zadania
                    if (newProcessor.getLoad() >= getUpperLimit()) {
                        ArrayList<Task> tasksOnNewProcessor = newProcessor.getTasks();

                        // Uwaga: Oryginalna pętla miała potencjalny błąd z random.nextInt(i) gdy i=0.
                        // Poniższa pętla próbuje przenieść zadania, dopóki procesor nie jest już przeciążony.
                        // Iterujemy po kopii, aby uniknąć ConcurrentModificationException, jeśli usuwamy elementy.
                        ArrayList<Task> tasksToConsiderMoving = new ArrayList<>(tasksOnNewProcessor);
                        for (Task taskToMove : tasksToConsiderMoving) {
                            if (newProcessor.getLoad() < getUpperLimit()) {
                                break; // Procesor już nie jest przeciążony
                            }
                            // Sprawdzamy, czy zadanie nadal jest na tym procesorze (mogło zostać przeniesione w innej części logiki)
                            // i czy zmieści się na procesorze o małym obciążeniu
                            if (tasksOnNewProcessor.contains(taskToMove) && processor.getLoad() + taskToMove.getLoad() <= 100) {
                                // Przenoszę zadanie z przeciążonego na mało obciążony procesor
                                processor.getTasks().add(taskToMove);
                                processor.setLoad(processor.getLoad() + taskToMove.getLoad());
                                newProcessor.setLoad(newProcessor.getLoad() - taskToMove.getLoad());
                                tasksOnNewProcessor.remove(taskToMove);

                                // Zwiększam licznik migracji, bo zadanie zostało przeniesione
                                setMigrations(getMigrations() + 1);
                            }
                        }
                    }
                }
            }

            // Aktualizuję pozostały czas wykonania dla wszystkich zadań
            updateTasksAndLoadForAllProcessors(1);

            // Usunięto końcowe zbieranie statystyk dla timeElapsedThisCycle
            // getStatistics().collectStatistics(getProcessors(), timeElapsedThisCycle);
        }

        // Zbieram końcowe statystyki i wypisuję wyniki
        printStatistics();
    }
}