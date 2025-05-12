import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProportionalAllocator implements FrameAllocator {
        @Override
        public void allocate(List<Process> processes, int totalFrames) {
            int totalUniquePages = 0;
            List<Integer> uniqueCounts = new ArrayList<>();

            // liczymy uniaklne strony dla kazdego procesu
            for (Process p : processes) {
                Set<Integer> uniquePages = new HashSet<>();
                for (Page page : p.getReferenceString()) {
                    uniquePages.add(page.getNumber());
                }
                uniqueCounts.add(uniquePages.size());
                totalUniquePages += uniquePages.size();
            }

            //przydzielmy proporcjonalnie ramki
            for (int i = 0; i < processes.size(); i++) {
                double ratio = (double) uniqueCounts.get(i) / totalUniquePages;
                int frames = Math.max(1, (int) Math.round(ratio * totalFrames)); // minimum 1 ramka
                Algoritm algorithm = new ApproxLRU(frames);
                processes.get(i).setAlgorithm(algorithm);
            }
        }
    }
