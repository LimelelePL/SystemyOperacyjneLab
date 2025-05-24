public interface PageReplacementAlgorithm {
    /**
     * Wykonuje cały algorytm i zwraca liczbę błędów stron 
     */
    int execute();

    /**
     * Nazwa algorytmu (do raportowania)
     */
    String getName();
    
    /**
     * Zwraca liczbę szamotań (thrashing)
     */
    int getThrashing();
    
    /**
     * Zwraca liczbę wstrzymań procesów
     */
    int getSuspensions();

    int getZoneCoef();
}
