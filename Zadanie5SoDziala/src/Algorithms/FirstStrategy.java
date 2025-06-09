package Algorithms;

import Processor.Processor;
import Processor.Task;

import java.util.ArrayList;
import java.util.Random;

public class FirstStrategy extends TaskAllocationStrategy {
    private int z;

    public FirstStrategy(ArrayList<Processor> processors, ArrayList<Task> tasks, int p, int z) {
        super(processors, tasks, p);

        this.z = z;
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
                    getStatistics().collectStatistics(getProcessors(), 1);
                    updateTasksAndLoadForAllProcessors(1);
                }

                if (canGetTask()) {
                    Task currentTask = getTasks().getFirst();
                    // wybieramy losowy procesor
                    Processor firstProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                    boolean taskAssigned = false;
                    int attempts = 0;

                    // z razy szukamy procesora o obciążeniu < P
                    while (!taskAssigned && attempts < z) {
                        //losujemy ten procesor
                        Processor currentProcessor = getProcessors().get(random.nextInt(getProcessorsCount()));
                        setQueries(getQueries() + 1);

                        if (canAssignTask(currentProcessor, currentTask)) {
                            currentProcessor.getTasks().add(currentTask);
                            currentProcessor.setLoad(currentProcessor.getLoad() + currentTask.getLoad());
                            getTasks().remove(currentTask);
                            taskAssigned = true;
                            setMigrations(getMigrations() + 1);
                        } else {
                            attempts++;
                            incrementCurrentTime(1);
                            updateTasksAndLoadForAllProcessors(1);
                            getStatistics().collectStatistics(getProcessors(), 1);
                        }
                    }

                    // jeśli nie znaleziono lepszego procesora w z próbach to zostaje na zrodlowym
                    if (!taskAssigned) {
                        if (firstProcessor.getLoad() + currentTask.getLoad() <= 100) {
                            firstProcessor.getTasks().add(currentTask);
                            firstProcessor.setLoad(firstProcessor.getLoad() + currentTask.getLoad());
                            getTasks().remove(currentTask);
                        } else {
                            incrementSuspendedTasks();
                            getTasks().remove(currentTask);
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