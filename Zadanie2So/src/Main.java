import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Disk disk = new Disk(53, 199);
        Generator generator=new Generator();
        List<Process> d= generator.generateRandom(1000,199,5000,  1500);
        List<Process> processes=new ArrayList<>();
        processes.add(new Process("P1", 1, 98, 999, false));
        processes.add(new Process("P2", 2, 183, 999,false));
        processes.add(new Process("P3", 3, 37, 999,true));
        processes.add(new Process("P4", 4, 122, 999,true));
        processes.add(new Process("P5", 5, 14, 999,false));
        processes.add(new Process("P6", 6, 124, 999,false));
        processes.add(new Process("P7", 7, 65, 999,true));
        processes.add(new Process("P8", 8, 67, 999,false));

        EDF scan =new EDF(disk);
        System.out.println("EDF");
        scan.run(new ArrayList<>(d));
        System.out.println("total head movements " + disk.getTotalHeadMovements());
        System.out.println("avg waititme " + scan.getAverageWaitTime());
        System.out.println("staverd " +  scan.getStarved());
        disk.reset();

        SSTF scan1 =new SSTF(disk);
        scan1.run(new ArrayList<>(d));
        System.out.println("SSTF");
        System.out.println("total head movements " + disk.getTotalHeadMovements());
        System.out.println("avg waititme " + scan1.getAverageWaitTime());
        System.out.println("staverd " +  scan1.getStarvedProcesses());
        disk.reset();

    }
}

