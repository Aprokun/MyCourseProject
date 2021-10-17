package model;

public class TransportTaskTable {
    public int[] storage;
    public boolean[] storageAvailable;
    public int storageAmount;

    public int[] need;
    public boolean[] needAvailable;
    public int customerAmount;

    public int[][] cost;

    public TransportTaskTable(int[] storage, int storageAmount, int[] need, int customerAmount, int[][] cost) {
        this.storage = storage;
        this.storageAmount = storageAmount;
        storageAvailable = new boolean[storageAmount];
        setAllTrue(storageAvailable, storageAvailable.length);
        this.need = need;
        this.customerAmount = customerAmount;
        needAvailable = new boolean[customerAmount];
        setAllTrue(needAvailable, needAvailable.length);
        this.cost = cost;
    }

    private void setAllTrue(boolean[] a, int size) {
        for (int i = 0; i < size; i++) {
            a[i] = true;
        }
    }

    public void setStorage(int[] storage) {
        this.storage = storage;
    }

    public void setNeed(int[] need) {
        this.need = need;
    }

    public void setCost(int[][] cost) {
        this.cost = cost;
    }
}
