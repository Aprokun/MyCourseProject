import model.TransportTaskTable;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static int PROBLEM_TYPE;

    //Выводит матрицу a размера MxN
    static void printMatrix(final int m, final int n, final Integer[][] a) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                System.out.println(a[i][j] + " ");
            }
            System.out.println();
        }
    }

    //Ввод массива a размера n.
    static void inputArray(int[] a, final int n) {
        for (int i = 0; i < n; ++i) a[i] = in.nextInt();
    }

    //Ввод матрицы a размера MxN.
    static void inputMatrix(final int m, final int n, int[][] a) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                a[i][j] = in.nextInt();
            }
        }
    }

    //Заполняет каждую ячейку матрицы a размера MxN числом num.
    public static void fillMatrixWith(final int num, final int m, final int n, int[][] a) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                a[i][j] = num;
            }
        }
    }

    /*
    Возвращает истину, если столбце col в матрице basePlan,
    в которой размер строки равен storage_amount, пустой, то
    есть не содержит в себе заполненных клеток, иначе - ложь.
    */
    boolean isEmptyCol(final int storage_amount, final int col, final int[][] basePlan) {
        for (int i = 0; i < storage_amount; ++i)
            if (basePlan[i][col] != -1) return false;
        return true;
    }

    /*
    Исправляет возникшую в ходе выполнения вырожденность плана.
    Происходит, когда запас склада равен потребности заказчика,
    в следствии чего теряются значения для невырожденного опорного плана.
    Исправление выполняется по принципу помещения нулевого значения в ЛЮБОЙ пустой столбец.
    */
    void fixDegenerate(final int customers_amount, final int storage_amount, int[][] basePlan, final int i) {
        //Перебираем столбцы
        for (int k = 0; k < customers_amount; ++k) {
            //Если столбец k в матрице basePlan пустой (т.е. в нём нет заполненных элементов)
            if (isEmptyCol(storage_amount, k, basePlan)) {
                basePlan[i][k] = 0;
                break;
            }
        }
    }

    //Возвращает сумму опорного плана, найденную с помощью метода "северо-западного" угла.
    static int[][] northWestMethod(TransportTaskTable table) {
        //Матрица опорного плана
        int[][] basePlan = new int[table.storageAmount][table.customerAmount];
        //Заполнение матрицы опорного плана "пустыми" клетками
        fillMatrixWith(-1, table.storageAmount, table.customerAmount, basePlan);

        for (int i = 0; i < table.storageAmount; ++i) {
            for (int j = 0; j < table.customerAmount; ++j) {
                //Если потребность заказчика i удовлетворена
                if (table.need[j] == 0) continue;

                //Если запасы скалада j равны нулю
                if (table.storage[i] == 0) break;

                //Если запасы склада i равны потребности заказчика j
                if (table.need[j] == table.storage[i]) {
                    basePlan[i][j] = table.storage[i];
                    if (j + 1 < table.customerAmount) {
                        basePlan[i][j + 1] = 0;
                    } else if (i + 1 < table.storageAmount) {
                        basePlan[i + 1][j] = 0;
                    }
                    table.storage[i] = table.need[j] = 0;
                }

                //Если потребность заказчика j больше, чем запасы склада i
                if (table.need[j] > table.storage[i]) {
                    table.need[j] -= table.storage[i];
                    basePlan[i][j] = table.storage[i];
                    table.storage[i] = 0;
                }

                //Если потребность заказчика i меньше чем запасы склада j
                if (table.need[j] < table.storage[i]) {
                    table.storage[i] -= table.need[j];
                    basePlan[i][j] = table.need[j];
                    table.need[j] = 0;
                }

                //Если все потребности удовлетворены и все запасы закончились
                if (Arrays.stream(table.need).allMatch(el -> el == 0) && Arrays.stream(table.storage).allMatch(el -> el == 0))
                    return basePlan;
            }
        }

        return basePlan;
    }

    //Маска матрицы a размера MxN, где уже использованный ранее элемент равен 1, иначе - 0
    static int[][] mask = new int[50][50];

    /* Возвращает кортеж из индексов свободного минимального элемента в матрице a размера MxN. */
    static Tuple<Integer> findMinMatrix(final int m, final int n, final int[][] a) {
        int min = Integer.MAX_VALUE;

        //Инициализация кортежа
        Tuple<Integer> min_index = new Tuple<>(-1, -1);

        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                //Если значение не было использовано ранее, меньше минимума и не равно нулю
                if (mask[i][j] != 1 && a[i][j] < min && a[i][j] != 0) {
                    min = a[i][j];
                    min_index.x = i;
                    min_index.y = j;
                }
            }
        }

        //Если задача открытая (то есть в ней есть нулевые стоимости)
        //и кортеж не изменился во время другого цикла
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

        //Отмечаем найденный элемент в маске как использованный
        mask[min_index.x][min_index.y] = 1;

        return min_index;
    }

    /* Возвращает сумму опорного плана, найденную с помощью метода минимальной стоимости. */
    static int[][] minCostMethod(TransportTaskTable table) {
        //Матрица опорного плана
        int[][] basePlan = new int[table.storageAmount][table.customerAmount];

        //Заполняем матрицу опорного плана "пустыми" клетками
        fillMatrixWith(-1, table.storageAmount, table.customerAmount, basePlan);

        while (!Arrays.stream(table.storage).allMatch(el -> el == 0) && !Arrays.stream(table.need).allMatch(el -> el == 0)) {
            final Tuple<Integer> min = findMinMatrix(table.storageAmount, table.customerAmount, table.cost);

            if (table.storage[min.x] != 0 && table.need[min.y] != 0) {
                //Если потребность заказчика равна запасу скалада
                if (table.storage[min.x] == table.need[min.y]) {
                    basePlan[min.x][min.y] = table.storage[min.x];
                    table.storage[min.x] = table.need[min.y] = 0;
                }
                //Иначе, если запас скалада больше потребности заказчика
                else if (table.storage[min.x] > table.need[min.y]) {
                    table.storage[min.x] -= table.need[min.y];
                    basePlan[min.x][min.y] = table.need[min.y];
                    table.need[min.y] = 0;
                }
                //Иначе, если потребность заказчика больше запаса склада
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
    Возвращает кортеж из двух минимальных элементов (x,y) в массиве a длины n.
    При этом x < y.
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
    Возвращает кортеж из двух минимальных элементов (x,y) в столбце col матрицы a.
    При этом x < y. 
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

    /* Возвращает индекс максимального элемента массива a размера n */
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
        //Подсчёт разности по строкам
        for (int i = 0; i < table.storageAmount; ++i) {
            final Tuple<Integer> storageMinValues = findTwoMinValues(table.customerAmount, table.cost[i], table.need);

            if (table.storage[i] != 0) {
                minDiffStorageValues[i] = storageMinValues.y - storageMinValues.x;
            } else {
                minDiffStorageValues[i] = -1;
            }
        }

        //Подсчёт разности по столбцам
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

    //Возвращает матрицу опорного плана, найденную с помощью метода "аппроксимации Фогеля"
    static int[][] vogelApproximationMethod(TransportTaskTable table) {
        //Массив дельт для складов
        int[] diffStorageValues = new int[table.storageAmount];
        //Массив дельт для заказчиков
        int[] diffCustomerValues = new int[table.customerAmount];

        //Матрица опорного плана
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

    //возвращает индексы, представленные в виде кортежа, максимального элемента в матрицы a размера MxN
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

    //Возвращает опорный план, найденный методом "двойного предпочтения".
    static int[][] doublePreferenceMethod(TransportTaskTable table) {
        //Маска матрицы
        int[][] mask = new int[table.storageAmount][table.customerAmount];
        //Матрица опорного плана
        int[][] basePlan = new int[table.storageAmount][table.customerAmount];
        fillMatrixWith(-1, table.storageAmount, table.customerAmount, basePlan);

        while (!Arrays.stream(table.storage).allMatch(el -> el == 0) && !Arrays.stream(table.need).allMatch(el -> el == 0)) {
            //Заполнение маски нулями
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

        //Если остались неудовлетворённые потребности и непустые склады
        if (!Arrays.stream(table.storage).allMatch(el -> el == 0) && !Arrays.stream(table.need).allMatch(el -> el == 0)) {
            for (int i = 0; i < table.storageAmount; ++i) {
                for (int j = 0; j < table.customerAmount; ++j) {
                    //Если потребность заказчика удовлетворена
                    if (table.need[j] == 0) continue;

                    //Если запасы склада равны нулю
                    if (table.storage[i] == 0) break;

                    //Если потребность заказчика больше чем запасы склада
                    if (table.need[j] >= table.storage[i]) {
                        basePlan[i][j] = table.storage[i];
                        table.need[j] -= table.storage[i];
                        table.storage[i] = 0;
                    }

                    //Если потребность заказчика меньше чем запасы склада
                    if (table.need[j] < table.storage[i]) {
                        basePlan[i][j] = table.need[j];
                        table.storage[i] -= table.need[j];
                        table.need[j] = 0;
                    }

                    //Если все потребности удовлетворены и все запасы складов закончились
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
     * Завершённость цикла: если 0, то цикл не завершён,
     * если 1, то цикл завершён.
     */
    static boolean isLoopDone = false;

    /** Строит линию цикла, направленную вправо
     * @param storageAmount количество складов
     * @param customerAmount - количество потребителей
     * @param loopMask маска перерасчёта
     * @param basePlan опорный план транспортной задачи
     * @param isPlus если true, то в клетку ставится "+", иначе - "-"
     * @param indexFirstNode матричные индексы самого первого узла перерасчёта
     * @param indexLastNode матричные индексы самого последнего узла перерасчёта
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

    /**Строит линию цикла, направленную вверх
     * @param storageAmount количество складов
     * @param customerAmount - количество потребителей
     * @param loopMask маска перерасчёта
     * @param basePlan опорный план транспортной задачи
     * @param isPlus если true, то в клетку ставится "+", иначе - "-"
     * @param indexFirstNode матричные индексы самого первого узла перерасчёта
     * @param indexLastNode матричные индексы самого последнего узла перерасчёта
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

    /**Строит линию цикла, направленную влево
     * @param storageAmount количество складов
     * @param customerAmount - количество потребителей
     * @param loopMask маска перерасчёта
     * @param basePlan опорный план транспортной задачи
     * @param isPlus если true, то в клетку ставится "+", иначе - "-"
     * @param indexFirstNode матричные индексы самого первого узла перерасчёта
     * @param indexLastNode матричные индексы самого последнего узла перерасчёта
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

    /**Строит линию цикла, направленную вниз
     * @param storageAmount количество складов
     * @param customerAmount - количество потребителей
     * @param loopMask маска перерасчёта
     * @param basePlan опорный план транспортной задачи
     * @param isPlus если true, то в клетку ставится "+", иначе - "-"
     * @param indexFirstNode матричные индексы самого первого узла перерасчёта
     * @param indexLastNode матричные индексы самого последнего узла перерасчёта
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
    Строит цикл перерасчёта опорного плана basePlan размера storageAmount на customersAmount,
    опираясь на loopMask размера storageAmount на customersAmount;
    Type - если true, то в клетку ставится "+" (+2), если false, то ставится "-" (-2);
    None_dir - основное направление, по которому нельзя двигаться на данный момент процедуры;
    Reverse_none_dir - обратное направление, по которому нельзя двигаться на данный момент процедуры;
    First_node_indexes - кортеж индексов (i,j) самой первой "+"-клетки расчёта;
    Last_node_indexes - кортеж индексов (i,j) самой последней клетки расчёта
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
        //Если расчёт цикла не закончен
        if (!isLoopDone) {
            //Если можно двигаться вправо
            if (noneDir != Dir.RIGHT && reverseNoneDir != Dir.RIGHT) {
                //Строим линию цикла в правую сторону
                buildRightLine(storageAmount, customersAmount, loopMask, basePlan, isPlus, indexFirstNode, indexLastNode);
            }
        }

        //Если расчёт цикла не закончен
        if (!isLoopDone) {
            //Если можно двигаться вверх
            if (noneDir != Dir.UP && reverseNoneDir != Dir.UP) {
                //Строим линию цикла вверх
                buildUpLine(storageAmount, customersAmount, loopMask, basePlan, isPlus, indexFirstNode, indexLastNode);
            }
        }

        //Если расчёт цикла не закончен
        if (!isLoopDone) {
            //Если можно двигаться влево
            if (noneDir != Dir.LEFT && reverseNoneDir != Dir.LEFT) {
                //Строим линию цикла в левую сторону
                buildLeftLine(storageAmount, customersAmount, loopMask, basePlan, isPlus, indexFirstNode, indexLastNode);
            }
        }

        //Если расчёт цикла не закончен
        if (!isLoopDone) {
            //Если можно двигаться вниз
            if (noneDir != Dir.DOWN && reverseNoneDir != Dir.DOWN) {
                //Строим линию цикла вниз
                buildDownLine(storageAmount, customersAmount, loopMask, basePlan, isPlus, indexFirstNode, indexLastNode);
            }
        }
    }

    //Производит перерасчёт опорного плана basePlan.
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
    Производит перерасчёт опорного плана basePlan размера storageAmount на customersAmount,
    опираясь на маску цикла loopMask
    */
    static void recalculateBasePlan(final int storageAmount,
                                    final int customersAmount,
                                    int[][] basePlan,
                                    final int[][] loopMask) {
        //Минимальная перевозка со знаком "-" в loopMask
        final int min = findMinNegativeNode(storageAmount, customersAmount, basePlan, loopMask);
        int zeros = 0;

        for (int i = 0; i < storageAmount; ++i) {
            for (int j = 0; j < customersAmount; ++j) {
                //Если в узле с индексами (i,j) стоит "+"
                if (loopMask[i][j] == 2) {
                    if (basePlan[i][j] == 0) {
                        zeros++;
                        basePlan[i][j] += min;
                    } else if (basePlan[i][j] == -1) {
                        basePlan[i][j] = 0;
                        basePlan[i][j] += min;
                    }
                }
                //Иначе, если в узле с индексами (i,j) стоит "-"
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

    //Возвращает минимальную перевозку со знаком "-" в loopMask
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

    /* Возвращает кортеж со значениями (-1,-1), если план оптимальный, иначе - возвращает кортеж (null,null). */
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

    //Выполняет расчёт потенциалов для свободных клеток
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

    //Выполняет подсчёт потенциалов для базисных (занятых) клеток.
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
    Метод потенциалов.
    Приводит заданный опорный план basePlan к оптимальному плану.
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

    //Возвращает истину, если план является вырожденным, иначе - ложь.
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
     Возвращает сумму опорного плана матрицы опорного плана
     basePlan размера MxN с использованием матрицы стоимостей cost размера MxN.
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

    //Выводит в консоль маршруты перевозок матрицы опорного плана basePlan размера MxN
    void printRoutes(final int m, final int n, final int[][] basePlan) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                if (basePlan[i][j] != 0) {
                    System.out.println(i + " склад ---" + basePlan[i][j] + "---> " + j + " магазин");
                }
            }
        }
    }

    /*
    Возвращает 1, если данная транспортная задача является закрытой;
    Возвращает 0, если транспортная задача является открытой и требует дополнительного склада;
    Возвращает -1, иначе, если является открытой и требует дополнительного заказчика.
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

    /* Приводит открытую транспортную задачу к закрытой */
    static void toCloseProblem(TransportTaskTable table, int sumOfAllStorage, int sumOfAllNeeds) {
        switch (PROBLEM_TYPE) {
            //Если у нас запасов больше, чем потребностей
            case 0 -> fictionalCustomerCase(sumOfAllStorage - sumOfAllNeeds, table);

            //Если у нас потребностей больше, чем есть на складах
            case -1 -> fictionalStorageCase(sumOfAllNeeds - sumOfAllStorage, table);

            default -> {
                try {
                    throw new Exception("Неверный problemType");
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
        System.out.println("Решение транспортной задачи");

        System.out.println("Введите количество поставщиков");
        int storageAmount = in.nextInt();

        System.out.println("Введите значения складов");
        int[] storage = new int[storageAmount];
        inputArray(storage, storageAmount);

        System.out.println("Введите количество заказчиков");
        int customersAmount = in.nextInt();

        System.out.println("Введите потребности заказчиков");
        int[] need = new int[customersAmount];
        inputArray(need, customersAmount);

        System.out.println("Введите матрицу стоимостей");
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

        //Если задача открытого вида
        if (PROBLEM_TYPE != 1) {
            //Приводим задачу к закрытому виду
            toCloseProblem(table, sumAllStorages, sumAllNeeds);
        }


        System.out.println("""
                Выберите метод решения
                1.Метод северо-западного угла
                2.Метод минимального элемента
                3.Метод аппроксимации Фогеля
                4.Метод двойного предпочтения""");

        int[][] basePlan;

        switch (in.nextByte()) {
            case 1 -> basePlan = northWestMethod(table);
            case 2 -> basePlan = minCostMethod(table);
            case 3 -> basePlan = vogelApproximationMethod(table);
            case 4 -> basePlan = doublePreferenceMethod(table);
            default -> throw new IllegalStateException("Unexpected value");
        }

        //Получаем матрицу опорного плана
        System.out.println(calculateBasePlan(storageAmount, customersAmount, cost, basePlan));

        //Проверяем план на оптимальность
        potentialMethod(basePlan, table.storageAmount, table.customerAmount, cost);

        System.out.println(calculateBasePlan(storageAmount, customersAmount, cost, basePlan));
    }
}
