import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Disk disk = new Disk(53);
        Generator generator = new Generator();

        List<Process> queue= generator.generate(100,1000, disk.getMaxPosition(), 1000);

        List<Process> processes=new ArrayList<>();
        processes.add(new Process("P1", 1, 98, 999));
        processes.add(new Process("P2", 2, 183, 999));
        processes.add(new Process("P3", 3, 37, 999));
        processes.add(new Process("P4", 4, 122, 999));
        processes.add(new Process("P5", 5, 14, 999));
        processes.add(new Process("P6", 6, 124, 999));
        processes.add(new Process("P7", 7, 65, 999));
        processes.add(new Process("P8", 8, 67, 999));

        FCFS fcfs=new FCFS(disk);
        fcfs.run(processes);
        System.out.println(disk.getTotalHeadMovements());
        System.out.println(fcfs.getAverageWaitTime());
        disk.reset();
    }
}
