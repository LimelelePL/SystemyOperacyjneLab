import java.util.List;

public interface FrameAllocator {
    void allocate(List<Process> processes, int totalFrames);
}