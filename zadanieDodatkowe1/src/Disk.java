public class Disk {
    private int readLatency;
    private int writeLatency;
    private int gcLatency;
    private int gcTreshold;
    private int writesSinceLastGC;

    public Disk(int gcLatency, int gcTreshold, int readLatency, int writeLatency) {
        this.gcLatency = gcLatency;
        this.gcTreshold = gcTreshold;
        this.readLatency = readLatency;
        this.writeLatency = writeLatency;
        this.writesSinceLastGC = 0;
    }

    public void resetWritesSinceLastGC() {
       this.writesSinceLastGC= 0;
    }
    public int getGcLatency() {
        return gcLatency;
    }

    public void setGcLatency(int gcLatency) {
        this.gcLatency = gcLatency;
    }

    public int getGcTreshold() {
        return gcTreshold;
    }

    public void setGcTreshold(int gcTreshold) {
        this.gcTreshold = gcTreshold;
    }

    public int getReadLatency() {
        return readLatency;
    }

    public void setReadLatency(int readLatency) {
        this.readLatency = readLatency;
    }

    public int getWriteLatency() {
        return writeLatency;
    }

    public void setWriteLatency(int writeLatency) {
        this.writeLatency = writeLatency;
    }

    public int getWritesSinceLastGC() {
        return writesSinceLastGC;
    }

    public void incrementWritesSinceLastGC() {
        this.writesSinceLastGC++;
    }
}