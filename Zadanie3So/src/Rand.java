import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Rand extends Algoritm{
    private Random rand;

    public Rand(int size) {
        super(size);
        rand = new Random();
    }

    @Override
    public void run(ArrayList<Page> pages) {
        Queue<Page> queue = new LinkedList<>(pages);

        while(!queue.isEmpty()){
            Page page=queue.poll();
            //checkTrashing();
            handleRequest(page, queue);
        }
    }

    @Override
    public void handleRequest(Page request, Queue<Page> queue) {
            if (pageFault(request)) {
                int index = rand.nextInt(getRam().getSize());
                getRam().insert(index, request);
            }
            incrementRequestCount();
    }
}
