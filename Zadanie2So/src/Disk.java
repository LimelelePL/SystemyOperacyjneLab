public class Disk {
    private int currentPosition;
    private int totalHeadMovements;
    private int maxPosition;
    private int startPosition;

    public Disk(int startPosition, int maxPosition) {
        this.currentPosition=startPosition;
        this.totalHeadMovements = 0;
        this.maxPosition = maxPosition;
    }

    public int moveTo(int newPosition) {
        if(newPosition > maxPosition) {
            throw new IndexOutOfBoundsException("Wykoczono ponad maksymalna pozycje głowicy");
        }

        int movement = Math.abs(newPosition - currentPosition);

        totalHeadMovements += movement;
        currentPosition = newPosition;

        return movement;
    }

    public int getTimeToMove(int target) {
        return Math.abs(currentPosition - target);
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getTotalHeadMovements() {
        return totalHeadMovements;
    }

    public int getMaxPosition() {
        return maxPosition;
    }

    public void reset() {
        currentPosition = 0;
        totalHeadMovements = 0;
    }
}