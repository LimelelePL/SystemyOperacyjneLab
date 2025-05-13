import java.util.List;

public abstract class Allocator {

    public abstract void allocate(List<Process> processes,
                                  List<ReferenceEvent> globalReferenceString,
                                  int ramSize);

    public Process getProcessByPid(int pid, List<Process> processes) {
        for (Process p : processes) {
            if (p.getPid() == pid) {
                return p;
            }
        }
        return null;
    }
}
