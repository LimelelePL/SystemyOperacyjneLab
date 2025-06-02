
public class Swip {
    private final boolean isSwizzled;
    private final long pageId;     // ważne tylko gdy isSwizzled==false
    private final int sizeClass;   // ważne tylko gdy isSwizzled==false
    private final Page pageRef;    // ważne tylko gdy isSwizzled==true

    /** Konstruktor dla nieswizzlowanego (trzyma pageId+sizeClass) */
    public Swip(long pageId, int sizeClass) {
        this.isSwizzled = false;
        this.pageId = pageId;
        this.sizeClass = sizeClass;
        this.pageRef = null;
    }

    /** Konstruktor dla swizzlowanego (trzymaj referencję Page) */
    public Swip(Page p) {
        this.isSwizzled = true;
        this.pageId = -1;
        this.sizeClass = -1;
        this.pageRef = p;
    }

    public boolean isSwizzled() {
        return isSwizzled;
    }


    public long getPageId() {
        if (isSwizzled) throw new IllegalStateException("Already swizzled");
        return pageId;
    }


    public int getSizeClass() {
        if (isSwizzled) throw new IllegalStateException("Already swizzled");
        return sizeClass;
    }


    public Page getPageRef() {
        if (!isSwizzled) throw new IllegalStateException("Not swizzled yet");
        return pageRef;
    }
}
