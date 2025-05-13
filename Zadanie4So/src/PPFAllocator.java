import java.util.List;

public class PPFAllocator implements FrameAllocator {

    private int deltaT = 100;
    private double upperThreshold = 0.8;
    private double lowerThreshold = 0.2;
    private int minFrames = 1;

    private int freeFrames = 0;

    @Override
    public void allocate(List<Process> processes, int totalFrames) {
        int framesPerProcess = totalFrames / processes.size();
        freeFrames = totalFrames - (framesPerProcess * processes.size());//obliczamy ile ramek jest w zapasie
        for (Process p : processes) {
            p.setAlgorithm(new ApproxLRU(framesPerProcess));
            p.resume();
        }
    }

    public void monitor(List<Process> processes) {
        for (Process p : processes) {
            if (p.isSuspended()) {
                p.suspend();
                continue;
            }

            Algoritm alg = p.getAlgorithm();
            int requests = alg.getRecentRequests();


            if (requests < deltaT) continue;

            int faults = alg.getRecentFaults();
            double ppf = (double) faults / requests;
            if (ppf > upperThreshold) { //
                if (freeFrames > 0) {
                    alg.setRamSize(alg.getRamSize() + 1);
                    freeFrames--;
                } else {
                    p.suspend();
                }
            } else if (ppf < lowerThreshold && alg.getRamSize() > minFrames) {
                alg.setRamSize(alg.getRamSize() - 1);
                freeFrames++;
            }

           alg.resetWindowStats(); // resetujemy okno po decyzji
        }
    }

    public boolean shouldMonitor(Process p) {
        // gdy łączna liczba żądań % deltaT == 0
        return p.getAlgorithm().getRequestCount() % deltaT == 0;
    }
}