import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Optimal extends Algoritm {
    public Optimal(int size) {
        super(size);
    }

    @Override
    public void run(ArrayList<Page> pages) {
        Queue<Page> queue = new LinkedList<>(pages);

        while (!queue.isEmpty()) {
            Page page = queue.poll();
           // checkTrashing();
            handleRequest(page, queue);
        }
    }

    @Override
    public void handleRequest(Page request, Queue<Page> queue) {
        if(pageFault(request)){
            if(getRam().hasEmptyIndes()) {
                getRam().addToAnEmptyIndex(request);
                incrementRequestCount();
                return;
            }
            int index=lookIntoFuture(queue);
            if(index!=-1) getRam().insert(index, request);
        }
        incrementRequestCount();
    }

    public int lookIntoFuture(Queue<Page> queue) {
        ArrayList<Page> list = new ArrayList<>(queue);

        if (!getRam().hasEmptyIndes()) {
            int idx = -1;
            int max = -1;

            for (int i = 0; i < getRam().getSize(); i++) {
                Page page = getRam().get(i);
                int count = 0;

                if (page != null) {
                    boolean found = false;
                    for (Page value : list) {
                        count++;
                        if (page!=null && value.getID().equals(page.getID())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        count = list.size() + 1; // strona nie wystąpi w przyszłości
                    }
                } else {
                    count = list.size() + 1; // pusta ramka
                }

                if (count > max) {
                    max = count;
                    idx = i;
                }
            }

            return idx;
        }
        return -1;
    }

}
