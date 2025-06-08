import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class BPlusTree {
    private final int t;
    private final BufferManager bufferManager;
    private Swip rootSwip;                      // swip korzenia na dysku/pamięci
    private final ReentrantReadWriteLock treeLatch = new ReentrantReadWriteLock(true);

    // Mapa pageId, obiekt Node (w pamięci), by przy swizzlingu powiązać stronę z Node
    private final Map<Long, Node> pageToNode = new HashMap<>();

    Random rand=new Random();
    public BPlusTree(int t, BufferManager bm) {
        this.t = t;
        this.bufferManager = bm;
        int size=rand.nextInt(0,1);
        Page rootPage = new Page(1L, size);
        bufferManager.fetchPage(1L, size);
        this.rootSwip = new Swip(rootPage);

        Node rootNode = new Node(true);
        rootNode.myPage = rootPage;
        pageToNode.put(1L, rootNode);
    }

    /** Reprezentacja węzła Node w pamięci
     *  - jeżeli isLeaf==true  to ma List<Long> keys + List<Long> values
     *  - jeżeli isLeaf==false to  ma List<Long> keys + List<Swip> children
     *  - każde Node trzyma referencję do swojej Page (myPage)
     */
    private class Node {
        final boolean isLeaf;
        final List<Long> keys = new ArrayList<>();
        final List<Long> values = new ArrayList<>();
        final List<Swip> children = new ArrayList<>();
        Page myPage;

        Node(boolean isLeaf) {
            this.isLeaf = isLeaf;
        }
    }

    //latch drzewa w write
    public void insert(long key, long value) {
        treeLatch.writeLock().lock();
        try {
            Node rootNode = loadNode(rootSwip);
            if (rootNode == null) {
                throw new IllegalStateException("Root node nie został załadowany");
            }

            SplitResult sr = insertRecursive(rootNode, key, value);

            if (sr != null) {
                // Trzeba splitnąć korzeń
                Node newRoot = new Node(false);
                newRoot.keys.add(sr.pivotKey);
                newRoot.children.add(sr.leftSwip);
                newRoot.children.add(sr.rightSwip);

                // Stwórz nową fizyczną stronę dla nowego korzenia
                long newRootId = generateNewPageId();
                int s=rand.nextInt(0,1);
                Page newRootPage = new Page(newRootId, s);
                bufferManager.fetchPage(newRootId, s);
                swizzleNode(newRoot, newRootPage);

                // Zaktualizuj Swip korzenia
                this.rootSwip = new Swip(newRootPage);
            }
        } finally {
            treeLatch.writeLock().unlock();
        }
    }

    //latch drzewa w read (dopuszczamy wiele jednoczesnych search)
    public Long search(long key) {
        treeLatch.readLock().lock();
        try {
            return searchRecursive(rootSwip, key);
        } finally {
            treeLatch.readLock().unlock();
        }
    }

    private Long searchRecursive(Swip currentSwip, long key) {
        Node node = loadNode(currentSwip);

        if (node == null) return null;

        node.myPage.latchShared();
        try {
            if (node.isLeaf) {
                // W liściu przeszukujemy liniowo (klucze są posortowane)
                for (int i = 0; i < node.keys.size(); i++) {
                    if (node.keys.get(i) == key) {
                        return node.values.get(i);
                    }
                }
                return null;
            } else {
                // W węźle wewnętrznym binarySearch po kluczach
                int idx = Collections.binarySearch(node.keys, key);
                int childIdx = (idx >= 0) ? idx + 1 : -idx - 1;
                Swip childSwip = node.children.get(childIdx);
                return searchRecursive(childSwip, key);
            }
        } finally {
            node.myPage.unlatchShared();
        }
    }

    /**
     * Rekurencyjny insert:
     *  - Jeżeli węzeł jest liściem i nie przepelniony -> wstaw, zwróć null.
     *  - Jeśli liść przepełniony -> split liścia, zwracamy SplitResult.
     *  - Jeśli węzeł wewnętrzny -> schodzimy do dziecka, sprawdzamy czy split, ewentualnie wstawiamy pivot i
     *    jeśli węzeł wewnętrzny przepelniony -> split węzła wewnętrznego, zwroc SplitResult.
     */
    private SplitResult insertRecursive(Node node, long key, long value) {
        node.myPage.latchExclusive();
        try {
            if (node.isLeaf) {
                int idx = Collections.binarySearch(node.keys, key);
                if (idx >= 0) {
                    // Nadpisujemy istniejącą wartość
                    node.values.set(idx, value);
                    persistNode(node);
                    return null;
                }
                int insertPos = (idx >= 0) ? idx : -idx - 1;
                node.keys.add(insertPos, key);
                node.values.add(insertPos, value);
                if (node.keys.size() <= t - 1) {
                    persistNode(node);
                    return null;
                }
                int mid = node.keys.size() / 2;
                Node right = new Node(true);
                List<Long> rightKeys = new ArrayList<>(node.keys.subList(mid, node.keys.size()));
                List<Long> rightVals = new ArrayList<>(node.values.subList(mid, node.values.size()));
                right.keys.addAll(rightKeys);
                right.values.addAll(rightVals);

                node.keys.subList(mid, node.keys.size()).clear();
                node.values.subList(mid, node.values.size()).clear();

                // Zapisujemy obie strony na dysk
                persistNode(node); // nadpisz lewego (jego myPage już istnieje)
                long rightId = generateNewPageId();
                int s1=rand.nextInt(0,1);
                Page rightPage = new Page(rightId, s1);
                bufferManager.fetchPage(rightId, s1);
                swizzleNode(right, rightPage);

                long pivot = right.keys.get(0);
                Swip leftSwip = new Swip(node.myPage);
                Swip rightSwip = new Swip(right.myPage);
                return new SplitResult(pivot, leftSwip, rightSwip);

            } else {
                // Węzeł wewnętrzny
                int idx = Collections.binarySearch(node.keys, key);
                int childIdx = (idx >= 0) ? idx + 1 : -idx - 1;
                Swip childSwip = node.children.get(childIdx);
                Node childNode = loadNode(childSwip);
                SplitResult sr = insertRecursive(childNode, key, value);
                if (sr == null) {
                    persistNode(node);
                    return null;
                }
                // dziecko splitnęło się: wstaw pivotKey i dwie Swipy
                int pos = Collections.binarySearch(node.keys, sr.pivotKey);
                int insertPos = (pos >= 0) ? pos + 1 : -pos - 1;
                node.keys.add(insertPos, sr.pivotKey);
                node.children.set(insertPos, sr.leftSwip);
                node.children.add(insertPos + 1, sr.rightSwip);
                if (node.keys.size() <= t - 1) {
                    persistNode(node);
                    return null;
                }
                // split węzła wewnętrznego
                int mid = node.keys.size() / 2;
                long pivotUp = node.keys.get(mid);

                Node right = new Node(false);
                List<Long> rightKeys = new ArrayList<>(node.keys.subList(mid + 1, node.keys.size()));
                right.keys.addAll(rightKeys);
                node.keys.subList(mid, node.keys.size()).clear(); // usuwamy pivot i prawą część

                List<Swip> rightChildren = new ArrayList<>(node.children.subList(mid + 1, node.children.size()));
                right.children.addAll(rightChildren);
                node.children.subList(mid + 1, node.children.size()).clear();

                // Zapisywanie obu stron
                persistNode(node);
                long rightId = generateNewPageId();
                int s2=rand.nextInt(0,1);
                Page rightPage = new Page(rightId, s2);
                bufferManager.fetchPage(rightId, s2);
                swizzleNode(right, rightPage);

                Swip leftSwip = new Swip(node.myPage);
                Swip rightSwip = new Swip(right.myPage);
                return new SplitResult(pivotUp, leftSwip, rightSwip);
            }
        } finally {
            node.myPage.unlatchExclusive();
        }
    }


    private void persistNode(Node node) {
        // TODO ALE NIE POTRZEBNE REALNIE DO ZAPISU JAK MI SIE BEDZIE CHICALO
    }

    /**
     * Wczytaj node z danej Swip:
     *  - Jeżeli Swip swizzlowany pobieramy istniejący Node z pageToNode
     *  - Jeżeli Swip nieswizzlowany to fetchPage otrzymujemy nowy Swip swizzlowany,
     *    dopisujemy do pageToNode (lub odczytujemy już istniejący), i zwracamy Node.
     */
    private Node loadNode(Swip swip) {
        if (swip.isSwizzled()) {
            Page p = swip.getPageRef();
            return pageToNode.get(p.pageId);
        } else {
            long pid = swip.getPageId();
            int sc = swip.getSizeClass();
            Swip newSwip = bufferManager.fetchPage(pid, sc);
            Page p = newSwip.getPageRef();

            Node node = pageToNode.get(pid);
            if (node == null) {
                node = new Node(true);
                node.myPage = p;
                pageToNode.put(pid, node);
            }
            return node;
        }
    }

    // wiążemy obiekt Node z obiektem Page i zarejestruj w pageToNode
    private void swizzleNode(Node node, Page p) {
        node.myPage = p;
        pageToNode.put(p.pageId, node);
    }

    // Generowanie kolejnych unikalnych pageId musi byc synchronizowane miedzy watpkami raczej
    private static long nextPageId = 2;
    private static synchronized long generateNewPageId() {
        return nextPageId++;
    }

    // Obiekt zwracany przy splitcie: pivotKey + Swipy do lewego i prawego „połówek”
    private static class SplitResult {
        final long pivotKey;
        final Swip leftSwip, rightSwip;

        SplitResult(long pivotKey, Swip left, Swip right) {
            this.pivotKey = pivotKey;
            this.leftSwip = left;
            this.rightSwip = right;
        }
    }

    /**
     * Odswizzlowanie ustaw wszystkie Swipy w drzewie na nieswizzlowane,
     * tak aby przy kolejnym search każdy krok wywoła fetchPage
     * Od-swizzlowanie oznacza: zastąp Swip(Page) → Swip(pageId, sizeClass=0).
     */
    public void unswizzleTree() {
        Node rootNode = loadNode(rootSwip);
        if (rootNode == null) return;
        int s4= rand.nextInt(0,1);

        long rootPageId = rootNode.myPage.pageId;
        this.rootSwip = new Swip(rootPageId, s4);

        unswizzleNodeRecursively(rootSwip);
    }

    private void unswizzleNodeRecursively(Swip swip) {
        Node node = loadNode(swip);
        if (node == null) return;

        if (!node.isLeaf) {
            for (int i = 0; i < node.children.size(); i++) {
                Swip childSwip = node.children.get(i);
                long childPageId = loadNode(childSwip).myPage.pageId;
                int s3= rand.nextInt(0,1);
                Swip newChildSwip = new Swip(childPageId, s3);
                node.children.set(i, newChildSwip);
                unswizzleNodeRecursively(newChildSwip);
            }
        }
    }

}
