import java.util.ArrayList;

public class FrameAllocator{
    public static void allocateEqual(ArrayList<Process> processes, int totalFrames) {
        int perProcess = totalFrames / processes.size();
        for (Process p : processes) {
            p.assignApproxLRUWithFrames(perProcess);
        }
    }
}
