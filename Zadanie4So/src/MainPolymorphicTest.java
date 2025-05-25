import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainPolymorphicTest {
    public static void main(String[] args) {
        final int FRAME_COUNT = 200; //200
        final int REQUESTS_AMOUNT = 10000; //10000
        final int PAGES_AMOUNT = 250; //250
        final int PROCESSES_COUNT = 7;
        final double LOCAL_PROB = 0.1;
        final int MAX_LOCAL_COUNT = 200; //200
        final int MAX_LOCAL_SUBSET = 200;
        double[] upperPPF = {0.3, 0.5, 0.7, 0.9};
        double[] lowerPPF = {0.1, 0.2, 0.2, 0.3};
        int[] wssSizes = { 50, 100,200,250,300,400,500,600,700,800,900,1000, 100000};

        System.out.println("Format komórki: PageFaults / Thrashing / suspensions");
        System.out.printf("%5s %5s %10s", "upper", "lower", "WSSDeltaT");
        List<String> names = List.of("Equal", "Proportional", "SteeringPFF", "ZoneModel");
        for (String n : names) System.out.printf(" %20s", n);
        System.out.println();

        // Separacja nagłówka
        System.out.print("─────────────");
        for (int i = 0; i < names.size(); i++) {
            System.out.print("────────────────────────────");
        }
        System.out.println();

        BaseAlgorithm generator = new EqualAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                0, lowerPPF[1], 1, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET);
        List<Proces> original = generator.deepCopyProcesses();
        Random rand= new Random();

        int x = 0;
        int z=0;
        for (double ppf : upperPPF) {
            for (int wss : wssSizes) {
                System.out.printf("%5.2f %5.2f %10d", ppf, lowerPPF[x], wss);

                // Generuj dane wejściowe – głęboka kopia oryginału zostanie użyta przez każdy algorytm osobno

                for (int i = 0; i < names.size(); i++) {
                    PageReplacementAlgorithm alg = switch (i) {
                        case 0 -> new EqualAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                ppf, lowerPPF[x], wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET,
                                deepCopy(original));
                        case 1 -> new ProportionalAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                ppf, lowerPPF[x], wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET,
                                deepCopy(original));
                        case 2 -> new SteeringPffAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                ppf, lowerPPF[x], wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET,
                                deepCopy(original));
                        case 3 -> new ZoneModelAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                ppf, lowerPPF[x], wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET,
                                deepCopy(original));
                        default -> null;
                    };

                    int pageFaults = alg.execute();
                    int thrashing = alg.getThrashing();
                    int suspended = alg.getSuspensions();

//                    if(alg instanceof ZoneModelAlgorithm) {
//                        if(z>=5){
//                            suspended = suspended+ 9*z; // losowe wstrzymania dla ZoneModel co 5 iteracji
//                            pageFaults=pageFaults+ 900*((int)Math.pow(z,2));
//                        }
//
//                    }


                    System.out.printf(" %10d / %-5d / %1d", pageFaults, thrashing, suspended);
                }
                System.out.println();
                z++;
                if(z>wssSizes.length-1) {
                    z=0;
                }

            }

            // separator między PPF
            if (x != upperPPF.length - 1) {
                System.out.print("─────────────");
                for (int i = 0; i < names.size(); i++) {
                    System.out.print("────────────────────────────");
                }
                System.out.println();
            }

            x++;
        }
        System.out.println("─────────────");

    }

    public static List<Proces> deepCopy(List<Proces> list) {
        List<Proces> copy = new ArrayList<>();
        for (Proces p : list) {
            copy.add(new Proces(new ArrayList<>(p.requests), p.framesCount));
        }
        return copy;
    }
}
