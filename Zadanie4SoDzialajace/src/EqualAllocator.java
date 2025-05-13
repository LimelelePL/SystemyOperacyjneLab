import java.util.List;

public class EqualAllocator  extends Allocator {
    private int nOFPP;

    @Override
    public void allocate(List<Process> processes, List<ReferenceEvent> globalReferenceString, int ramSize) {
        Algoritm algoritm = new ApproxLRU(ramSize);
        algoritm.resetStats();

        nOFPP=ramSize/processes.size();

        for (Process p : processes) {
            p.assignApproxLRUWithFrames(nOFPP);

        }

        for (ReferenceEvent page : globalReferenceString) {
            Process p = getProcessByPid(page.getPid(), processes);
            if(p!=null) {
                p.handle(page.getPage());
            }
        }
    }

}
