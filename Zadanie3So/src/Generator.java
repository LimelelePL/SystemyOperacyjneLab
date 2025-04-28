import java.util.ArrayList;
import java.util.Random;

public class Generator {
    public ArrayList<Page> ClassicNormal(int size, int pageNumbers) {
        ArrayList<Page> pages = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
         pages.add(new Page(rand.nextInt(pageNumbers)));
        }
        return pages;
    }
    public ArrayList<Page> usingSubstrings(int size, int pageNumbers, double subsetChance) {
        ArrayList<Page> pages = new ArrayList<>();
        Random rand = new Random();

        int sectionSize = 100;
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

    public ArrayList<Page> generateHighTrashing(int size, int pageNumbers) {
        ArrayList<Page> pages = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < size; i++) {
            int page;

            if (i % 2 == 0) {
                page = rand.nextInt(pageNumbers);
            } else {
                page = (rand.nextInt(pageNumbers / 2) + pageNumbers / 2) % pageNumbers;
            }

            pages.add(new Page(page));
        }

        return pages;
    }
}
