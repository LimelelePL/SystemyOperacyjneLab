
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        System.out.println("LEGENDA");
        System.out.println(" CZAS REALIZACJI- To całkowity czas od momentu pojawienia się procesu \n" +
               " w systemie (moment arrival time) aż do zakończenia jego wykonania (moment completion time). ");
        System.out.println(" CZAS OCZEKIWANIA -Jest to całkowity czas, jaki proces spędza w kolejce gotowych (ready queue) \n" +
                " oczekując na wykonanie przez CPU.");

        System.out.println("\n====== TEST 1: Procesy rzadkie, krótkie ======");
        runAll(4000, 0.01, 10, 3);

        System.out.println("\n====== TEST 2: Procesy rzadkie, długie ======");
        runAll(4000, 0.01, 180, 50);

        System.out.println("\n====== TEST 3: Wszystkie procesy naraz, bardzo krótkie  ======");
        runAll(4000, 10, 1, 1);

        System.out.println("\n====== TEST 4: Dużo procesów naraz, bardzo długie zadania ======");
        runAll(4000, 1, 100, 30);

        System.out.println("\n====== TEST 5: Procesy umiarkowane, średnia długość zadań ======");
        runAll(4000, 0.075, 20, 7);

    }

    public static void runAll(int n, double lambda, int maxBurstTime, int quantum) {
        List<Process> processes = GenerateTests.generateTests(n, lambda, maxBurstTime);

        PriorityQueue<Process> fcfsQueue = new PriorityQueue<>(Comparator.comparing(Process::getArrivalTime));
        processes.forEach(p -> fcfsQueue.add(new Process(p.getName(), p.getArrivalTime(), p.getBurstTime())));
        Fcfs fcfs = new Fcfs();
        fcfs.run(fcfsQueue);
        printStats("FCFS", fcfs.getAvgWaitingTime(), fcfs.getAvgTurnaroundTime(),
                fcfs.getMedianWaitingTime(), fcfs.getMedianTurnaroundTime(),
                fcfs.getSumOfProcessSwaps(), fcfs.getMaxWaitingTime(), fcfs.getMaxTurnaroundTime());

        PriorityQueue<Process> sjfQueue = new PriorityQueue<>(Comparator.comparing(Process::getArrivalTime));
        processes.forEach(p -> sjfQueue.add(new Process(p.getName(), p.getArrivalTime(), p.getBurstTime())));
        Sjf sjf = new Sjf();
        sjf.run(sjfQueue);
        printStats("SJF", sjf.getAvgWaitingTime(), sjf.getAvgTurnaroundTime(),
                sjf.getMedianWaitingTime(), sjf.getMedianTurnaroundTime(),
                sjf.getSumOfProcessSwaps(), sjf.getMaxWaitingTime(), sjf.getMaxTurnaroundTime());
        System.out.println("Liczba zagłodzonych procesów: " + sjf.getStarvedProcesses());

        PriorityQueue<Process> rrQueue = new PriorityQueue<>(Comparator.comparing(Process::getArrivalTime));
        processes.forEach(p -> rrQueue.add(new Process(p.getName(), p.getArrivalTime(), p.getBurstTime())));
        RoundRobin rr = new RoundRobin();
        rr.setQuantum(quantum);
        rr.run(rrQueue);
        printStats("Round Robin (q=" + quantum + ")", rr.getAvgWaitingTime(), rr.getAvgTurnaroundTime(),
                rr.getMedianWaitingTime(), rr.getMedianTurnaroundTime(),
                rr.getSumOfProcessSwaps(), rr.getMaxWaitingTime(), rr.getMaxTurnaroundTime());
    }

    private static void printStats(String algorithm, double avgWait, double avgTurnaround,
                                   double medianWait, double medianTurnaround,
                                   double swaps, double maxWait, double maxTurnaround) {
        System.out.printf("\n--- %s ---\n", algorithm);
        System.out.printf("Średni czas oczekiwania: %.2f\n", avgWait);
        System.out.printf("Średni czas realizacji: %.2f\n", avgTurnaround);
        System.out.printf("Mediana czas oczekiwania: %.2f\n", medianWait);
        System.out.printf("Mediana czas realizacji: %.2f\n", medianTurnaround);
        System.out.printf("Liczba przełączeń: %.0f\n", swaps);
        System.out.printf("Maksymalny czas oczekiwania: %.0f\n", maxWait);
        System.out.printf("Maksymalny czas realizacji: %.0f\n", maxTurnaround);
    }
}
