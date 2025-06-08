package Processor;

import java.util.ArrayList;

public class Processor {
    private int load; // obciążenie procesora
    private ArrayList<Task> tasks;

    // Tworzę pusty procesor, bo potrzebuję inicjalizować nowe procesory
    public Processor() {
        this.load = 0;
        this.tasks = new ArrayList<>();
    }

    // Tworzę kopię procesora, bo potrzebuję niezależnych instancji dla różnych strategii
    public Processor(Processor processor) {
        this.load = processor.load;
        this.tasks = new ArrayList<>(processor.tasks);
    }

    // Gettery i settery, bo potrzebuję dostępu do tych pól z zewnątrz
    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }
}
