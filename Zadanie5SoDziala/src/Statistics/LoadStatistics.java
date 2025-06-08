package Statistics;

import Processor.Processor;

import java.util.ArrayList;
import java.util.List;

public class LoadStatistics {
    // Przechowuję średnie obciążenia i odchylenia standardowe w czasie
    // bo potrzebuję analizować zmienność obciążenia w trakcie symulacji
    private List<Double> averageLoads = new ArrayList<>();
    private List<Double> standardDeviations = new ArrayList<>();

    // Zbieram statystyki, bo potrzebuję monitorować zmiany obciążenia procesorów w czasie
    public void collectStatistics(ArrayList<Processor> processors) {
        double avg = calculateAverageLoad(processors);
        double stdDev = calculateStandardDeviation(processors, avg);

        averageLoads.add(avg);
        standardDeviations.add(stdDev);
    }

    // Obliczam średnie obciążenie, bo to podstawowa miara efektywności dystrybucji zadań
    private double calculateAverageLoad(ArrayList<Processor> processors) {
        return processors.stream()
                .mapToDouble(Processor::getLoad)
                .average()
                .orElse(0.0);
    }

    // Obliczam odchylenie standardowe, bo to miara równomierności rozłożenia obciążenia
    // Niskie odchylenie oznacza lepszą równowagę obciążenia między procesorami
    private double calculateStandardDeviation(ArrayList<Processor> processors, double average) {
        double variance = processors.stream()
                .mapToDouble(p -> Math.pow(p.getLoad() - average, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    // Obliczam średnią z wszystkich pomiarów, bo potrzebuję podsumować wyniki całej symulacji
    public double getFinalAverage() {
        return averageLoads.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    // Obliczam średnie odchylenie z całej symulacji, bo to pozwala ocenić
    // stabilność rozkładu obciążenia w czasie
    public double getFinalStandardDeviation() {
        return standardDeviations.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}
