import java.util.*;

public class WorkingSetAllocator implements FrameAllocator {

    private int deltaT = 20;
    private int minFrames = 1;
    private int freeFrames = 0;
    private int monitorTick = 0;

    private Map<Integer, Deque<Integer>> recentRefs = new HashMap<>();

    @Override
    public void allocate(List<Process> processes, int totalFrames) {
        // Początkowy przydział – równy
        int base = totalFrames / processes.size();
        freeFrames = totalFrames - (base * processes.size());

        for (Process p : processes) {
            p.setAlgorithm(new ApproxLRU(base));
            p.resume();
            recentRefs.put(p.getProcessID(), new ArrayDeque<>());
        }
    }

    public void recordReference(Process p, Page page) {
        int id = p.getProcessID();
        Deque<Integer> queue = recentRefs.get(id);
        queue.addLast(page.getNumber());

        if (queue.size() > deltaT) {
            queue.pollFirst();
        }
    }

    public boolean shouldMonitor(Process p) {
        monitorTick++;
        return monitorTick % deltaT == 0;
    }

    public void monitor(List<Process> processes) {
        for (Process p : processes) {
            if (p.isSuspended()){
                p.resume();
                continue;
            }

            int id = p.getProcessID();
            Set<Integer> workingSet = new HashSet<>(recentRefs.get(id));

            int desired = Math.max(minFrames, workingSet.size());
            int current = p.getAlgorithm().getRamSize();

            if (desired > current && freeFrames >= (desired - current)) {
                p.getAlgorithm().setRamSize(desired);
                freeFrames -= (desired - current);
            }
            else if (desired < current) {
                int toFree = current - desired;
                p.getAlgorithm().setRamSize(desired);
                freeFrames += toFree;
            } else p.suspend();
        }
    }
}

