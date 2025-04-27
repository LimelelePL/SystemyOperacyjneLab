import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {
    public List<Request> generate(int size, int maxArrivalTime, int maxDeadLine, int SectorNumbers, int writeFrequency){
        Random rand = new Random();
        List<Request> requests = new ArrayList<>();
        int count=0;
        for(int i = 0; i < size; i++){
            int arrivalTime=rand.nextInt(maxArrivalTime);
            int deadLine=rand.nextInt(maxDeadLine*100);
            int sectorNumber=rand.nextInt(SectorNumbers);
            String type="READ";

            if(count==writeFrequency){
                type="WRITE";
                count=0;
            }
            int ID=i;

            requests.add(new Request(ID,type,arrivalTime,sectorNumber,deadLine ));
            count++;
        }
        return requests;
    }
}
