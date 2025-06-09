package Processor;

public class Task {
    private boolean wasSuspended = false;
    private int load; // Obciążenie, jakie zadanie wprowadza na procesor
    private int remainingTime;// Pozostały czas wykonania zadania
    private int arrivalTime; // Czas przybycia zadania, jeśli potrzebny do symulacji

    // Konstruktor z parametrami, bo potrzebuję tworzyć nowe zadania z określonym obciążeniem i czasem wykonania
    public Task(int load, int remainingTime, int arrivalTime) {
        this.load = load;
        this.remainingTime = remainingTime;
        this.arrivalTime = arrivalTime;

    }

    // Konstruktor kopiujący, bo potrzebuję tworzyć niezależne kopie zadań dla różnych symulacji
    public Task(Task task) {
        this.load = task.load;
        this.remainingTime = task.remainingTime;
        this.arrivalTime = task.arrivalTime;
    }

    public boolean wasSuspended() { return wasSuspended; }
    public void setSuspended(boolean value) { wasSuspended = value; }

    // Gettery i settery, bo potrzebuję dostępu do tych pól z zewnątrz
    public int getLoad() {
        return load;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
}
