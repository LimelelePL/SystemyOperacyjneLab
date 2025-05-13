
import java.util.List;

public class ProportionalAllocator extends Allocator {

    @Override
    public void allocate(List<Process> processes, List<ReferenceEvent> globalReferenceString, int ramSize) {
        int totalPages= getSumOfPagesUsingByProcess(processes);
        for (Process p : processes) {
            int framesForProcess = (int) Math.floor((double) p.getNumberOfPages() / totalPages * ramSize);
            p.assignApproxLRUWithFrames(framesForProcess);
        }

        for (ReferenceEvent page : globalReferenceString) {
            Process p = getProcessByPid(page.getPid(), processes);
            if (p != null) {
                p.handle(page.getPage());
            }
        }
    }

    public int getSumOfPagesUsingByProcess(List<Process> processes) {
            int sum = 0;
            for (Process p : processes) {
                sum += p.getNumberOfPages();
            }
            return sum;
    }
}
