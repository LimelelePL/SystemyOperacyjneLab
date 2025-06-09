package Algorithms;

import Processor.Processor;
import Processor.Task;

import java.util.ArrayList;
import java.util.Random;

public class ThirdStrategy extends TaskAllocationStrategy {
    private int lowerLimit = 0;

    public ThirdStrategy(ArrayList<Processor> processors, ArrayList<Task> tasks, int p, int r) {
        super(processors, tasks, p);

        this.lowerLimit = r;
    }

    @Override
    public void run() {
        setCurrentTime(0);
        Random random = new Random();

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
                    // Losujęmy pierwszy procesor
                    Processor firstProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                    setQueries(getQueries() + 1);

                    // Sprawdzamy czy pierwszy procesor może przyjąć zadanie
                    if (canAssignTask(firstProcessor, currentTask)) {
                        firstProcessor.getTasks().add(currentTask);
                        firstProcessor.setLoad(firstProcessor.getLoad() + currentTask.getLoad());
                        getTasks().removeFirst();
                    } else {
                        boolean taskAssigned = false;
                        // szukamy procesora o obciążeniu < P logarytmiczną liczbę razy względem liczby procesorów
                        while (!taskAssigned && attempts < Math.log(getProcessorsCount())) {
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

            //  procesory o małym obciążeniu pytają inne o możliwość przejęcia zadań
            for (Processor processor : getProcessors()) {
                if (processor.getLoad() < lowerLimit) {
                   // losujemy procesor do zapytania
                    Processor newProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                    setQueries(getQueries() + 1);

                    // Sprawdzamy, czy nowy procesor jest przeciążony
                    if (newProcessor.getLoad() >= getUpperLimit()) {
                        ArrayList<Task> tasksOnNewProcessor = newProcessor.getTasks();

                        // przenosimy zadania o malym obciazzeniu do momentu, gdy nowy procesor nie jest przeciążony
                        ArrayList<Task> tasksToConsiderMoving = new ArrayList<>(tasksOnNewProcessor);
                        for (Task taskToMove : tasksToConsiderMoving) {
                            if (newProcessor.getLoad() < getUpperLimit()) {
                                break; // Procesor już nie jest przeciążony
                            }
                            if (tasksOnNewProcessor.contains(taskToMove) && processor.getLoad() + taskToMove.getLoad() <= 90) {
                                processor.getTasks().add(taskToMove);
                                processor.setLoad(processor.getLoad() + taskToMove.getLoad());
                                newProcessor.setLoad(newProcessor.getLoad() - taskToMove.getLoad());
                                tasksOnNewProcessor.remove(taskToMove);
                                setMigrations(getMigrations() + 1);
                            }
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