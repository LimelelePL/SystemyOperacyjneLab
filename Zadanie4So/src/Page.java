
public class Page {
    public final int id;
    public final int processID;
    public int lastUsed;

    public Page(int id, int lastUsed, int processID) {
        this.id = id;
        this.lastUsed = lastUsed;
        this.processID = processID;
    }

    @Override
    public String toString() {
        return "[Nr:" + id + " | proc:" + processID + " | ref:" + lastUsed + "]";
    }
}
