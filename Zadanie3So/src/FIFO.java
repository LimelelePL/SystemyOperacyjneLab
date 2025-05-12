
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class FIFO extends Algoritm{

    private int pointer = 0;
    public FIFO(int size){
        super(size);
    }

    @Override
    public void run(ArrayList<Page> pages) {
        Queue<Page> queue = new LinkedList<>(pages);

        while(!queue.isEmpty()){
            Page page=queue.poll();
                handleRequest(page, queue);
        }
    }

    @Override
    public void handleRequest(Page page, Queue<Page> queue) {
        if(pageFault(page)){
            getRam().insert(pointer,page);
            pointer = (pointer + 1) % getRam().getSize();
        }
        incrementRequestCount();
    }
}
