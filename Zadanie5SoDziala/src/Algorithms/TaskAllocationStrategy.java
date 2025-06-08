package Algorithms;

import Processor.Processor;
import Processor.Task;
import Statistics.LoadStatistics;

import java.util.ArrayList;

public abstract class TaskAllocationStrategy {
    private int processorsCount; // liczba procesorów
    private int upperLimit; // górny limit obciążenia
    private int tasksCount; // ilość zadań
    private int suspendedAndFinishedTasks = 0;
    // Tworzę kopie list zadań i procesorów, bo chcę symulować niezależnie różne strategie
    // na tych samych danych wejściowych
    private ArrayList<Task> tasks = new ArrayList<>();
    private ArrayList<Processor> processors = new ArrayList<>();

    // Inicjalizuję statystyki, bo potrzebuję zbierać dane o wydajności algorytmów
    private LoadStatistics statistics = new LoadStatistics();
    private int statisticsInterval = 1; // Co ile zadań zbierać statystyki
    private int queries = 0; // Licznik zapytań o obciążenie
    private int migrations = 0; // Licznik migracji procesów

    private int currentTime = 0;
    private int suspendedTasks = 0;// Aktualny czas symulacji

    public TaskAllocationStrategy(ArrayList<Processor> processors, ArrayList<Task> tasks, int p) {
        this.processorsCount = processors.size();
        this.tasksCount = tasks.size();
        this.upperLimit = p;
        this.currentTime = 0;

        // Tworzę głębokie kopie procesorów, bo chcę uniknąć modyfikacji oryginalnych danych
        for (Processor processor : processors) {
            this.processors.add(new Processor(processor));
        }

        // Tworzę głębokie kopie zadań, bo będę je modyfikować w trakcie symulacji
        for (Task task : tasks) {
            this.tasks.add(new Task(task));
        }
    }

    public void incrementCurrentTime(int time) {
        this.currentTime += time;
    }

    public int getSuspendedTasks() {
        return suspendedTasks;
    }
    public void incrementSuspendedTasks() {
        this.suspendedTasks++;
    }

    public void incrementSuspendedAndFinishedTasks() { suspendedAndFinishedTasks++; }
    public int getSuspendedAndFinishedTasks() { return suspendedAndFinishedTasks; }

    // Abstrakcyjna metoda run, bo każda strategia musi ją zaimplementować po swojemu
    public abstract void run();

    // Wyświetlam wyniki symulacji, bo chcę porównać skuteczność strategii
    protected void printStatistics() {
        System.out.println("\n======== " + this.getClass().getSimpleName() + " ========");
        System.out.println("Ilość zapytań: " + getQueries());
        System.out.println("Ilość migracji: " + getMigrations());
        System.out.printf("Średnie obciążenie: %.2f%%\n", statistics.getFinalAverage());
        System.out.printf("Odchylenie standardowe: %.2f%%\n", statistics.getFinalStandardDeviation());
        System.out.println("Liczba wstrzymanych/odrzuconych zadań: " + getSuspendedTasks());
    }

    public boolean processorsHaveTasks() {
        for (Processor p : getProcessors()) {
            if (!p.getTasks().isEmpty()) return true;
        }
        return false;
    }

    // Gettery i settery, bo potrzebuję dostępu do tych pól z klas pochodnych
    public int getProcessorsCount() {
        return processorsCount;
    }

    public void setProcessorsCount(int processorsCount) {
        this.processorsCount = processorsCount;
    }

    public int getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(int upperLimit) {
        this.upperLimit = upperLimit;
    }

    public int getTasksCount() {
        return tasksCount;
    }

    public void setTasksCount(int tasksCount) {
        this.tasksCount = tasksCount;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public ArrayList<Processor> getProcessors() {
        return processors;
    }

    public void setProcessors(ArrayList<Processor> processors) {
        this.processors = processors;
    }

    public int getQueries() {
        return queries;
    }

    public void setQueries(int queries) {
        this.queries = queries;
    }

    public int getMigrations() {
        return migrations;
    }

    public void setMigrations(int migrations) {
        this.migrations = migrations;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public LoadStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(LoadStatistics statistics) {
        this.statistics = statistics;
    }

    public int getStatisticsInterval() {
        return statisticsInterval;
    }

    public void setStatisticsInterval(int statisticsInterval) {
        this.statisticsInterval = statisticsInterval;
    }
}
