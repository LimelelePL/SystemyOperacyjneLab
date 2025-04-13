import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Disk disk = new Disk(53, 199);
        List<Process> processes=new ArrayList<>();
        processes.add(new Process("P1", 1, 98, 999, true));
        processes.add(new Process("P2", 2, 183, 999,true));
        processes.add(new Process("P3", 3, 37, 999,true));
        processes.add(new Process("P4", 4, 122, 999,true));
        processes.add(new Process("P5", 5, 14, 999,true));
        processes.add(new Process("P6", 6, 124, 999,true));
        processes.add(new Process("P7", 7, 65, 999,true));
        processes.add(new Process("P8", 8, 67, 999,true));

        SCAN scan =new SCAN(disk);
        scan.run(processes);
        System.out.println(disk.getTotalHeadMovements());
        System.out.println(scan.getAverageWaitTime());
        System.out.println(scan.getReturns());
        System.out.println(scan.getStarvedProcesses());
        disk.reset();

    }
}

