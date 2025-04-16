import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Generator generator = new Generator();


        List<Process> randomList = generator.generateRandom(0.05, 500, 5000, 5000);
        List<Process> oneSide   = generator.generateInOneSide(0.05, 500, 5000, 5000);
        List<Process> bothSides = generator.generateInBothEdges(0.05, 500, 5000, 5000);


        ArrayList<List<Process>> standardGeneratedLists = new ArrayList<>();
        standardGeneratedLists.add(randomList);
        standardGeneratedLists.add(oneSide);
        standardGeneratedLists.add(bothSides);


        ArrayList<String> standardGeneratorNames = new ArrayList<>();
        standardGeneratorNames.add("Random");
        standardGeneratorNames.add("OneSide");
        standardGeneratorNames.add("Both Edges");


        FCFS fcfs   = new FCFS(new Disk(53, 500));
        SSTF sstf   = new SSTF(new Disk(53, 500));
        EDF edf     = new EDF(new Disk(53, 500));
        CSCAN cscan = new CSCAN(new Disk(53, 500));
        SCAN scan   = new SCAN(new Disk(53, 500));
        FDScan fdscan = new FDScan(new Disk(53, 500));


        System.out.println("===================== STANDARDOWE TESTY DLA WSZYSTKICH ALGORYTMÓW =====================");
        runTests(fcfs, standardGeneratedLists, standardGeneratorNames, false);
        runTests(sstf, standardGeneratedLists, standardGeneratorNames, false);
        runTests(edf, standardGeneratedLists, standardGeneratorNames, false);
        runTests(cscan, standardGeneratedLists, standardGeneratorNames, true);
        runTests(scan, standardGeneratedLists, standardGeneratorNames, true);
        runTests(fdscan, standardGeneratedLists, standardGeneratorNames, true);

        System.out.println("\n===================== SPECJALNE TESTY DLA ALGORYTMU SCAN (Procesy za głowicą) =====================");

        List<Process> behindHead = generator.generateBehindHead(500, 5000, 20000, 53,  true);

        SCAN specialScan = new SCAN(new Disk(3, 500));
        specialScan.getDisk().reset();
        specialScan.reset();

        System.out.println("------ SCAN z procesami generowanymi za głowicą (behind head) ------");
        specialScan.run(new ArrayList<>(behindHead));
        printTestResults(specialScan, true);
    }

    public static void runTests(Algoritm algorithm, List<List<Process>> generatedLists, ArrayList<String> generatorNames, boolean showReturns) {
        System.out.println("================== " + algorithm.getClass().getName() + " ===============================");
        int index = 0;
        for (List<Process> processes : generatedLists) {
            algorithm.getDisk().reset();
            algorithm.reset();

            System.out.println("------ GENERATOR: " + generatorNames.get(index) + " ------");
            algorithm.run(new ArrayList<>(processes));

            System.out.println("Total head movements: " + algorithm.getDisk().getTotalHeadMovements());
            System.out.println("Average wait time: " + algorithm.getAverageWaitTime());
            System.out.println("Starved processes: " + algorithm.getStarvedProcesses());

            if (showReturns) {
                if (algorithm instanceof CSCAN) {
                    System.out.println("Returns: " + ((CSCAN) algorithm).getReturns());
                } else if (algorithm instanceof SCAN) {
                    System.out.println("Returns: " + ((SCAN) algorithm).getReturns());
                } else if (algorithm instanceof FDScan) {
                    System.out.println("Returns: " + ((FDScan) algorithm).getReturns());
                }
            }
            index++;
        }
    }

    public static void printTestResults(Algoritm algorithm, boolean showReturns) {
        System.out.println("Total head movements: " + algorithm.getDisk().getTotalHeadMovements());
        System.out.println("Average wait time: " + algorithm.getAverageWaitTime());
        System.out.println("Starved processes: " + algorithm.getStarvedProcesses());

        if (showReturns) {
            if (algorithm instanceof CSCAN) {
                System.out.println("Returns: " + ((CSCAN) algorithm).getReturns());
            } else if (algorithm instanceof SCAN) {
                System.out.println("Returns: " + ((SCAN) algorithm).getReturns());
            } else if (algorithm instanceof FDScan) {
                System.out.println("Returns: " + ((FDScan) algorithm).getReturns());
            }
        }
    }
}
