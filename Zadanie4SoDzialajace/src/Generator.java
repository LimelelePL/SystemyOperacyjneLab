import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {
   //do generowania jednego procesu
    public ArrayList<Page> generate(int size, int pageNumbers, double subsetChance, int sectionSize) {
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

    /*pomysl jest taki ze dla kazdego procesu generujemy substring w mainie a potem robimy z niego
    global ReferenceSubstring, roundobinowskim podejsciem. Kazdy element w globalReferenceSubstring
    ma tez referencje do procesu z ktorego pochodzi.
     */

    public ArrayList<ReferenceEvent> generateGlobalReferenceEvents(List<Process> processes) {
        ArrayList<ReferenceEvent> global = new ArrayList<>();

        boolean stillAdding = true;
        int index = 0;

        while (stillAdding) {
            stillAdding = false;
            for (Process p : processes) {
                List<Page> refs = p.getReferenceString();
                if (index < refs.size()) {
                    global.add(new ReferenceEvent(p.getPid(), refs.get(index)));
                    stillAdding = true;
                }
            }
            index++;
        }

        return global;
    }
}
