
import java.util.*;

public class Proces {
    public List<Page> requests;
    public int framesCount = 0;
    public int errors = 0;

    public Proces(List<Page> requests, int framesCount) {
        this.requests = new ArrayList<>(requests);
        this.framesCount = framesCount;
    }

}
