import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {

    public List<Page> generateProcessReferences(
            int size,               // ile odwołań
            int pageNumbers,        // ile różnych stron
            int basePageOffset,     // od jakiego numeru strony zacząć (żeby były rozłączne)
            double subsetChance,    // lokalność
            int sectionSize         // co ile zmiana sekcji lokalności
    ) {
        ArrayList<Page> pages = new ArrayList<>();
        Random rand = new Random();

        int subrangeSize = Math.max(5, pageNumbers / 5);
        int rangeStart = basePageOffset;
        int rangeEnd = Math.min(rangeStart + subrangeSize, basePageOffset + pageNumbers);

        for (int i = 0; i < size; i++) {
            if (i % sectionSize == 0) {
                rangeStart = basePageOffset + rand.nextInt(Math.max(1, pageNumbers - subrangeSize));
                rangeEnd = Math.min(rangeStart + subrangeSize, basePageOffset + pageNumbers);
            }

            int page;
            if (rand.nextDouble() < subsetChance) {
                page = rand.nextInt(rangeEnd - rangeStart) + rangeStart;
            } else {
                page = rand.nextInt(pageNumbers) + basePageOffset;
            }

            pages.add(new Page(page));
        }

        return pages;
    }

    public List<Reference> generateGlobalReferencesForProcesses(List<Process> processes) {
        List<Reference> globalRefs = new ArrayList<>();
        int[] indices = new int[processes.size()];
        boolean done = false;

        while (!done) {
            done = true;
            for (int i = 0; i < processes.size(); i++) {
                List<Page> local = processes.get(i).getReferenceString();
                if (indices[i] < local.size()) {
                    globalRefs.add(new Reference(i, local.get(indices[i])));
                    indices[i]++;
                    done = false;
                }
            }
        }

        return globalRefs;
    }
    public ArrayList<Page> ClassicNormal(int size, int pageNumbers) {
        ArrayList<Page> pages = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            pages.add(new Page(rand.nextInt(pageNumbers)));
        }
        return pages;
    }
    public ArrayList<Page> usingSubstrings(int size, int pageNumbers, double subsetChance, int sectionSize) {
        ArrayList<Page> pages = new ArrayList<>();
        Random rand = new Random();

        int subrangeSize = Math.max(5, pageNumbers / 5);

        int currentSection = 0;
        int rangeStart = 0;
        int rangeEnd = Math.min(rangeStart + subrangeSize, pageNumbers);

        for (int i = 0; i < size; i++) {
            if (i % sectionSize == 0) {
                currentSection++;
                rangeStart = rand.nextInt(Math.max(1, pageNumbers - subrangeSize));
                rangeEnd = Math.min(rangeStart + subrangeSize, pageNumbers);
            }
            if (rand.nextDouble() < subsetChance) {
                int page = rand.nextInt(rangeEnd - rangeStart) + rangeStart;
                pages.add(new Page(page));
            } else {
                int page = rand.nextInt(pageNumbers);
                pages.add(new Page(page));
            }
        }

        return pages;
    }


}