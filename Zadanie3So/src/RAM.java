import java.util.ArrayList;

public class RAM {
    private Page[] ram;
    private int size;

    public RAM(int size) {
        this.size = size;
        ram=new Page[size];
    }

    public void insert(int index, Page page) {
        ram[index]=page;
    }

    public boolean contains(Page page) {
        for (int i=0;i<size;i++){
            if(ram[i] != null && (ram[i].getID().equals(page.getID()))){
                return true;
            }
        }
        return false;
    }

    public Page get(int index){
        return ram[index];
    }

    public boolean hasEmptyIndes(){
        return getEmptyIndex()!=-1;
    }

    public int getEmptyIndex(){
        for (int i=0;i<size;i++){
            if(ram[i]==null){
                return i;
            }
        }
        return -1;
    }

    public int findIndex(Page page) {
        for (int i = 0; i < size; i++) {
            if (ram[i] != null && ram[i].getID().equals(page.getID())) {
                return i;
            }
        }
        return -1;
    }

    public void addToAnEmptyIndex(Page page){
        int emptyIndex=getEmptyIndex();
        if(emptyIndex!=-1){
            insert(emptyIndex,page);
        }
    }

    public void reset(){
        ram=new Page[size];
    }

    public int getSize() {
        return size;
    }

}
