import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ApproxLRU extends Algoritm {
    private boolean[] bits;
    private int pointer;

    public ApproxLRU(int size) {
        super(size);
        bits = new boolean[size];
        pointer = 0;
    }

    //useless teraz jest
    @Override
    public void run(ArrayList<Page> pages) {
//        Queue<Page> queue = new LinkedList<>(pages);
//        while (!queue.isEmpty()) {
//            Page page = queue.poll();
//            handleRequest(page);
//        }
    }

    @Override
    public void handleRequest(Page request) {
        if (pageFault(request)) {
            if (getRam().hasEmptyIndes()) {
                getRam().addToAnEmptyIndex(request);
                bits[pointer] = true;
                movePointer();
            } else {
                while (true) {
                    if (!bits[pointer]) {
                        getRam().insert(pointer, request);
                        bits[pointer] = true;
                        movePointer();
                        break;
                    } else {
                        bits[pointer] = false;
                        movePointer();
                    }
                }
            }
        } else {
            int index = getRam().findIndex(request);
            if (index != -1) {
                bits[index] = true;
            }
        }
        incrementRequestCount();
    }
    private void movePointer(){
        pointer = (pointer + 1) % getRam().getSize();
    }
}
