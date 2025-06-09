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
            getStatistics().collectStatistics(getProcessors(), 1);

            if (!getTasks().isEmpty()) {
                while (!canGetTask()) {
                    incrementCurrentTime(1);
                    updateTasksAndLoadForAllProcessors(1);
                    getStatistics().collectStatistics(getProcessors(), 1);
                }
                
                if (canGetTask()) {
                    int attempts = 0;
                    Task currentTask = getTasks().getFirst();
                    // Losuję pierwszy procesor
                    Processor firstProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                    setQueries(getQueries() + 1);

                    // Sprawdzamy czy pierwszy procesor może przyjąć zadanie
                    if (canAssignTask(firstProcessor, currentTask)) {
                        firstProcessor.getTasks().add(currentTask);
                        firstProcessor.setLoad(firstProcessor.getLoad() + currentTask.getLoad());
                        getTasks().removeFirst();
                    } else {
                      //szukamy innego
                        boolean taskAssigned = false;

                        // Próbujemy logarytmiczną liczbę razy względem liczby procesorów
                        while (!taskAssigned && attempts < Math.log(getProcessorsCount())) {
                            // Losujęmy procesor z obciążeniem < P
                            Processor currentProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                            setQueries(getQueries() + 1);

                            if (canAssignTask(currentProcessor, currentTask)) {
                                currentProcessor.getTasks().add(currentTask);
                                currentProcessor.setLoad(currentProcessor.getLoad() + currentTask.getLoad());
                                getTasks().removeFirst();
                                taskAssigned = true;
                                setMigrations(getMigrations() + 1);
                            } else {
                                attempts++;
                                incrementCurrentTime(1);
                                updateTasksAndLoadForAllProcessors(1);
                                getStatistics().collectStatistics(getProcessors(), 1);
                            }
                        }
                        if (!taskAssigned) {
                            incrementSuspendedTasks();
                            getTasks().removeFirst();
                        }
                    }
                }
            }
            updateTasksAndLoadForAllProcessors(1);
        }
        printStatistics();
    }

    private boolean canAssignTask(Processor processor, Task task) {
        return processor.getLoad() < getUpperLimit() && processor.getLoad() + task.getLoad() <= 100;
    }

    private boolean canGetTask() {
        return !getTasks().isEmpty() && getTasks().getFirst().getArrivalTime() <= getCurrentTime();
    }
}