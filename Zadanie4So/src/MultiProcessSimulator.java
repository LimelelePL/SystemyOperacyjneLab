import java.util.LinkedList;
import java.util.List;

public class MultiProcessSimulator {
    private List<Process> processes;
    private List<Reference> globalReferenceString;
    private PPFAllocator ppfAllocator;
    private WorkingSetAllocator wssAllocator;

    public MultiProcessSimulator(List<Process> processes, List<Reference> globalReferenceString) {
        this.processes = processes;
        this.globalReferenceString = globalReferenceString;
    }


    public void run() {
        for (Reference ref : globalReferenceString) {
            Process p = processes.get(ref.processId);
            if (p.isSuspended()){
                p.resume();
                continue;
            }
            p.getAlgorithm().handleRequest(ref.page, new LinkedList<>());

            if (ppfAllocator != null) {
                if (ppfAllocator.shouldMonitor(p)) {
                    ppfAllocator.monitor(processes);
                }
            }

            if (wssAllocator != null) {
                wssAllocator.recordReference(p, ref.page);
                if (wssAllocator.shouldMonitor(p)) {
                    wssAllocator.monitor(processes);
                }
            }
        }
    }

    public void setWorkingSetAllocator(WorkingSetAllocator allocator) {
        this.wssAllocator = allocator;
    }
    public void setPPFAllocator(PPFAllocator allocator) {
        this.ppfAllocator = allocator;
    }
}