import model.TransportTaskTable;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static int PROBLEM_TYPE;

    //������� ������� a ������� MxN
    static void printMatrix(final int m, final int n, final Integer[][] a) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                System.out.println(a[i][j] + " ");
            }
            System.out.println();
        }
    }

    //���� ������� a ������� n.
    static void inputArray(int[] a, final int n) {
        for (int i = 0; i < n; ++i) a[i] = in.nextInt();
    }

    //���� ������� a ������� MxN.
    static void inputMatrix(final int m, final int n, int[][] a) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                a[i][j] = in.nextInt();
            }
        }
    }

    //��������� ������ ������ ������� a ������� MxN ������ num.
    public static void fillMatrixWith(final int num, final int m, final int n, int[][] a) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                a[i][j] = num;
            }
        }
    }

    /*
    ���������� ������, ���� ������� col � ������� basePlan,
    � ������� ������ ������ ����� storage_amount, ������, ��
    ���� �� �������� � ���� ����������� ������, ����� - ����.
    */
    boolean isEmptyCol(final int storage_amount, final int col, final int[][] basePlan) {
        for (int i = 0; i < storage_amount; ++i)
            if (basePlan[i][col] != -1) return false;
        return true;
    }

    /*
    ���������� ��������� � ���� ���������� ������������� �����.
    ����������, ����� ����� ������ ����� ����������� ���������,
    � ��������� ���� �������� �������� ��� �������������� �������� �����.
    ����������� ����������� �� �������� ��������� �������� �������� � ����� ������ �������.
    */
    void fixDegenerate(final int customers_amount, final int storage_amount, int[][] basePlan, final int i) {
        //���������� �������
        for (int k = 0; k < customers_amount; ++k) {
            //���� ������� k � ������� basePlan ������ (�.�. � �� ��� ����������� ���������)
            if (isEmptyCol(storage_amount, k, basePlan)) {
                basePlan[i][k] = 0;
                break;
            }
        }
    }

    //���������� ����� �������� �����, ��������� � ������� ������ "������-���������" ����.
    static int[][] northWestMethod(TransportTaskTable table) {
        //������� �������� �����
        int[][] basePlan = new int[table.storageAmount][table.customerAmount];
        //���������� ������� �������� ����� "�������" ��������
        fillMatrixWith(-1, table.storageAmount, table.customerAmount, basePlan);

        for (int i = 0; i < table.storageAmount; ++i) {
            for (int j = 0; j < table.customerAmount; ++j) {
                //���� ����������� ��������� i �������������
                if (table.need[j] == 0) continue;

                //���� ������ ������� j ����� ����
                if (table.storage[i] == 0) break;

                //���� ������ ������ i ����� ����������� ��������� j
                if (table.need[j] == table.storage[i]) {
                    basePlan[i][j] = table.storage[i];
                    if (j + 1 < table.customerAmount) {
                        basePlan[i][j + 1] = 0;
                    } else if (i + 1 < table.storageAmount) {
                        basePlan[i + 1][j] = 0;
                    }
                    table.storage[i] = table.need[j] = 0;
                }

                //���� ����������� ��������� j ������, ��� ������ ������ i
                if (table.need[j] > table.storage[i]) {
                    table.need[j] -= table.storage[i];
                    basePlan[i][j] = table.storage[i];
                    table.storage[i] = 0;
                }

                //���� ����������� ��������� i ������ ��� ������ ������ j
                if (table.need[j] < table.storage[i]) {
                    table.storage[i] -= table.need[j];
                    basePlan[i][j] = table.need[j];
                    table.need[j] = 0;
                }

                //���� ��� ����������� ������������� � ��� ������ �����������
                if (Arrays.stream(table.need).allMatch(el -> el == 0) && Arrays.stream(table.storage).allMatch(el -> el == 0))
                    return basePlan;
            }
        }

        return basePlan;
    }

    //����� ������� a ������� MxN, ��� ��� �������������� ����� ������� ����� 1, ����� - 0
    static int[][] mask = new int[50][50];

    /* ���������� ������ �� �������� ���������� ������������ �������� � ������� a ������� MxN. */
    static Tuple<Integer> findMinMatrix(final int m, final int n, final int[][] a) {
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

    /* ���������� ����� �������� �����, ��������� � ������� ������ ����������� ���������. */
    static int[][] minCostMethod(TransportTaskTable table) {
        //������� �������� �����
        int[][] basePlan = new int[table.storageAmount][table.customerAmount];

        //��������� ������� �������� ����� "�������" ��������
        fillMatrixWith(-1, table.storageAmount, table.customerAmount, basePlan);

        while (!Arrays.stream(table.storage).allMatch(el -> el == 0) && !Arrays.stream(table.need).allMatch(el -> el == 0)) {
            final Tuple<Integer> min = findMinMatrix(table.storageAmount, table.customerAmount, table.cost);

            if (table.storage[min.x] != 0 && table.need[min.y] != 0) {
                //���� ����������� ��������� ����� ������ �������
                if (table.storage[min.x] == table.need[min.y]) {
                    basePlan[min.x][min.y] = table.storage[min.x];
                    table.storage[min.x] = table.need[min.y] = 0;
                }
                //�����, ���� ����� ������� ������ ����������� ���������
                else if (table.storage[min.x] > table.need[min.y]) {
                    table.storage[min.x] -= table.need[min.y];
                    basePlan[min.x][min.y] = table.need[min.y];
                    table.need[min.y] = 0;
                }
                //�����, ���� ����������� ��������� ������ ������ ������
                else if (table.need[min.y] > table.storage[min.x]) {
                    table.need[min.y] -= table.storage[min.x];
                    basePlan[min.x][min.y] = table.storage[min.x];
                    table.storage[min.x] = 0;
                }
            }
        }

        return basePlan;
    }

    /*
    ���������� ������ �� ���� ����������� ��������� (x,y) � ������� a ����� n.
    ��� ���� x < y.
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

    /* 
    ���������� ������ �� ���� ����������� ��������� (x,y) � ������� col ������� a.
    ��� ���� x < y. 
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

    public static void calculateDiffs(TransportTaskTable table, int[] minDiffStorageValues, int[] minDiffCustomerValues) {
        //������� �������� �� �������
        for (int i = 0; i < table.storageAmount; ++i) {
            final Tuple<Integer> storageMinValues = findTwoMinValues(table.customerAmount, table.cost[i], table.need);

            if (table.storage[i] != 0) {
                minDiffStorageValues[i] = storageMinValues.y - storageMinValues.x;
            } else {
                minDiffStorageValues[i] = -1;
            }
        }

        //������� �������� �� ��������
        for (int j = 0; j < table.customerAmount; ++j) {
            final Tuple<Integer> customerMinValues = findTwoMinValuesInCol(table.storageAmount, table.cost, j, table.storage);

            if (table.need[j] != 0) {
                minDiffCustomerValues[j] = customerMinValues.y - customerMinValues.x;
            } else {
                minDiffCustomerValues[j] = -1;
            }
        }
    }

    static int getIndexMinRow(int customers_amount, int[][] cost, int row, int[] need) {
        int min = Integer.MAX_VALUE, min_i = Integer.MAX_VALUE;

        for (int i = 0; i < customers_amount; ++i) {
            if (cost[row][i] < min && need[i] != 0) {
                min = cost[row][i];
                min_i = i;
            }
        }

        return min_i;
    }

    static int getIndexMinCol(int storage_amount, int[][] cost, int col) {
        int minValue = Integer.MAX_VALUE, res = Integer.MAX_VALUE;

        for (int j = 0; j < storage_amount; ++j) {
            if (cost[j][col] < minValue) {
                minValue = cost[j][col];
                res = j;
            }
        }

        return res;
    }

    //���������� ������� �������� �����, ��������� � ������� ������ "������������� ������"
    static int[][] vogelApproximationMethod(TransportTaskTable table) {
        //������ ����� ��� �������
        int[] diffStorageValues = new int[table.storageAmount];
        //������ ����� ��� ����������
        int[] diffCustomerValues = new int[table.customerAmount];

        //������� �������� �����
        int[][] basePlan = new int[table.storageAmount][table.customerAmount];

        while (true) {
            calculateDiffs(table, diffStorageValues, diffCustomerValues);

            final int indexMaxStorageDiff = getIndexMax(table.storageAmount, diffStorageValues);
            final int indexMaxNeedDiff = getIndexMax(table.customerAmount, diffCustomerValues);

            if (diffStorageValues[indexMaxStorageDiff] >= diffCustomerValues[indexMaxNeedDiff]) {
                final int indexMinRow = getIndexMinRow(table.customerAmount, table.cost, indexMaxStorageDiff, table.need);
                storageBiggerThanNeedCase(table.need, table.storage, basePlan, indexMaxStorageDiff, indexMinRow);
            } else {
                final int indexMinCol = getIndexMinCol(table.storageAmount, table.cost, indexMaxNeedDiff);
                storageBiggerThanNeedCase(table.need, table.storage, basePlan, indexMinCol, indexMaxNeedDiff);
            }

            if (Arrays.stream(table.storage).allMatch(el -> el == 0) && Arrays.stream(table.need).allMatch(el -> el == 0))
                break;
        }

        return basePlan;
    }

    private static void storageBiggerThanNeedCase(int[] need, int[] storage, int[][] basePlan, int index_of_max_elem_storage_diffs, int min_j) {
        if (storage[index_of_max_elem_storage_diffs] >= need[min_j]) {
            basePlan[index_of_max_elem_storage_diffs][min_j] = need[min_j];
            storage[index_of_max_elem_storage_diffs] -= need[min_j];
            need[min_j] = 0;
        } else {
            basePlan[index_of_max_elem_storage_diffs][min_j] = storage[index_of_max_elem_storage_diffs];
            need[min_j] -= storage[index_of_max_elem_storage_diffs];
            storage[index_of_max_elem_storage_diffs] = 0;
        }
    }

    //���������� �������, �������������� � ���� �������, ������������� �������� � ������� a ������� MxN
    static Tuple<Integer> findMaxMatrixMask(final int m, final int n, int[][] a) {
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

    //���������� ������� ����, ��������� ������� "�������� ������������".
    static int[][] doublePreferenceMethod(TransportTaskTable table) {
        //����� �������
        int[][] mask = new int[table.storageAmount][table.customerAmount];
        //������� �������� �����
        int[][] basePlan = new int[table.storageAmount][table.customerAmount];
        fillMatrixWith(-1, table.storageAmount, table.customerAmount, basePlan);

        while (!Arrays.stream(table.storage).allMatch(el -> el == 0) && !Arrays.stream(table.need).allMatch(el -> el == 0)) {
            //���������� ����� ������
            fillMatrixWith(0, table.storageAmount, table.customerAmount, mask);

            setRowPriorities(table, mask);
            setColPriorities(table, mask);

            Tuple<Integer> indexMaxPriorityCell = findMaxMatrixMask(table.storageAmount, table.customerAmount, mask);

            int currMaxStorage = table.storage[indexMaxPriorityCell.x];
            int currMaxNeed = table.need[indexMaxPriorityCell.y];

            if (currMaxStorage != 0 && currMaxNeed != 0) {
                if (currMaxStorage >= currMaxNeed) {
                    basePlan[indexMaxPriorityCell.x][indexMaxPriorityCell.y] = currMaxNeed;
                    table.storage[indexMaxPriorityCell.x] -= table.need[indexMaxPriorityCell.y];
                    table.need[indexMaxPriorityCell.y] = 0;
                } else {
                    basePlan[indexMaxPriorityCell.x][indexMaxPriorityCell.y] = currMaxStorage;
                    table.need[indexMaxPriorityCell.y] -= table.storage[indexMaxPriorityCell.x];
                    table.storage[indexMaxPriorityCell.x] = 0;
                }
            }
        }

        //���� �������� ���������������� ����������� � �������� ������
        if (!Arrays.stream(table.storage).allMatch(el -> el == 0) && !Arrays.stream(table.need).allMatch(el -> el == 0)) {
            for (int i = 0; i < table.storageAmount; ++i) {
                for (int j = 0; j < table.customerAmount; ++j) {
                    //���� ����������� ��������� �������������
                    if (table.need[j] == 0) continue;

                    //���� ������ ������ ����� ����
                    if (table.storage[i] == 0) break;

                    //���� ����������� ��������� ������ ��� ������ ������
                    if (table.need[j] >= table.storage[i]) {
                        basePlan[i][j] = table.storage[i];
                        table.need[j] -= table.storage[i];
                        table.storage[i] = 0;
                    }

                    //���� ����������� ��������� ������ ��� ������ ������
                    if (table.need[j] < table.storage[i]) {
                        basePlan[i][j] = table.need[j];
                        table.storage[i] -= table.need[j];
                        table.need[j] = 0;
                    }

                    //���� ��� ����������� ������������� � ��� ������ ������� �����������
                    if (Arrays.stream(table.storage).allMatch(el -> el == 0) && Arrays.stream(table.need).allMatch(el -> el == 0))
                        return basePlan;
                }

            }
        }

        return basePlan;
    }

    private static void setColPriorities(TransportTaskTable table, int[][] mask) {
        for (int j = 0; j < table.customerAmount; j++) {
            if (table.need[j] != 0) {
                int min = Integer.MAX_VALUE;

                for (int i = 0; i < table.storageAmount; ++i) {
                    if (table.cost[i][j] < min && table.storage[i] != 0) min = table.cost[i][j];
                }

                for (int i = table.storageAmount - 1; i >= 0; i--) {
                    if (table.cost[i][j] == min) {
                        mask[i][j]++;
                        break;
                    }
                }
            }
        }
    }

    private static void setRowPriorities(TransportTaskTable table, int[][] mask) {
        for (int i = 0; i < table.storageAmount; i++) {
            if (table.storage[i] != 0) {
                int min = Integer.MAX_VALUE;

                for (int j = 0; j < table.customerAmount; j++) {
                    if (table.cost[i][j] < min && table.need[j] != 0) min = table.cost[i][j];
                }

                for (int j = table.customerAmount - 1; j >= 0; j--) {
                    if (table.cost[i][j] == min && table.need[j] != 0) {
                        mask[i][j]++;
                        break;
                    }
                }
            }
        }
    }

    /**
     * ������������� �����: ���� 0, �� ���� �� ��������,
     * ���� 1, �� ���� ��������.
     */
    static boolean isLoopDone = false;

    /** ������ ����� �����, ������������ ������
     * @param storageAmount ���������� �������
     * @param customerAmount - ���������� ������������
     * @param loopMask ����� �����������
     * @param basePlan ������� ���� ������������ ������
     * @param isPlus ���� true, �� � ������ �������� "+", ����� - "-"
     * @param indexFirstNode ��������� ������� ������ ������� ���� �����������
     * @param indexLastNode ��������� ������� ������ ���������� ���� �����������
     */
    static void buildRightLine(final int storageAmount,
                        final int customerAmount,
                        int[][] loopMask,
                        final int[][] basePlan,
                        final boolean isPlus,
                        final Tuple<Integer> indexFirstNode,
                        final Tuple<Integer> indexLastNode) {
        for (int j = indexLastNode.y + 1; j < storageAmount; ++j) {
            if (isLoopDone || (Objects.equals(indexLastNode.x, indexFirstNode.x) && j == indexFirstNode.y)) {
                isLoopDone = true;
                break;
            }
            if (!isPlus && loopMask[indexLastNode.x][j] == 1 && basePlan[indexLastNode.x][j] != -1) {
                loopMask[indexLastNode.x][j] = -2;
                Tuple<Integer> lastNode = new Tuple<>(indexLastNode.x, j);
                buildLoopLine(storageAmount,
                        customerAmount,
                        loopMask,
                        basePlan,
                        true,
                        1,
                        3,
                        indexFirstNode,
                        lastNode);
                if (!isLoopDone) loopMask[indexLastNode.x][j] = 1;
            } else if (isPlus && loopMask[indexLastNode.x][j] == 1) {
                loopMask[indexLastNode.x][j] = 2;
                Tuple<Integer> lastNode = new Tuple<>(indexLastNode.x, j);
                buildLoopLine(storageAmount,
                        customerAmount,
                        loopMask,
                        basePlan,
                        false,
                        1,
                        3,
                        indexFirstNode,
                        lastNode);
                if (!isLoopDone) loopMask[indexLastNode.x][j] = 1;
            }
        }
    }

    /**������ ����� �����, ������������ �����
     * @param storageAmount ���������� �������
     * @param customerAmount - ���������� ������������
     * @param loopMask ����� �����������
     * @param basePlan ������� ���� ������������ ������
     * @param isPlus ���� true, �� � ������ �������� "+", ����� - "-"
     * @param indexFirstNode ��������� ������� ������ ������� ���� �����������
     * @param indexLastNode ��������� ������� ������ ���������� ���� �����������
     */
    static void buildUpLine(final int storageAmount,
                            final int customerAmount,
                            int[][] loopMask,
                            final int[][] basePlan,
                            final boolean isPlus,
                            final Tuple<Integer> indexFirstNode,
                            final Tuple<Integer> indexLastNode) {
        for (int i = indexLastNode.x - 1; i >= 0; i--) {
            if (isLoopDone || (i == indexFirstNode.x && Objects.equals(indexLastNode.y, indexFirstNode.y))) {
                isLoopDone = true;
                break;
            }
            if (!isPlus && loopMask[i][indexLastNode.y] == 1 && basePlan[i][indexLastNode.y] != -1) {
                loopMask[i][indexLastNode.y] = -2;
                Tuple<Integer> lastNode = new Tuple<>(i, indexLastNode.y);
                buildLoopLine(storageAmount,
                        customerAmount,
                        loopMask,
                        basePlan,
                        true,
                        2,
                        4,
                        indexFirstNode,
                        lastNode);
                if (!isLoopDone) loopMask[i][indexLastNode.y] = 1;
            } else if (isPlus && loopMask[i][indexLastNode.y] == 1) {
                loopMask[i][indexLastNode.y] = 2;
                Tuple<Integer> lastNode = new Tuple<>(i, indexLastNode.y);
                buildLoopLine(storageAmount,
                        customerAmount,
                        loopMask,
                        basePlan,
                        false,
                        2,
                        4,
                        indexFirstNode,
                        lastNode);
                if (!isLoopDone) loopMask[i][indexLastNode.y] = 1;
            }
        }
    }

    /**������ ����� �����, ������������ �����
     * @param storageAmount ���������� �������
     * @param customerAmount - ���������� ������������
     * @param loopMask ����� �����������
     * @param basePlan ������� ���� ������������ ������
     * @param isPlus ���� true, �� � ������ �������� "+", ����� - "-"
     * @param indexFirstNode ��������� ������� ������ ������� ���� �����������
     * @param indexLastNode ��������� ������� ������ ���������� ���� �����������
     */
    static void buildLeftLine(final int storageAmount,
                              final int customerAmount,
                              int[][] loopMask,
                              final int[][] basePlan,
                              final boolean isPlus,
                              final Tuple<Integer> indexFirstNode,
                              final Tuple<Integer> indexLastNode) {
        for (int j = indexLastNode.y - 1; j >= 0; j--) {
            if (isLoopDone || (Objects.equals(indexLastNode.x, indexFirstNode.x) && j == indexFirstNode.y)) {
                isLoopDone = true;
                break;
            }
            if (!isPlus && loopMask[indexLastNode.x][j] == 1 && basePlan[indexLastNode.x][j] != -1) {
                loopMask[indexLastNode.x][j] = -2;
                Tuple<Integer> lastNode = new Tuple<>(indexLastNode.x, j);
                buildLoopLine(storageAmount,
                        customerAmount,
                        loopMask,
                        basePlan,
                        true,
                        3,
                        1,
                        indexFirstNode,
                        lastNode);
                if (!isLoopDone) loopMask[indexLastNode.x][j] = 1;
            } else if (isPlus && loopMask[indexLastNode.x][j] == 1) {
                loopMask[indexLastNode.x][j] = 2;
                Tuple<Integer> lastNode = new Tuple<>(indexLastNode.x, j);
                buildLoopLine(storageAmount,
                        customerAmount,
                        loopMask,
                        basePlan,
                        false,
                        3,
                        1,
                        indexFirstNode,
                        lastNode);
                if (!isLoopDone) loopMask[indexLastNode.x][j] = 1;
            }
        }
    }

    /**������ ����� �����, ������������ ����
     * @param storageAmount ���������� �������
     * @param customerAmount - ���������� ������������
     * @param loopMask ����� �����������
     * @param basePlan ������� ���� ������������ ������
     * @param isPlus ���� true, �� � ������ �������� "+", ����� - "-"
     * @param indexFirstNode ��������� ������� ������ ������� ���� �����������
     * @param indexLastNode ��������� ������� ������ ���������� ���� �����������
     */
    static void buildDownLine(final int storageAmount,
                              final int customerAmount,
                              int[][] loopMask,
                              final int[][] basePlan,
                              final boolean isPlus,
                              final Tuple<Integer> indexFirstNode,
                              final Tuple<Integer> indexLastNode) {
        for (int i = indexLastNode.x + 1; i < storageAmount; ++i) {
            if (isLoopDone || i == indexFirstNode.x && Objects.equals(indexFirstNode.y, indexLastNode.y)) {
                isLoopDone = true;
                break;
            }
            if (!isPlus && loopMask[i][indexLastNode.y] == 1 && basePlan[i][indexLastNode.y] != -1) {
                loopMask[i][indexLastNode.y] = -2;
                Tuple<Integer> lastNode = new Tuple<>(i, indexLastNode.y);
                buildLoopLine(storageAmount,
                        customerAmount,
                        loopMask,
                        basePlan,
                        true,
                        4,
                        2,
                        indexFirstNode,
                        lastNode);
                if (!isLoopDone) loopMask[i][indexLastNode.y] = 1;
            } else if (isPlus && loopMask[i][indexLastNode.y] == 1) {
                loopMask[i][indexLastNode.y] = 2;
                Tuple<Integer> lastNode = new Tuple<>(i, indexLastNode.y);
                buildLoopLine(storageAmount,
                        customerAmount,
                        loopMask,
                        basePlan,
                        false,
                        4,
                        2,
                        indexFirstNode,
                        lastNode);
                if (!isLoopDone) loopMask[i][indexLastNode.y] = 1;
            }
        }
    }

    /*
    ������ ���� ����������� �������� ����� basePlan ������� storageAmount �� customersAmount,
    �������� �� loopMask ������� storageAmount �� customersAmount;
    Type - ���� true, �� � ������ �������� "+" (+2), ���� false, �� �������� "-" (-2);
    None_dir - �������� �����������, �� �������� ������ ��������� �� ������ ������ ���������;
    Reverse_none_dir - �������� �����������, �� �������� ������ ��������� �� ������ ������ ���������;
    First_node_indexes - ������ �������� (i,j) ����� ������ "+"-������ �������;
    Last_node_indexes - ������ �������� (i,j) ����� ��������� ������ �������
    */
    static void buildLoopLine(final int storageAmount,
                              final int customersAmount,
                              int[][] loopMask,
                              final int[][] basePlan,
                              final boolean isPlus,
                              final int noneDir,
                              final int reverseNoneDir,
                              final Tuple<Integer> indexFirstNode,
                              final Tuple<Integer> indexLastNode) {
        //���� ������ ����� �� ��������
        if (!isLoopDone) {
            //���� ����� ��������� ������
            if (noneDir != Dir.RIGHT && reverseNoneDir != Dir.RIGHT) {
                //������ ����� ����� � ������ �������
                buildRightLine(storageAmount, customersAmount, loopMask, basePlan, isPlus, indexFirstNode, indexLastNode);
            }
        }

        //���� ������ ����� �� ��������
        if (!isLoopDone) {
            //���� ����� ��������� �����
            if (noneDir != Dir.UP && reverseNoneDir != Dir.UP) {
                //������ ����� ����� �����
                buildUpLine(storageAmount, customersAmount, loopMask, basePlan, isPlus, indexFirstNode, indexLastNode);
            }
        }

        //���� ������ ����� �� ��������
        if (!isLoopDone) {
            //���� ����� ��������� �����
            if (noneDir != Dir.LEFT && reverseNoneDir != Dir.LEFT) {
                //������ ����� ����� � ����� �������
                buildLeftLine(storageAmount, customersAmount, loopMask, basePlan, isPlus, indexFirstNode, indexLastNode);
            }
        }

        //���� ������ ����� �� ��������
        if (!isLoopDone) {
            //���� ����� ��������� ����
            if (noneDir != Dir.DOWN && reverseNoneDir != Dir.DOWN) {
                //������ ����� ����� ����
                buildDownLine(storageAmount, customersAmount, loopMask, basePlan, isPlus, indexFirstNode, indexLastNode);
            }
        }
    }

    //���������� ���������� �������� ����� basePlan.
    static void performReallocation(final int storageAmount,
                                    final int customersAmount,
                                    int[][] basePlan,
                                    final Tuple<Integer> indexMaxAbsDelta) {
        int[][] loopMask = new int[storageAmount][customersAmount];
        fillMatrixWith(1, storageAmount, customersAmount, loopMask);

        for (int i = 0; i < storageAmount; ++i) {
            int filledCells = (int) Arrays.stream(basePlan[i]).filter(value -> value != -1).count();

            if (filledCells <= 1 && i != indexMaxAbsDelta.x) {
                for (int k = 0; k < storageAmount; ++k) {
                    loopMask[i][k] = 0;
                }
            }
        }

        for (int j = 0; j < customersAmount; ++j) {
            int finalJ = j;
            int filledCells = (int) Arrays.stream(basePlan).filter(ints -> ints[finalJ] != -1).count();

            if (filledCells <= 1 && j != indexMaxAbsDelta.y) {
                for (int k = 0; k < storageAmount; ++k) {
                    loopMask[k][j] = 0;
                }
            }
        }

        loopMask[indexMaxAbsDelta.x][indexMaxAbsDelta.y] = 2;

        buildLoopLine(storageAmount,
                customersAmount,
                loopMask,
                basePlan,
                false,
                0,
                0,
                indexMaxAbsDelta,
                indexMaxAbsDelta);

        isLoopDone = false;

        recalculateBasePlan(storageAmount, customersAmount, basePlan, loopMask);
    }

    /*
    ���������� ���������� �������� ����� basePlan ������� storageAmount �� customersAmount,
    �������� �� ����� ����� loopMask
    */
    static void recalculateBasePlan(final int storageAmount,
                                    final int customersAmount,
                                    int[][] basePlan,
                                    final int[][] loopMask) {
        //����������� ��������� �� ������ "-" � loopMask
        final int min = findMinNegativeNode(storageAmount, customersAmount, basePlan, loopMask);
        int zeros = 0;

        for (int i = 0; i < storageAmount; ++i) {
            for (int j = 0; j < customersAmount; ++j) {
                //���� � ���� � ��������� (i,j) ����� "+"
                if (loopMask[i][j] == 2) {
                    if (basePlan[i][j] == 0) {
                        zeros++;
                        basePlan[i][j] += min;
                    } else if (basePlan[i][j] == -1) {
                        basePlan[i][j] = 0;
                        basePlan[i][j] += min;
                    }
                }
                //�����, ���� � ���� � ��������� (i,j) ����� "-"
                else if (loopMask[i][j] == -2) {
                    if (zeros > 0) {
                        basePlan[i][j] -= min;
                    } else {
                        basePlan[i][j] -= min;
                        if (basePlan[i][j] == 0) basePlan[i][j] = -1;
                    }
                }
            }
        }
    }

    //���������� ����������� ��������� �� ������ "-" � loopMask
    static int findMinNegativeNode(final int storage_amount,
                                   final int customers_amount,
                                   final int[][] basePlan,
                                   final int[][] loopMask) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < storage_amount; ++i) {
            for (int j = 0; j < customers_amount; ++j) {
                if (loopMask[i][j] == -2) {
                    if (basePlan[i][j] < min) {
                        min = basePlan[i][j];
                    }
                }
            }
        }
        return min;
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

    //��������� ������ ����������� ��� ��������� ������
    static void calculateDeltaFreeCells(int storageAmount,
                                        int customersAmount,
                                        int[][] basePlan,
                                        int[][] cost,
                                        int[] u,
                                        int[] v,
                                        int[][] deltaCost) {
        for (int i = 0; i < storageAmount; ++i) {
            for (int j = 0; j < customersAmount; ++j) {
                if (basePlan[i][j] == -1) {
                    deltaCost[i][j] = cost[i][j] - (u[i] + v[j]);
                }
            }
        }
    }

    //��������� ������� ����������� ��� �������� (�������) ������.
    public static void calculateDeltaBasicCells(int storageAmount,
                                                int customersAmount,
                                                int[][] basePlan,
                                                int[][] cost,
                                                int[] u,
                                                int[] v) {
        boolean[] uMask = new boolean[u.length];
        boolean[] vMask = new boolean[v.length];
        uMask[0] = true;

        LinkedList<Integer> t1 = new LinkedList<>();
        LinkedList<Integer> t2 = new LinkedList<>();

        for (int j = 0; j < customersAmount; j++) {
            if (basePlan[0][j] != -1 && !vMask[j]) {
                v[j] = cost[0][j] - u[0];
                vMask[j] = true;
                t1.push(j);
            }
        }

        do {
            while (!t1.isEmpty()) {
                int j = t1.pop();
                for (int i = 0; i < storageAmount; i++) {
                    if (basePlan[i][j] != -1) {
                        u[i] = cost[i][j] - v[j];
                        uMask[i] = true;
                        t2.push(i);
                    }
                }
            }

            while (!t2.isEmpty()) {
                int i = t2.pop();
                for (int j = 0; j < customersAmount; j++) {
                    if (basePlan[i][j] != -1) {
                        v[j] = cost[i][j] - u[i];
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
    static void potentialMethod(int[][] basePlan, final int storageAmount, final int customersAmount, final int[][] cost) {
        int[] u = new int[storageAmount];
        int[] v = new int[customersAmount];

        while (true) {
            for (int i = 0; i < storageAmount; i++) {
                u[i] = -1;
            }

            for (int i = 0; i < customersAmount; i++) {
                v[i] = -1;
            }

            u[0] = 0;

            calculateDeltaBasicCells(storageAmount, customersAmount, basePlan, cost, u, v);

            int[][] deltaCost = new int[storageAmount][customersAmount];

            calculateDeltaFreeCells(storageAmount, customersAmount, basePlan, cost, u, v, deltaCost);

            Tuple<Integer> indexMaxAbsDelta = isOptimalBasePlan(storageAmount, customersAmount, deltaCost);

            if (indexMaxAbsDelta.x != null && indexMaxAbsDelta.y != null) {
                performReallocation(storageAmount, customersAmount, basePlan, indexMaxAbsDelta);
            } else break;
        }
    }

    //���������� ������, ���� ���� �������� �����������, ����� - ����.
    boolean isDegeneratePlan(int storageAmount, int customersAmount, int[][] basePlan) {
        int need_base_plan_cells = storageAmount + customersAmount - 1, base_plan_cells = 0;

        for (int i = 0; i < storageAmount; ++i) {
            for (int j = 0; j < customersAmount; ++j) {
                if (basePlan[i][j] != -1) base_plan_cells++;
            }
        }

        return base_plan_cells == need_base_plan_cells;
    }

    void makeDegeneratePlan(int storageAmount, int customersAmount, int[][] basePlan) {
        for (int i = 0; i < storageAmount; ++i) {
            for (int j = 0; j < customersAmount; ++j) {
            }
        }
    }

    /*
     ���������� ����� �������� ����� ������� �������� �����
     basePlan ������� MxN � �������������� ������� ���������� cost ������� MxN.
     */
    static int calculateBasePlan(int m, int n, int[][] cost, int[][] basePlan) {
        int res = 0;

        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                if (basePlan[i][j] != -1) {
                    res += cost[i][j] * basePlan[i][j];
                }
            }
        }

        return res;
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

    /*
    ���������� 1, ���� ������ ������������ ������ �������� ��������;
    ���������� 0, ���� ������������ ������ �������� �������� � ������� ��������������� ������;
    ���������� -1, �����, ���� �������� �������� � ������� ��������������� ���������.
    */
    static int checkOpenClose(int sumOfAllStorage, int sumOfAllNeeds) {
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
    static void toCloseProblem(TransportTaskTable table, int sumOfAllStorage, int sumOfAllNeeds) {
        switch (PROBLEM_TYPE) {
            //���� � ��� ������� ������, ��� ������������
            case 0 -> fictionalCustomerCase(sumOfAllStorage - sumOfAllNeeds, table);

            //���� � ��� ������������ ������, ��� ���� �� �������
            case -1 -> fictionalStorageCase(sumOfAllNeeds - sumOfAllStorage, table);

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

        for (int i = 0; i < table.storageAmount; i++) {
            table.cost[i] = add(table.cost[i], 0);
        }

        table.customerAmount++;
    }

    private static void fictionalStorageCase(int delta, TransportTaskTable table) {
        table.setStorage(add(table.storage, delta));

        List<int[]> newCost = Arrays.stream(table.cost).collect(Collectors.toList());

        newCost.add(new int[table.customerAmount]);

        table.setCost(newCost.toArray(new int[0][0]));

        table.storageAmount++;
    }

    public static void main(String[] args) {
        System.out.println("������� ������������ ������");

        System.out.println("������� ���������� �����������");
        int storageAmount = in.nextInt();

        System.out.println("������� �������� �������");
        int[] storage = new int[storageAmount];
        inputArray(storage, storageAmount);

        System.out.println("������� ���������� ����������");
        int customersAmount = in.nextInt();

        System.out.println("������� ����������� ����������");
        int[] need = new int[customersAmount];
        inputArray(need, customersAmount);

        System.out.println("������� ������� ����������");
        int[][] cost = new int[storageAmount][customersAmount];
        inputMatrix(storageAmount, customersAmount, cost);

        TransportTaskTable table = new TransportTaskTable(
                storage, storageAmount,
                need, customersAmount,
                cost
        );

        final int sumAllStorages = Arrays.stream(storage).sum();
        final int sumAllNeeds = Arrays.stream(need).sum();

        PROBLEM_TYPE = checkOpenClose(sumAllStorages, sumAllNeeds);

        //���� ������ ��������� ����
        if (PROBLEM_TYPE != 1) {
            //�������� ������ � ��������� ����
            toCloseProblem(table, sumAllStorages, sumAllNeeds);
        }


        System.out.println("""
                �������� ����� �������
                1.����� ������-��������� ����
                2.����� ������������ ��������
                3.����� ������������� ������
                4.����� �������� ������������""");

        int[][] basePlan;

        switch (in.nextByte()) {
            case 1 -> basePlan = northWestMethod(table);
            case 2 -> basePlan = minCostMethod(table);
            case 3 -> basePlan = vogelApproximationMethod(table);
            case 4 -> basePlan = doublePreferenceMethod(table);
            default -> throw new IllegalStateException("Unexpected value");
        }

        //�������� ������� �������� �����
        System.out.println(calculateBasePlan(storageAmount, customersAmount, cost, basePlan));

        //��������� ���� �� �������������
        potentialMethod(basePlan, table.storageAmount, table.customerAmount, cost);

        System.out.println(calculateBasePlan(storageAmount, customersAmount, cost, basePlan));
    }
}
