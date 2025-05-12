import java.util.List;

public class EqualAllocator implements FrameAllocator {

    @Override
    public void allocate(List<Process> processes, int totalFrames) {
        int framesPerProcess = totalFrames / processes.size();

        for (Process p : processes) {
            Algoritm algorithm = new ApproxLRU(framesPerProcess);
            p.setAlgorithm(algorithm);
        }
    }
}