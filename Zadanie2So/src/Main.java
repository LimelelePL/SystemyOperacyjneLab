import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Disk disk = new Disk(53, 200);
        Generator generator = new Generator();

        List<Process> queue= generator.generate(100, 200, 200, 1000);

        List<Process> processes=new ArrayList<>();
        processes.add(new Process("P1", 0, 98, 999));
        processes.add(new Process("P2", 0, 183, 999));
        processes.add(new Process("P3", 0, 37, 999));
        processes.add(new Process("P4", 0, 122, 999));
        processes.add(new Process("P5", 0, 14, 999));
        processes.add(new Process("P6", 0, 124, 999));
        processes.add(new Process("P7", 0, 65, 999));
        processes.add(new Process("P8", 0, 67, 999));

        SCAN scan =new SCAN(disk);
        scan.run(processes);
        System.out.println(disk.getTotalHeadMovements());
        System.out.println(scan.getAverageWaitTime());
        System.out.println(scan.getReturns());
        disk.reset();
    }
}
