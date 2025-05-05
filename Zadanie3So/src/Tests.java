import java.util.ArrayList;

public class Tests {
        public static void main(String[] args) {
            Generator generator = new Generator();

            // Generowanie różnych typów stron
            ArrayList<Page> randomPages = generator.ClassicNormal(10000, 40);
            ArrayList<Page> usingSubsetPages = generator.usingSubstrings(10000, 40, 0.4,100);
            ArrayList<Page> allSubsetsPages = generator.usingSubstrings(10000, 40, 1.0,100);
            ArrayList<Page> bigSubsets = generator.usingSubstrings(10000, 40, 0.4, 1000);
            ArrayList<Page> smallSubsets = generator.usingSubstrings(10000, 40, 0.4, 5);
            ArrayList<Page> highTrashingPages = generator.generateHighTrashing(10000, 100);

            ArrayList<ArrayList<Page>> generatedLists = new ArrayList<>();
            generatedLists.add(randomPages);
            generatedLists.add(usingSubsetPages);
            generatedLists.add(allSubsetsPages);
            generatedLists.add(bigSubsets);
            generatedLists.add(smallSubsets);
            generatedLists.add(highTrashingPages);

            ArrayList<String> generatorNames = new ArrayList<>();
            generatorNames.add("FullRandom");
            generatorNames.add("UsingSubset(0.4)");
            generatorNames.add("AllSubsets(1.0)");
            generatorNames.add("BigSubsets");
            generatorNames.add("SmallSubsets");
            generatorNames.add("HighTrashing");

            runTests(generatedLists, generatorNames);
        }

        public static void runTests(ArrayList<ArrayList<Page>> generatedLists, ArrayList<String> generatorNames) {
            ArrayList<Algoritm> algorithms = new ArrayList<>();
            ArrayList<String> algorithmNames = new ArrayList<>();

            int ramSize = 15;

            algorithms.add(new Rand(ramSize));
            algorithmNames.add("RAND");

            algorithms.add(new FIFO(ramSize));
            algorithmNames.add("FIFO");

            algorithms.add(new Optimal(ramSize));
            algorithmNames.add("OPTIMAL");

            algorithms.add(new LRU(ramSize));
            algorithmNames.add("LRU");

            algorithms.add(new ApproxLRU(ramSize));
            algorithmNames.add("ApproxLRU");

            System.out.printf("%-15s | %-20s | %-15s | %-15s\n", "Algorithm", "Generator", "PageFaults", "Trashings");
            System.out.println("----------------------------------------------------------------------------------------");

            int algIndex = 0;
            for (Algoritm algoritm : algorithms) {
                int genIndex = 0;
                for (ArrayList<Page> requestList : generatedLists) {
                    algoritm.resetStats();
                    algoritm.run(new ArrayList<>(requestList));

                    System.out.printf(
                            "%-15s | %-20s | %-15d | %-15d\n",
                            algorithmNames.get(algIndex),
                            generatorNames.get(genIndex),
                            algoritm.getPageFaultCount(),
                            algoritm.getTrashingCount()
                    );

                    genIndex++;
                }
                algIndex++;

                System.out.println("----------------------------------------------------------------------------------------");
            }
        }
    }

