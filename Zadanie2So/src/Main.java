import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Disk disk = new Disk(53, 199);
        Generator generator=new Generator();
        List<Process> d= generator.generateInBothEdges(1000,199,5000,  2000);
        List<Process> processes=new ArrayList<>();
        processes.add(new Process("P1", 1, 98, 999, false));
        processes.add(new Process("P2", 2, 183, 999,false));
        processes.add(new Process("P3", 3, 37, 999,true));
        processes.add(new Process("P4", 4, 122, 999,true));
        processes.add(new Process("P5", 5, 14, 999,false));
        processes.add(new Process("P6", 6, 124, 999,false));
        processes.add(new Process("P7", 7, 65, 999,true));
        processes.add(new Process("P8", 8, 67, 999,false));

        SCAN scan =new SCAN(disk);
        System.out.println("SCAN NORMAL");
        scan.run(new ArrayList<>(d));
        System.out.println("total head movements " + disk.getTotalHeadMovements());
        System.out.println("avg waititme " + scan.getAverageWaitTime());
        System.out.println("returns " + scan.getReturns());
        System.out.println("staverd " +  scan.getStarvedProcesses());
        disk.reset();

        FDScan scan1 =new FDScan(disk);
        scan1.run(new ArrayList<>(d));
        System.out.println("FDSCAN");
        System.out.println("total head movements " + disk.getTotalHeadMovements());
        System.out.println("avg waititme " + scan1.getAverageWaitTime());
        System.out.println("returns " + scan1.getReturns());
        System.out.println("staverd " +  scan1.getStarvedProcesses());
        disk.reset();

    }
}

