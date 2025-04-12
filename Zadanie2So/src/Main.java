import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Disk disk1 = new Disk(50, 199);
        Disk disk2 = new Disk(50, 199);

        List<Process> processes = new ArrayList<>();
        // Niewykonalny proces real-time dla FD-SCAN
        processes.add(new Process("P1", 0, 180, 40, true));
        // Wykonalny
        processes.add(new Process("P2", 0, 60, 1000, true));
        // Normalny
        processes.add(new Process("P3", 0, 30, 1000, false));

        // SCAN (NORMAL)
        SCAN scanNormal = new SCAN(disk1, SCAN.ScanMode.NORMAL);
        scanNormal.run(new ArrayList<>(processes));
        System.out.println("== SCAN ==");
        System.out.println("TotalHeadMovements: " + disk1.getTotalHeadMovements());
        System.out.println("Starved: " + scanNormal.getStarvedProcesses());

        System.out.println();

        // FD-SCAN
        SCAN scanFD = new SCAN(disk2, SCAN.ScanMode.FD_SCAN);
        scanFD.run(new ArrayList<>(processes));
        System.out.println("== FD-SCAN ==");
        System.out.println("TotalHeadMovements: " + disk2.getTotalHeadMovements());
        System.out.println("Starved: " + scanFD.getStarvedProcesses());
    }
}

