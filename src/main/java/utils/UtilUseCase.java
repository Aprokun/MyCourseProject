package utils;

import model.Shipment;
import model.TransportTaskTable;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.toCollection;

public class UtilUseCase {
    public static final Scanner in = new Scanner(System.in);
    public static int PROBLEM_TYPE;


    /**
     * ���� �������.
     *
     * @param a ������.
     * @param n ����� �������.
     */
    public static void inputArray(int[] a, final int n) {
        for (int i = 0; i < n; ++i) a[i] = in.nextInt();
    }


    /**
     * ���� �������.
     *
     * @param m ���������� �����.
     * @param n ���������� ��������.
     * @param a �������.
     */
    public static void inputMatrix(final int m, final int n, int[][] a) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                a[i][j] = in.nextInt();
            }
        }
    }

    /**
     * ������ ���� ��� ���������
     *
     * @param basePlan ������� ���� ������������ ������.
     * @param s        ���������, ��� ������� �������������� ���������� �����.
     * @return ������ ��������� ����� �����������, ����� - ������ ������.
     */
    static Shipment[] getClosedPath(Shipment[][] basePlan, Shipment s) {
        LinkedList<Shipment> path = matrixToList(basePlan);
        path.addFirst(s);

        //������� ���������, � ������� ��� ���� �� ������ ������ �� �������/������
        while (path.removeIf(sm -> {
            Shipment[] neighboors = getNeighbors(sm, path);
            return neighboors[0] == null || neighboors[1] == null;
        })) ;

        Shipment[] stepStones = path.toArray(new Shipment[0]);
        Shipment prev = s;
        for (int i = 0; i < stepStones.length; i++) {
            stepStones[i] = prev;
            prev = getNeighbors(prev, path)[i % 2];
        }

        return stepStones;
    }

    /**
     * ������������ ����� �������� ������ �� ������/�������.
     *
     * @param s    ��������� �� ������� �������� �����, ��� ������� �������������� �����
     *             �������� ������ � �����.
     * @param path ��������� ���� �����������.
     * @return ������� ��������� ����� � ��������, �������� �������� ������ �� ������/�������.
     */
    private static Shipment[] getNeighbors(Shipment s, LinkedList<Shipment> path) {
        Shipment[] neighbors = new Shipment[2];

        for (Shipment o : path) {
            if (o != s) {
                if (o.row == s.row & neighbors[0] == null) neighbors[0] = o;
                else if (o.col == s.col && neighbors[1] == null) neighbors[1] = o;
                if (neighbors[0] != null && neighbors[1] != null) break;
            }
        }

        return neighbors;
    }

    /**
     * ���������� ��������� ������������� �������� ����� ������������ ������.
     *
     * @param table    ������� ������������ ������.
     * @param basePlan ������� ���� ������������ ������.
     */
    public static void fixDegenerate(TransportTaskTable table, Shipment[][] basePlan) {
        final int eps = 0;

        if (table.storage.length + table.need.length - 1 != matrixToList(basePlan).size()) {
            for (int i = 0; i < table.storage.length; i++) {
                for (int j = 0; j < table.need.length; j++) {
                    //���� ������ ������
                    if (basePlan[i][j] == null) {
                        //������ ��������� ���������
                        Shipment dummy = new Shipment(table.cost[i][j], i, j, eps);
                        //���� � ��� ��������� ���� ������ 0 (�.�. ���������� ������� ����)
                        if (getClosedPath(basePlan, dummy).length == 0) {
                            basePlan[i][j] = dummy;
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * �������� ���������� ��������� �������� �����.
     * ����������� � ���, ��� ��� ������ ������ ������ �������� ����� �������� ���� �����������
     * � ������������ � ����������. � ����� �� �������� ����, ������� ����������� �������
     * ����� ��������� ��������� �������� �����.
     *
     * @param table    ������� ������������ ������
     * @param basePlan ������� �������� �����.
     */
    public static void steppingStone(TransportTaskTable table, Shipment[][] basePlan) {
        //������������ ���������� �������� �����
        int maxReduction = 0;
        //���� ��������� ��� ������������� ���������� �����
        Shipment[] move = null;
        //������ ������� �������� �����, ������� ������
        //��������� �� �������� ����� ����� �����������
        Shipment leaving = null;

        for (int row = 0; row < basePlan.length; row++) {
            for (int col = 0; col < basePlan[row].length; col++) {
                if (basePlan[row][col] != null) continue;

                //��������� ���������
                Shipment dummy = new Shipment(table.cost[row][col], row, col, 0);

                //�������� ���� ��� ��������� ���������
                Shipment[] path = getClosedPath(basePlan, dummy);

                //������� ���������� ����� ��������� �������� �����
                int reduction = 0;
                int lowestQuantity = Integer.MAX_VALUE;
                Shipment leavingCandidate = null;

                boolean isPlus = true;
                for (Shipment sm : path) {
                    if (isPlus) reduction += sm.costPerUnit;
                    else {
                        reduction -= sm.costPerUnit;
                        if (sm.quantity == Integer.MIN_VALUE) {
                            leavingCandidate = sm;
                            lowestQuantity = 0;
                        } else if (sm.quantity < lowestQuantity) {
                            leavingCandidate = sm;
                            lowestQuantity = sm.quantity;
                        }
                    }
                    isPlus = !isPlus;
                }

                //���� ������� ���������� ������������ ����� ����������
                //������ �������������
                if (reduction < maxReduction) {
                    move = path;
                    leaving = leavingCandidate;
                    maxReduction = reduction;
                }
            }
        }

        //���� ������ ���� ����������� � ������������ ������ ��� ��������
        //�� ������� �������� �����
        if (move != null && leaving != null) {
            performReallocation(basePlan, move, leaving);
        }
    }

    /**
     * ���������� ���������� �������� ����� ������������ ������.
     *
     * @param basePlan ������� ���� ������������ ������.
     * @param move     ���� �����������.
     * @param leaving  ������, ������� ������ ���� ������� �� ��������
     *                 ����� ����� �����������.
     */
    private static void performReallocation(Shipment[][] basePlan, Shipment[] move, Shipment leaving) {
        int minQuantity = leaving.quantity == Integer.MIN_VALUE ? 0 : leaving.quantity;
        boolean isPlus = true;
        for (Shipment sm : move) {
            sm.quantity += isPlus ? minQuantity : -minQuantity;
            if (isPlus) {
                basePlan[sm.row][sm.col] = sm;
            } else {
                if (sm.quantity == 0) {
                    basePlan[sm.row][sm.col] = null;
                } else {
                    basePlan[sm.row][sm.col] = sm;
                }
            }
            isPlus = !isPlus;
        }
    }


    /**
     * ����� ������� a ������� MxN, ��� ��� �������������� ����� ������� ����� 1, ����� - 0
     */
    static int[][] mask = new int[50][50];


    /**
     * ����� ������������ �������� � �������.
     *
     * @param m ���������� ����� �������.
     * @param n ���������� �������� �������.
     * @param a �������.
     * @return ������ �� �������� �� ������ � ������� ������������
     * �������� �������.
     */
    public static Tuple<Integer> findMinMatrix(final int m, final int n, final int[][] a) {
        int min = Integer.MAX_VALUE;

        //������������� �������
        Tuple<Integer> min_index = new Tuple<>(-1, -1);

        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                //���� �������� �� ���� ������������ �����, ������ �������� � �� ����� ����
                if (mask[i][j] != 1 && a[i][j] < min && a[i][j] != 0) {
                    min = a[i][j];
                    min_index.x = i;
                    min_index.y = j;
                }
            }
        }

        //���� ������ �������� (�� ���� � ��� ���� ������� ���������)
        //� ������ �� ��������� �� ����� ������� �����
        if (PROBLEM_TYPE != 1 && min_index.x == -1 && min_index.y == -1) {
            for (int i = 0; i < m; ++i) {
                for (int j = 0; j < n; ++j) {
                    if (mask[i][j] != 1 && a[i][j] < min) {
                        min = a[i][j];
                        min_index.x = i;
                        min_index.y = j;
                    }
                }
            }
        }

        //�������� ��������� ������� � ����� ��� ��������������
        mask[min_index.x][min_index.y] = 1;

        return min_index;
    }


    /**
     * ����� ���� ����������� ��������� � �������. ��� ������������ ������.
     *
     * @param n    ������� �������.
     * @param a    ������.
     * @param need ��������������� ������ ������������.
     * @return ������ �� ���� ����������� ��������� (x, y), ��� x < y.
     */
    public static Tuple<Integer> findTwoMinValues(final int n, final int[] a, final int[] need) {
        int min1 = Integer.MAX_VALUE, min2 = Integer.MAX_VALUE;

        for (int i = 0; i < n; ++i) {
            if (a[i] <= min1 && need[i] != 0) {
                min2 = min1;
                min1 = a[i];
            } else if (a[i] <= min2 && need[i] != 0) {
                min2 = a[i];
            }
        }

        if (min1 == Integer.MAX_VALUE && min2 != Integer.MAX_VALUE) {
            min1 = min2;
        } else if (min1 != Integer.MAX_VALUE && min2 == Integer.MAX_VALUE) {
            min2 = min1;
        }

        return new Tuple<>(min1, min2);
    }


    /**
     * ����� ���� ����������� ��������� � ������� �������.
     *
     * @param m       ���������� ����� �������.
     * @param a       �������.
     * @param col     �������, � ������� �������������� �����.
     * @param storage ��������������� ������ �������.
     * @return ������ �� ���� ����������� ��������� (x, y), ��� x < y.
     */
    static Tuple<Integer> findTwoMinValuesInCol(final int m, final int[][] a, final int col, final int[] storage) {
        int min1 = Integer.MAX_VALUE, min2 = Integer.MAX_VALUE;

        for (int i = 0; i < m; i++) {
            if (a[i][col] <= min1 && storage[i] != 0) {
                min2 = min1;
                min1 = a[i][col];
            } else if (a[i][col] <= min2 && storage[i] != 0) {
                min2 = a[i][col];
            }
        }

        if (min1 == Integer.MAX_VALUE && min2 != Integer.MAX_VALUE) {
            min1 = min2;
        } else if (min1 != Integer.MAX_VALUE && min2 == Integer.MAX_VALUE) {
            min2 = min1;
        }

        return new Tuple<>(min1, min2);

    }

    /* ���������� ������ ������������� �������� ������� a ������� n */
    public static int getIndexMax(final int n, final int[] a) {
        int max = 0, indexMax = 0;

        for (int i = 0; i < n; ++i) {
            if (a[i] >= max && a[i] != -1) {
                max = a[i];
                indexMax = i;
            }
        }

        return indexMax;
    }

    public static void calculateDiffs(TransportTaskTable table,
                                      int[] minDiffStorageValues,
                                      int[] minDiffCustomerValues) {
        //������� �������� �� �������
        for (int i = 0; i < table.storage.length; ++i) {
            final Tuple<Integer> storageMinValues = findTwoMinValues(table.need.length,
                    table.cost[i],
                    table.need);

            if (table.storage[i] != 0) minDiffStorageValues[i] = storageMinValues.y - storageMinValues.x;
            else minDiffStorageValues[i] = -1;
        }

        //������� �������� �� ��������
        for (int j = 0; j < table.need.length; ++j) {
            final Tuple<Integer> customerMinValues = findTwoMinValuesInCol(table.storage.length,
                    table.cost,
                    j,
                    table.storage);

            if (table.need[j] != 0) minDiffCustomerValues[j] = customerMinValues.y - customerMinValues.x;
            else minDiffCustomerValues[j] = -1;
        }
    }

    public static int getIndexMinRow(int customers_amount, int[][] cost, int row, int[] need) {
        int min = Integer.MAX_VALUE, min_i = Integer.MAX_VALUE;

        for (int i = 0; i < customers_amount; ++i) {
            if (cost[row][i] < min && need[i] != 0) {
                min = cost[row][i];
                min_i = i;
            }
        }

        return min_i;
    }

    public static int getIndexMinCol(int storage_amount, int[][] cost, int col, int[] storage) {
        int minValue = Integer.MAX_VALUE, res = Integer.MAX_VALUE;

        for (int j = 0; j < storage_amount; ++j) {
            if (cost[j][col] < minValue && storage[j] != 0) {
                minValue = cost[j][col];
                res = j;
            }
        }

        return res;
    }

    /**
     * ��������� ���������������� ���� ������� ��� ���������� �������� �����.
     *
     * @param table ������� ������������ ������
     * @return ������, ���� ��� ����������� ������������� � ��� ������ ������,
     * ����� - ����
     */
    public static boolean isAllConditionsMet(TransportTaskTable table) {
        return Arrays.stream(table.storage).allMatch(el -> el == 0) && Arrays.stream(table.need).allMatch(el -> el == 0);
    }

    public static void lol(TransportTaskTable table, Shipment[][] basePlan, int i, int j) {
        if (table.storage[i] >= table.need[j]) {
            basePlan[i][j] = new Shipment(table.cost[i][j], i, j, table.need[j]);
            table.storage[i] -= table.need[j];
            table.need[j] = 0;
        } else {
            basePlan[i][j] = new Shipment(table.cost[i][j], i, j, table.storage[i]);
            table.need[j] -= table.storage[i];
            table.storage[i] = 0;
        }
    }

    //���������� �������, �������������� � ���� �������, ������������� �������� � ������� a ������� MxN
    public static Tuple<Integer> findMaxMatrixMask(final int m, final int n, int[][] a) {
        Tuple<Integer> res = new Tuple<>(null, null);
        int max = 0;

        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                if (a[i][j] >= max) {
                    max = a[i][j];
                    res.x = i;
                    res.y = j;
                }
            }
        }

        if (res.x != null && res.y != null) a[res.x][res.y] = 0;
        return res;
    }

    public static void setColPriorities(TransportTaskTable table, int[][] mask) {
        for (int j = 0; j < table.need.length; j++) {
            if (table.need[j] != 0) {
                int min = Integer.MAX_VALUE;

                for (int i = 0; i < table.storage.length; ++i) {
                    if (table.cost[i][j] < min && table.storage[i] != 0) min = table.cost[i][j];
                }

                for (int i = table.storage.length - 1; i >= 0; i--) {
                    if (table.cost[i][j] == min) {
                        mask[i][j]++;
                        break;
                    }
                }
            }
        }
    }

    public static void setRowPriorities(TransportTaskTable table, int[][] mask) {
        for (int i = 0; i < table.storage.length; i++) {
            if (table.storage[i] != 0) {
                int min = Integer.MAX_VALUE;

                for (int j = 0; j < table.need.length; j++) {
                    if (table.cost[i][j] < min && table.need[j] != 0) min = table.cost[i][j];
                }

                for (int j = table.need.length - 1; j >= 0; j--) {
                    if (table.cost[i][j] == min && table.need[j] != 0) {
                        mask[i][j]++;
                        break;
                    }
                }
            }
        }
    }

    /* ���������� ������ �� ���������� (-1,-1), ���� ���� �����������, ����� - ���������� ������ (null,null). */
    public static Tuple<Integer> isOptimalBasePlan(int storageAmount, int customersAmount, int[][] deltaCost) {
        int maxAbsDelta = 0, maxAbsDeltaRow = -1, maxAbsDeltaCol = -1;

        for (int i = 0; i < storageAmount; ++i) {
            for (int j = 0; j < customersAmount; j++) {
                if (deltaCost[i][j] < 0) {
                    int absDelta = abs(deltaCost[i][j]);
                    if (absDelta > maxAbsDelta) {
                        maxAbsDelta = absDelta;
                        maxAbsDeltaRow = i;
                        maxAbsDeltaCol = j;
                    }
                }
            }
        }

        Tuple<Integer> res = new Tuple<>(null, null);

        if (maxAbsDeltaRow != -1) {
            res.x = maxAbsDeltaRow;
            res.y = maxAbsDeltaCol;
        }

        return res;
    }

    /**
     * ������������ ������� �������� ����� � ���
     *
     * @param basePlan ������� �������� �����
     * @return ���, ��������� � �������� ��������������� ������� �������� �����
     */
    static LinkedList<Shipment> matrixToList(Shipment[][] basePlan) {
        return Arrays.stream(basePlan)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .collect(toCollection(LinkedList::new));
    }

    //��������� ������ ����������� ��� ��������� ������
    static void calculateDeltaFreeCells(TransportTaskTable table,
                                        Shipment[][] basePlan,
                                        int[] u,
                                        int[] v,
                                        int[][] deltaCost) {
        for (int i = 0; i < table.storage.length; ++i) {
            for (int j = 0; j < table.need.length; ++j) {
                if (basePlan[i][j] == null) {
                    deltaCost[i][j] = table.cost[i][j] - (u[i] + v[j]);
                }
            }
        }
    }

    //��������� ������� ����������� ��� �������� (�������) ������.
    public static void calculateDeltaBasicCells(TransportTaskTable table,
                                                Shipment[][] basePlan,
                                                int[] u,
                                                int[] v) {
        boolean[] uMask = new boolean[u.length];
        boolean[] vMask = new boolean[v.length];

        u[0] = 0;
        uMask[0] = true;

        LinkedList<Integer> t1 = new LinkedList<>();
        LinkedList<Integer> t2 = new LinkedList<>();

        for (int j = 0; j < table.need.length; j++) {
            if (basePlan[0][j] != null && !vMask[j]) {
                v[j] = table.cost[0][j] - u[0];
                vMask[j] = true;
                t1.push(j);
            }
        }

        do {
            while (!t1.isEmpty()) {
                int j = t1.pop();
                for (int i = 0; i < table.storage.length; i++) {
                    if (basePlan[i][j] != null) {
                        u[i] = table.cost[i][j] - v[j];
                        uMask[i] = true;
                        t2.push(i);
                    }
                }
            }

            while (!t2.isEmpty()) {
                int i = t2.pop();
                for (int j = 0; j < table.need.length; j++) {
                    if (basePlan[i][j] != null) {
                        v[j] = table.cost[i][j] - u[i];
                        vMask[j] = true;
                        t1.push(j);
                    }
                }
            }

        } while (!isAllTrue(vMask, vMask.length) || !isAllTrue(uMask, uMask.length));
    }

    public static boolean isAllTrue(boolean[] a, int size) {
        for (int i = 0; i < size; i++) {
            if (!a[i]) return false;
        }
        return true;
    }

    /*
        ����� �����������.
        �������� �������� ������� ���� basePlan � ������������ �����.
        */
    public static boolean isOptimal(TransportTaskTable table, Shipment[][] basePlan) {
        int[] u = new int[table.storage.length];
        int[] v = new int[table.need.length];

        for (int i = 0; i < table.storage.length; i++) u[i] = -1;
        for (int i = 0; i < table.need.length; i++) v[i] = -1;

        calculateDeltaBasicCells(table, basePlan, u, v);

        int[][] deltaCost = new int[table.storage.length][table.need.length];

        calculateDeltaFreeCells(table, basePlan, u, v, deltaCost);

        Tuple<Integer> indexMaxAbsDelta = isOptimalBasePlan(table.storage.length, table.need.length, deltaCost);

        return indexMaxAbsDelta.x == null || indexMaxAbsDelta.y == null;
    }

    /*
         ���������� ����� �������� ����� ������� �������� �����
         basePlan ������� MxN � �������������� ������� ���������� cost ������� MxN.
         */
    public static int calculateBasePlan(int m, int n, int[][] cost, Shipment[][] basePlan) {
        int res = 0;

        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                if (basePlan[i][j] != null) {
                    res += cost[i][j] * basePlan[i][j].quantity;
                }
            }
        }

        return res;
    }

    /*
        ���������� 1, ���� ������ ������������ ������ �������� ��������;
        ���������� 0, ���� ������������ ������ �������� �������� � ������� ��������������� ������;
        ���������� -1, �����, ���� �������� �������� � ������� ��������������� ���������.
        */
    public static int checkOpenClose(int sumOfAllStorage, int sumOfAllNeeds) {
        int res;

        if (sumOfAllNeeds == sumOfAllStorage) {
            return 1;
        } else {
            if (sumOfAllNeeds > sumOfAllStorage) {
                res = -1;
            } else {
                res = 0;
            }
        }

        return res;
    }

    static int[] add(int[] a, int x) {
        a = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = x;
        return a;
    }

    /* �������� �������� ������������ ������ � �������� */
    public static void getBalanced(TransportTaskTable table, int sumOfAllStorage, int sumOfAllNeeds) {
        switch (PROBLEM_TYPE) {
            //���� � ��� ������� ������, ��� ������������
            case 0 -> fictionalCustomerCase(sumOfAllStorage - sumOfAllNeeds, table);

            //���� � ��� ������������ ������, ��� ���� �� �������
            case -1 -> fictionalStorageCase(sumOfAllNeeds - sumOfAllStorage, table);

            //��������� ������
            default -> {
                try {
                    throw new Exception("�������� problemType");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void fictionalCustomerCase(int delta, TransportTaskTable table) {
        table.setNeed(add(table.need, delta));

        for (int i = 0; i < table.storage.length; i++) {
            table.cost[i] = add(table.cost[i], 0);
        }
    }

    private static void fictionalStorageCase(int delta, TransportTaskTable table) {
        table.setStorage(add(table.storage, delta));

        List<int[]> newCost = Arrays.stream(table.cost).collect(Collectors.toList());

        newCost.add(new int[table.need.length]);

        table.setCost(newCost.toArray(new int[0][0]));
    }

    /**
     * ��������� ������ ������ �������.
     *
     * @param num �����, ������� ����������� ������ �������.
     * @param m   ���������� ����� �������.
     * @param n   ���������� �������� �������.
     * @param a   �������.
     */
    public static void fillMatrixWith(final int num, final int m, final int n, int[][] a) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                a[i][j] = num;
            }
        }
    }

    //������� � ������� �������� ��������� ������� �������� ����� basePlan ������� MxN
    void printRoutes(final int m, final int n, final int[][] basePlan) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                if (basePlan[i][j] != 0) {
                    System.out.println(i + " ����� ---" + basePlan[i][j] + "---> " + j + " �������");
                }
            }
        }
    }
}
