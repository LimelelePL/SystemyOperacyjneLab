import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Disk disk = new Disk(53, 199);
        List<Process> processes=new ArrayList<>();
        processes.add(new Process("P1", 0, 98, 999, true));
        processes.add(new Process("P2", 0, 183, 999,true));
        processes.add(new Process("P3", 0, 37, 999,true));
        processes.add(new Process("P4", 0, 122, 999,true));
        processes.add(new Process("P5", 0, 14, 999,true));
        processes.add(new Process("P6", 0, 124, 999,true));
        processes.add(new Process("P7", 0, 65, 999,true));
        processes.add(new Process("P8", 0, 67, 999,true));

        SSTF scan =new SSTF(disk);
        scan.run(processes);
        System.out.println(disk.getTotalHeadMovements());
        System.out.println(scan.getAverageWaitTime());
        disk.reset();
    }
}

