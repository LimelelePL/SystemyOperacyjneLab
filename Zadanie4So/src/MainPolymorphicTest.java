import java.util.*;

public class MainPolymorphicTest {
    public static void main(String[] args) {
        final int FRAME_COUNT       = 100;
        final int REQUESTS_AMOUNT   = 10000;
        final int PAGES_AMOUNT      = 250;
        final int PROCESSES_COUNT   = 7;
        final double LOCAL_PROB     = 0.05;
        final int MAX_LOCAL_COUNT   = 100;
        final int MAX_LOCAL_SUBSET  = 100;

        double[] ppfCoeffs = {0.1, 0.3, 0.5, 0.7, 0.9};
        int[]  wssSizes  = {10, 20, 30, 40, 50};

        // Nagłówek tabeli z informacją o zawartości
        System.out.println("Format komórki: PageFaults / Thrashing / Suspensions\n");

        // Wyświetl nagłówki tabeli
        System.out.printf("%5s %5s", "PPF", "WSS");
        List<String> names = List.of("Equal","Proportional","SteeringPFF","ZoneModel");
        for (String n : names) System.out.printf(" %25s", n);
        System.out.println();

        // Linia separująca nagłówek
        System.out.print("─────────────");
        for (int i = 0; i < names.size(); i++) {
            System.out.print("─────────────────────────");
        }
        System.out.println();

        // Uruchom testy i wyświetl wyniki
        for (double ppf : ppfCoeffs) {
            for (int wss : wssSizes) {
                System.out.printf("%5.2f %5d", ppf, wss);

                // Dla każdego algorytmu uruchamiamy testy i zbieramy wyniki
                for (int i = 0; i < names.size(); i++) {
                    PageReplacementAlgorithm alg = null;

                    // Tworzymy odpowiedni obiekt algorytmu
                    switch (i) {
                        case 0: // Equal
                            alg = new EqualAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                    ppf, wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET);
                            break;
                        case 1: // Proportional
                            alg = new ProportionalAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                    ppf, wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET);
                            break;
                        case 2: // SteeringPFF
                            alg = new SteeringPffAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                    ppf, wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET);
                            break;
                        case 3: // ZoneModel
                            alg = new ZoneModelAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                    ppf, wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET);
                            break;
                    }

                    // Wykonujemy algorytm i zbieramy wyniki
                    int pageFaults = alg.execute();
                    int thrashing = alg.getThrashing();
                    int suspensions = alg.getSuspensions();

                    // Wyświetlamy wyniki w formacie: "PageFaults / Thrashing / Suspensions"
                    System.out.printf(" %10d / %-7d / %-5d", pageFaults, thrashing, suspensions);
                }
                System.out.println();
            }

            // Linia separująca różne wartości PPF
            if (ppf != ppfCoeffs[ppfCoeffs.length-1]) {
                System.out.print("─────────────");
                for (int i = 0; i < names.size(); i++) {
                    System.out.print("─────────────────────────");
                }
                System.out.println();
            }
        }
    }
}
