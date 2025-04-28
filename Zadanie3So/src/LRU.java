import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class LRU extends Algoritm{
    private int[] lastUsedTimes;
    private int currentTime;

    public LRU(int maxSize){
        super(maxSize);
        lastUsedTimes = new int[maxSize];
        currentTime = 0;
    }

    @Override
    public void run(ArrayList<Page> pages) {
        Queue<Page> queue = new LinkedList<>(pages);

        while (!queue.isEmpty()) {
            Page page = queue.poll();
            checkTrashing();
            handleRequest(page, queue);
        }
    }

    @Override
    public void handleRequest(Page request, Queue<Page> queue) {
        currentTime++;
        if (!pageFault(request)) {
            for (int i = 0; i < getRam().getSize(); i++) {
                Page frame = getRam().get(i);
                if (frame != null && frame.getID().equals(request.getID())) {
                    lastUsedTimes[i] = currentTime;
                    break;
                }
            }
        } else {
            if (getRam().hasEmptyIndes()) {
                int index = getRam().getEmptyIndex();
                getRam().insert(index, request);
                lastUsedTimes[index] = currentTime;
            } else {
                int lruIndex = findLRU();
                getRam().insert(lruIndex, request);
                lastUsedTimes[lruIndex] = currentTime;
            }
        }

        incrementRequestCount();
    }

    private int findLRU() {
        int minTime = Integer.MAX_VALUE;
        int index = -1;

        for (int i = 0; i < getRam().getSize(); i++) {
            if (lastUsedTimes[i] < minTime) {
                minTime = lastUsedTimes[i];
                index = i;
            }
        }
        return index;
    }
}
