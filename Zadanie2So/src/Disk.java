public class Disk {
    private int currentPosition;
    private int totalHeadMovements;
    final static int MAX_POSITION=1000;
    private int startPosition;

    public Disk(int startPosition) {
        this.currentPosition=startPosition;
        this.totalHeadMovements = 0;
    }

    public int moveTo(int newPosition) {
        if(newPosition > MAX_POSITION) {
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
        return MAX_POSITION;
    }

    public void reset() {
        currentPosition = 0;
        totalHeadMovements = 0;
    }
}