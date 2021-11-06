package utils;

import model.Shipment;
import model.TransportTaskTable;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

public class UtilUseCase {
    public static final Scanner in = new Scanner(System.in);

    /**
     * ��� ������������ ������.
     * 1 - �������� ������;
     * 0 - ������ � ��������� ������������;
     * -1 - ������ � ��������� �������;
     */
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
     * �������� �������� ����� �� �������������.
     *
     * @param basePlan ������� ����.
     * @return ������, ���� ������� ���� �������� �����������, ����� - ����.
     */
    public static boolean isDegenerate(Shipment[][] basePlan) {
        long count = Arrays.stream(basePlan).mapToLong(ints -> Arrays.stream(ints).filter(Objects::nonNull).count()).sum();
        return count < basePlan[0].length + basePlan.length - 1;
    }


    /**
     * ���������� ��������� ������������� �������� ����� ������������ ������.
     *
     * @param table    ������� ������������ ������.
     * @param basePlan ������� ���� ������������ ������.
     */
    public static void fixDegenerate(TransportTaskTable table, Shipment[][] basePlan) {
        final int eps = 0;

        if (table.supply.length + table.demand.length - 1 != matrixToList(basePlan).size()) {
            for (int i = 0; i < table.supply.length; i++) {
                for (int j = 0; j < table.demand.length; j++) {
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
     * @return ������ �� ���� ����������� ��������� (x, y), ��� x &lt; y.
     */
    public static Tuple<Integer> findTwoMinValues(final int n, final int[] a, final int[] need) {
        int min1 = Integer.MAX_VALUE, min2 = Integer.MAX_VALUE;

        for (int i = 0; i < n; ++i) {
            if (PROBLEM_TYPE == 0 && i != a.length - 1) continue;
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
     * @return ������ �� ���� ����������� ��������� (x, y), ��� x &lt; y.
     */
    static Tuple<Integer> findTwoMinValuesInCol(final int m, final int[][] a, final int col, final int[] storage) {
        int min1 = Integer.MAX_VALUE, min2 = Integer.MAX_VALUE;

        for (int i = 0; i < m; i++) {
            if (PROBLEM_TYPE == -1 && i == a.length - 1) continue;
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


    /**
     * ����� ������� ������������� �������� �������.
     *
     * @param n ����� �������.
     * @param a ������.
     * @return ������ ������������� �������� �������.
     */
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


    /**
     * ������� ������ ���� ����������� ��������� ��� �����/��������.
     * ������������ � ������ ������������� ������. ����� ������������� ���������
     * �������, ������������ � �������� ����������.
     *
     * @param table                 ������� ������������ ������.
     * @param minDiffStorageValues  ������ ������ �����.
     * @param minDiffCustomerValues ������ ������ ��������.
     */
    public static void calculateDiffs(TransportTaskTable table,
                                      int[] minDiffStorageValues,
                                      int[] minDiffCustomerValues) {
        //������� �������� �� �������
        for (int i = 0; i < table.supply.length; ++i) {
            final Tuple<Integer> storageMinValues = findTwoMinValues(table.demand.length,
                    table.cost[i],
                    table.demand);

            if (table.supply[i] != 0) minDiffStorageValues[i] = storageMinValues.y - storageMinValues.x;
            else minDiffStorageValues[i] = -1;
        }

        //������� �������� �� ��������
        for (int j = 0; j < table.demand.length; ++j) {
            final Tuple<Integer> customerMinValues = findTwoMinValuesInCol(table.supply.length,
                    table.cost,
                    j,
                    table.supply);

            if (table.demand[j] != 0) minDiffCustomerValues[j] = customerMinValues.y - customerMinValues.x;
            else minDiffCustomerValues[j] = -1;
        }
    }

    /**
     * ����� ������������ �������� � ������.
     *
     * @param cost   ������� ����������.
     * @param row    ������ ������.
     * @param demand ��������������� ������ ������������.
     * @return ������ ������������ �������� � ������
     */
    public static int getIndexMinRow(int[][] cost, int row, int[] demand) {
        int min = Integer.MAX_VALUE, min_i = Integer.MAX_VALUE;

        for (int i = 0; i < demand.length; ++i) {
            if (cost[row][i] < min && demand[i] != 0) {
                min = cost[row][i];
                min_i = i;
            }
        }

        return min_i;
    }

    /**
     * ����� ������������ �������� � �������.
     *
     * @param cost   ������� ����������.
     * @param col    ������ �������.
     * @param supply ��������������� ������ �������.
     * @return ������ ������������ �������� � �������.
     */
    public static int getIndexMinCol(int[][] cost, int col, int[] supply) {
        int minValue = Integer.MAX_VALUE, res = Integer.MAX_VALUE;

        for (int j = 0; j < supply.length; ++j) {
            if (cost[j][col] < minValue && supply[j] != 0) {
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
        return Arrays.stream(table.supply).allMatch(el -> el == 0) && Arrays.stream(table.demand).allMatch(el -> el == 0);
    }

    /**
     * ��������������� ������� ��� vogelApproximationMethod.
     *
     * @param table    ������� ������������ ������.
     * @param basePlan ������� �������� �����.
     * @param i        ������ ������.
     * @param j        ������ �����������.
     */
    public static void calc(TransportTaskTable table, Shipment[][] basePlan, int i, int j) {
        if (table.supply[i] >= table.demand[j]) {
            basePlan[i][j] = new Shipment(table.cost[i][j], i, j, table.demand[j]);
            table.supply[i] -= table.demand[j];
            table.demand[j] = 0;
        } else {
            basePlan[i][j] = new Shipment(table.cost[i][j], i, j, table.supply[i]);
            table.demand[j] -= table.supply[i];
            table.supply[i] = 0;
        }
    }

    /**
     * ����� ������������� �������� � �������.
     * ����� ����������� ����� ��������, ���������� ��� � ����� �������.
     *
     * @param m ���������� ����� �������.
     * @param n ���������� �������� �������.
     * @param a �������.
     * @return ������ �������� ������������� �������� �������.
     */
    public static Tuple<Integer> findMaxMatrix(final int m, final int n, int[][] a) {
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


    /**
     * ����������� ���������� ��������� �����.
     * ������������ � ������ �������� ������������.
     *
     * @param table ������� �������� �����.
     * @param mask  ����� �����������.
     */
    public static void setColPriorities(TransportTaskTable table, int[][] mask) {
        for (int j = 0; j < table.demand.length; j++) {
            if (table.demand[j] != 0) {
                int min = Integer.MAX_VALUE;

                for (int i = 0; i < table.supply.length; ++i) {
                    if (table.cost[i][j] < min && table.supply[i] != 0) min = table.cost[i][j];
                }

                for (int i = table.supply.length - 1; i >= 0; i--) {
                    if (table.cost[i][j] == min && table.supply[i] != 0) {
                        mask[i][j]++;
                        break;
                    }
                }
            }
        }
    }

    /**
     * ����������� ���������� ��������� ��������.
     * ������������ � ������ �������� ������������.
     *
     * @param table ������� �������� �����.
     * @param mask  ����� �����������.
     */
    public static void setRowPriorities(TransportTaskTable table, int[][] mask) {
        for (int i = 0; i < table.supply.length; i++) {
            if (table.supply[i] != 0) {
                int min = Integer.MAX_VALUE;

                for (int j = 0; j < table.demand.length; j++) {
                    if (table.cost[i][j] < min && table.demand[j] != 0) min = table.cost[i][j];
                }

                for (int j = table.demand.length - 1; j >= 0; j--) {
                    if (table.cost[i][j] == min && table.demand[j] != 0) {
                        mask[i][j]++;
                        break;
                    }
                }
            }
        }
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

    /**
     * ��������� ������ ����������� ��� ��������� ������.
     *
     * @param table     ������� ������������ ������.
     * @param basePlan  ������� �������� �����.
     * @param u         ���������� �������.
     * @param v         ���������� ������������.
     * @param deltaCost ������� ����������� ��������� ������.
     */
    static void calculateDeltaFreeCells(TransportTaskTable table,
                                        Shipment[][] basePlan,
                                        int[] u,
                                        int[] v,
                                        int[][] deltaCost) {
        for (int i = 0; i < table.supply.length; ++i) {
            for (int j = 0; j < table.demand.length; ++j) {
                if (basePlan[i][j] == null) {
                    deltaCost[i][j] = table.cost[i][j] - (u[i] + v[j]);
                }
            }
        }
    }

    /**
     * ��������� ������� ����������� ��� �������� (�������) ������.
     *
     * @param table    ������� ������������ ������.
     * @param basePlan ������� �������� �����.
     * @param u        ���������� �������.
     * @param v        ���������� ������������.
     */
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

        for (int j = 0; j < table.demand.length; j++) {
            if (basePlan[0][j] != null && !vMask[j]) {
                v[j] = table.cost[0][j] - u[0];
                vMask[j] = true;
                t1.push(j);
            }
        }

        do {
            while (!t1.isEmpty()) {
                int j = t1.pop();
                for (int i = 0; i < table.supply.length; i++) {
                    if (basePlan[i][j] != null) {
                        u[i] = table.cost[i][j] - v[j];
                        uMask[i] = true;
                        t2.push(i);
                    }
                }
            }

            while (!t2.isEmpty()) {
                int i = t2.pop();
                for (int j = 0; j < table.demand.length; j++) {
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

    /**
     * ����� �����������.
     * �������� �������� ������� ���� � ������������ �����.
     * @param table    ������� ������������ ������.
     * @param basePlan ������� �������� �����.
     * @return ������, ���� ������ ������� ���� �������� �����������,
     * ����� - ����.
     */
    public static boolean isOptimal(TransportTaskTable table, Shipment[][] basePlan) {
        int[] u = new int[table.supply.length];
        int[] v = new int[table.demand.length];

        for (int i = 0; i < table.supply.length; i++) u[i] = -1;
        for (int i = 0; i < table.demand.length; i++) v[i] = -1;

        calculateDeltaBasicCells(table, basePlan, u, v);

        int[][] deltaCost = new int[table.supply.length][table.demand.length];

        calculateDeltaFreeCells(table, basePlan, u, v, deltaCost);

        return Arrays.stream(deltaCost).noneMatch(sm -> Arrays.stream(sm).anyMatch(val -> val < 0));
    }

    /**
     * ��������� ����� ��������� ���� ��������� ������� �������� �����.
     *
     * @param table    ������� ������������ ������.
     * @param basePlan ������� �������� �����.
     * @return ��������� ��������� ������� �������� �����.
     */
    public static int calculateBasePlan(TransportTaskTable table, Shipment[][] basePlan) {
        int res = 0;

        for (int i = 0; i < table.supply.length; ++i) {
            for (int j = 0; j < table.demand.length; ++j) {
                if (basePlan[i][j] != null) {
                    res += table.cost[i][j] * basePlan[i][j].quantity;
                }
            }
        }

        return res;
    }

    /**
     * �������� ����������/���������� ������������ ������.
     *
     * @param sumOfAllSupplies ����� ���� �������.
     * @param sumOfAllDemands  ����� ���� ������������.
     * @return ���������� 1, ���� ������ ������������ ������ �������� ��������;
     * ���������� 0, ���� ������������ ������ �������� �������� � ������� ��������������� ������;
     * ���������� -1, �����, ���� �������� �������� � ������� ��������������� ���������.
     */
    public static int checkOpenClose(int sumOfAllSupplies, int sumOfAllDemands) {
        int res;

        if (sumOfAllDemands == sumOfAllSupplies) {
            return 1;
        } else {
            if (sumOfAllDemands > sumOfAllSupplies) {
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

    /**
     * �������� �������� ������������ ������ � ��������.
     *
     * @param table            ������� ������������ ������.
     * @param sumOfAllSupplies ����� ���� �������.
     * @param sumOfAllDemands  ����� ���� ������������.
     */
    public static void getBalanced(TransportTaskTable table, int sumOfAllSupplies, int sumOfAllDemands) {
        //���� � ��� ������� ������, ��� ������������
        if (PROBLEM_TYPE == 0) fictionalDemandCase(sumOfAllSupplies - sumOfAllDemands, table);
            //���� � ��� ������������ ������, ��� ���� �� �������
        else if (PROBLEM_TYPE == -1) fictionalSupplyCase(sumOfAllDemands - sumOfAllSupplies, table);
    }

    /**
     * ���������� ���������� �����������.
     *
     * @param newDemand ���������� ������������ ������ �����������.
     * @param table     ������� ������������ ������.
     */
    private static void fictionalDemandCase(int newDemand, TransportTaskTable table) {
        table.setDemand(add(table.demand, newDemand));
        for (int i = 0; i < table.supply.length; i++) table.cost[i] = add(table.cost[i], 0);
    }


    /**
     * ���������� ���������� ������.
     *
     * @param newSupply ���������� ������� ������ ������.
     * @param table     ������� ������������ ������.
     */
    private static void fictionalSupplyCase(int newSupply, TransportTaskTable table) {
        table.setSupply(add(table.supply, newSupply));

        List<int[]> newCost = Arrays.stream(table.cost).collect(Collectors.toList());

        newCost.add(new int[table.demand.length]);

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

    /**
     * ������� � ������� �������� ��������� ������� �������� �����.
     *
     * @param m        ���������� ����� �������.
     * @param n        ���������� �������� �������.
     * @param basePlan ������� �������� �����.
     */
    void printRoutes(final int m, final int n, final Shipment[][] basePlan) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                if (basePlan[i][j] != null) {
                    System.out.println(i + " ����� ---" + basePlan[i][j].quantity + "---> " + j + " �������");
                }
            }
        }
    }
}
