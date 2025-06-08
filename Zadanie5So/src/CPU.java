import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CPU {
    private  int ID;
    private  Queue<Task> cpuQueue;
    private float utilization;


    public CPU(Queue<Task> cpuQueue, int ID) {
        this.cpuQueue = new LinkedList<>();
        this.ID = ID;
        this.utilization = 0;
    }

    public void reset() {
        cpuQueue.clear();
        utilization = 0;
    }

    public boolean canHandleTask(Task task) {
        if (task == null) {
            return false;
        }

        float util = utilization;
        return util + task.getDemand() <= 1.0;
    }

    public float getCurrentLoad() {
        float total = 0;
        for (Task t : cpuQueue) {
            if (!t.isFinished()) {
                total += t.getDemand();
            }
        }
        return total;
    }

    public void handleTask(Task task){
        if (task != null) {
            cpuQueue.add(task);
            utilization += task.getDemand();
        }
    }

    public void removeTask(Task task) {
        if (task != null && cpuQueue.contains(task)) {
            cpuQueue.remove(task);
            utilization -= task.getDemand();

        }
    }

    public void tick() {
        Iterator<Task> it = cpuQueue.iterator();
        while (it.hasNext()) {
            Task task = it.next();
            task.decrementTimeLeft();
            if (task.isFinished()) {
                // Używamy tylko iteratora do usuwania
                utilization -= task.getDemand(); // Aktualizacja wykorzystania
                it.remove(); // Bezpieczne usunięcie przez iterator
                // Nie wywołujemy już removeTask(task) - to powodowało wyjątek
            }
        }
    }

    public Queue<Task> getCpuQueue() {
        return cpuQueue;
    }

    public void setCpuQueue(Queue<Task> cpuQueue) {
        this.cpuQueue = cpuQueue;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }


    public void setUtilization(int utilization) {
        this.utilization = utilization;
    }
}
