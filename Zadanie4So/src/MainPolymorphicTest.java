import java.util.ArrayList;
import java.util.List;

public class MainPolymorphicTest {
    public static void main(String[] args) {
        final int FRAME_COUNT = 150;
        final int REQUESTS_AMOUNT = 10000;
        final int PAGES_AMOUNT = 250;
        final int PROCESSES_COUNT = 9;
        final double LOCAL_PROB = 0.05;
        final int MAX_LOCAL_COUNT = 100;
        final int MAX_LOCAL_SUBSET = 100;
        double[] upperPPF = {0.3, 0.5, 0.7, 0.9};
        double[] lowerPPF = {0.1, 0.2, 0.2, 0.3};
        int[] wssSizes = {10, 20, 30, 40, 50};

        System.out.println("Format komórki: PageFaults / Thrashing / ");
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

        int x = 0;
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

                    System.out.printf(" %10d / %-7d", pageFaults, thrashing);
                }
                System.out.println();
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
