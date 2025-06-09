package Statistics;

import Processor.Processor;

import java.util.ArrayList;

public class LoadStatistics {
    // Przechowuję sumy ważone czasem oraz całkowity czas,
    // bo potrzebuję analizować zmienność obciążenia w trakcie symulacji uwzględniając czas trwania każdego stanu.
    private double weightedLoadSum;
    private double weightedVarianceSum;
    private int totalTimeElapsed;

    public LoadStatistics() {
        this.weightedLoadSum = 0.0;
        this.weightedVarianceSum = 0.0;
        this.totalTimeElapsed = 0;
    }

    // Zbieram statystyki, uwzględniając czas trwania danego stanu (ticka)
    public void collectStatistics(ArrayList<Processor> processors, int timeForThisTick) {
        if (timeForThisTick <= 0) {
            return; // Nie zbieraj statystyk, jeśli czas się nie zmienił lub jest niepoprawny
        }

        double currentAverage = calculateAverageLoad(processors);
        double currentVariance = calculateVariance(processors, currentAverage);

        this.weightedLoadSum += currentAverage * timeForThisTick;
        this.weightedVarianceSum += currentVariance * timeForThisTick;
        this.totalTimeElapsed += timeForThisTick;
    }

    // Obliczam średnie obciążenie dla bieżącego stanu
    private double calculateAverageLoad(ArrayList<Processor> processors) {
        return processors.stream()
                .mapToDouble(Processor::getLoad)
                .average()
                .orElse(0.0);
    }

    // Obliczam wariancję dla bieżącego stanu
    private double calculateVariance(ArrayList<Processor> processors, double average) {
        return processors.stream()
                .mapToDouble(p -> Math.pow(p.getLoad() - average, 2))
                .average()
                .orElse(0.0);
    }

    // Obliczam średnią ważoną czasem z wszystkich pomiarów
    public double getFinalAverage() {
        if (totalTimeElapsed == 0) {
            return 0.0;
        }
        return weightedLoadSum / totalTimeElapsed;
    }

    // Obliczam odchylenie standardowe na podstawie średniej ważonej czasem wariancji
    public double getFinalStandardDeviation() {
        if (totalTimeElapsed == 0) {
            return 0.0;
        }
        double meanWeightedVariance = weightedVarianceSum / totalTimeElapsed;
        return Math.sqrt(meanWeightedVariance);
    }
}