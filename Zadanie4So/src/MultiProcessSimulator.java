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
            if (p.isSuspended()) continue;

            p.getAlgorithm().handleRequest(ref.page, new LinkedList<>());

            // Obsługa PPF
            if (ppfAllocator != null && ppfAllocator.shouldMonitor(p)) {
                ppfAllocator.monitor(processes);
            }

            // Obsługa WSS
            if (wssAllocator != null) {
                wssAllocator.recordReference(p, ref.page);
                if (wssAllocator.shouldMonitor(p)) {
                    wssAllocator.monitor(processes);
                }
            }
        }
    }

    public void printStats() {
        for (Process p : processes) {
            System.out.println("Process " + p.getProcessID() + ":");
            System.out.println("Page Faults: " + p.getAlgorithm().getPageFaultCount());
            System.out.println("Trashing: " + p.getAlgorithm().getTrashingCount());
        }
    }

    public void setWorkingSetAllocator(WorkingSetAllocator allocator) {
        this.wssAllocator = allocator;
    }
    public void setPPFAllocator(PPFAllocator allocator) {
        this.ppfAllocator = allocator;
    }
}