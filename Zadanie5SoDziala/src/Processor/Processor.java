package Processor;

import java.util.ArrayList;

public class Processor {
    private int load;
    private ArrayList<Task> tasks;

    public Processor() {
        this.load = 0;
        this.tasks = new ArrayList<>();
    }

    public Processor(Processor processor) {
        this.load = processor.load;
        this.tasks = new ArrayList<>(processor.tasks);
    }


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
