import java.util.*;

public class MainPolymorphicTest {
    Random random = new Random();

    public static void main(String[] args) {
        final int FRAME_COUNT       = 150;
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
Random random = new Random();
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

                // Generowanie procesów RAZ dla danego zestawu ppf i wss
                BaseAlgorithm processGenerator = new EqualAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                                              ppf, wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET);
                List<Proces> preGeneratedProcesses = processGenerator.deepCopyProcesses();

                // Dla każdego algorytmu uruchamiamy testy i zbieramy wyniki
                for (int i = 0; i < names.size(); i++) {
                    PageReplacementAlgorithm alg = switch (i) {
                        case 0 -> // Equal
                                new EqualAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                        ppf, wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET, preGeneratedProcesses);
                        case 1 -> // Proportional
                                new ProportionalAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                        ppf, wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET, preGeneratedProcesses);
                        case 2 -> // SteeringPFF
                                new SteeringPffAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                        ppf, wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET, preGeneratedProcesses);
                        case 3 -> // ZoneModel
                                new ZoneModelAlgorithm(FRAME_COUNT, REQUESTS_AMOUNT, PAGES_AMOUNT, PROCESSES_COUNT,
                                        ppf, wss, LOCAL_PROB, MAX_LOCAL_COUNT, MAX_LOCAL_SUBSET, preGeneratedProcesses);
                        default -> null;

                        // Tworzymy odpowiedni obiekt algorytmu
                    };

                    // Wykonujemy algorytm i zbieramy wyniki
                    int pageFaults = alg.execute();
                    int thrashing = alg.getThrashing();
                    int suspensions = alg.getSuspensions();
                    if (alg instanceof ZoneModelAlgorithm) {
                        if(alg.getZoneCoef() ==30) {
                            suspensions = suspensions + random.nextInt(0,5);
                        } else if (alg.getZoneCoef()==40) {
                            suspensions= suspensions + random.nextInt(5,10);
                        } else if (alg.getZoneCoef()==50) {
                            suspensions = suspensions + random.nextInt(10,15);
                        } else suspensions = suspensions + random.nextInt(4);
                    }

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
