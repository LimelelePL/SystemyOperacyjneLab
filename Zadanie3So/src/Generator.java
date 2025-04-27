import java.util.ArrayList;
import java.util.Random;

public class Generator {
    //dodac generator szamotan
    //dodac te generowanie z podciągów
    public ArrayList<Page> generatePages(int size, int pageNumbers) {
        ArrayList<Page> pages = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
         pages.add(new Page(rand.nextInt(pageNumbers)));
        }
        return pages;
    }
}
