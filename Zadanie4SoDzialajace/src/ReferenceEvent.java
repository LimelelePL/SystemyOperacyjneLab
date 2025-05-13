public class ReferenceEvent {
    private int pid;     // który proces
    private Page page;   // do której strony

    public ReferenceEvent(int pid, Page page) {
        this.pid = pid;
        this.page = page;
    }


    public int getPid() {
        return pid;
    }

    public Page getPage() {
        return page;
    }
}