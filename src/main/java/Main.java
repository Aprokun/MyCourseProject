import model.Shipment;
import model.TransportTaskTable;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.toCollection;

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


    /**
     * Заполняет каждую ячейку матрицы.
     *
     * @param num число, которым заполняется матрица
     * @param m   количество строк матрицы
     * @param n   количество столбцов матрицы
     * @param a   матрица
     */
    public static void fillMatrixWith(final int num, final int m, final int n, int[][] a) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                a[i][j] = num;
            }
        }
    }


    /**
     * Возвращает сумму опорного плана, найденную с помощью метода "северо-западного" угла.
     *
     * @param table таблица транспортной задачи.
     * @return Матрицу опорного плана транспортной задачи.
     */
    static Shipment[][] northWestMethod(TransportTaskTable table) {
        //Матрица опорного плана
        Shipment[][] basePlan = new Shipment[table.storage.length][table.need.length];

        for (int i = 0, nw = 0; i < table.storage.length; ++i) {
            for (int j = nw; j < table.need.length; ++j) {
                int quantity = Math.min(table.storage[i], table.need[j]);

                if (quantity > 0) {
                    basePlan[i][j] = new Shipment(table.cost[i][j], i, j, quantity);

                    table.storage[i] -= quantity;
                    table.need[j] -= quantity;

                    if (table.storage[i] == 0) {
                        nw = j;
                        break;
                    }
                }
            }
        }

        return basePlan;
    }


    /**
     * Строит цикл для перевозки
     *
     * @param basePlan опорный план транспортной задачи.
     * @param s        перевозка, для которой осуществляется построение цикла.
     * @return Массив перевозок цикла перерасчёта,
     * иначе - пустой массив.
     */
    static Shipment[] getClosedPath(Shipment[][] basePlan, Shipment s) {
        LinkedList<Shipment> path = matrixToList(basePlan);
        path.addFirst(s);

        //Удаляем перевозки, у которых нет хотя бы одного соседа по столбцу/строке
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
     * Осуществляет поиск соседних клеток по строке/столбцу.
     *
     * @param s    перевозка из матрицы опорного плана, для которой осуществляется поиск
     *             соседних клеток в цикле.
     * @param path возможный цикл перерасчёта.
     * @return Матрицу перевозок цикла с клетками, имеющими соседние клетки по строке/столбцу.
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
     * Исправляет возникшую вырожденность опорного плана транспортной задачи.
     *
     * @param table    таблица транспортной задачи.
     * @param basePlan опорный план транспортной задачи.
     */
    static void fixDegenerate(TransportTaskTable table, Shipment[][] basePlan) {
        final int eps = 0;

        if (table.storage.length + table.need.length - 1 != matrixToList(basePlan).size()) {
            for (int i = 0; i < table.storage.length; i++) {
                for (int j = 0; j < table.need.length; j++) {
                    //Если клетка пустая
                    if (basePlan[i][j] == null) {
                        //Создаём фиктивную перевозку
                        Shipment dummy = new Shipment(table.cost[i][j], i, j, eps);
                        //Если у нас получился цикл длиной 0 (т.е. ацикличный опорный план)
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
     * Алгоритм пошагового улучшения опорного плана.
     * Заключается в том, что для каждой пустой клетки опорного плана строится цикл перерасчёта
     * и сравнивается с предыдущим. В итоге мы получаем цикл, который максимально снижает
     * общую стоимость перевозок опорного плана.
     *
     * @param table    таблица транспортной задачи
     * @param basePlan таблица опорного плана.
     */
    static void steppingStone(TransportTaskTable table, Shipment[][] basePlan) {
        //Максимальное сокращение опорного плана
        int maxReduction = 0;
        //Цикл перевозок для максимального сокращения плана
        Shipment[] move = null;
        //Клетка матрицы опорного плана, которая должна
        //исчезнуть из опорного плана после перерасчёта
        Shipment leaving = null;

        for (int row = 0; row < basePlan.length; row++) {
            for (int col = 0; col < basePlan[row].length; col++) {
                if (basePlan[row][col] != null) continue;

                //Фиктивная перевозка
                Shipment dummy = new Shipment(table.cost[row][col], row, col, 0);

                //Получаем цикл для фиктивной перевозки
                Shipment[] path = getClosedPath(basePlan, dummy);

                //Текущее сокращение общей стоимости опорного плана
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

                //Если текущее сокращение минимального плана получилось
                //меньше максимального
                if (reduction < maxReduction) {
                    move = path;
                    leaving = leavingCandidate;
                    maxReduction = reduction;
                }
            }
        }

        //Если найден цикл перерасчёта и присутствует клетка для удаления
        //из матрицы опорного плана
        if (move != null && leaving != null) {
            performReallocation(basePlan, move, leaving);
        }
    }

    /**
     * Производит перерасчёт опорного плана трнаспортной задачи.
     *
     * @param basePlan опорный план транспортной задачи.
     * @param move     цикл перерасчёта.
     * @param leaving  клетка, которая должна быть удалена из опорного
     *                 плана после перерасчёта.
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
    static Shipment[][] minCostMethod(TransportTaskTable table) {
        //Матрица опорного плана
        Shipment[][] basePlan = new Shipment[table.storage.length][table.need.length];

        while (!Arrays.stream(table.storage).allMatch(el -> el == 0) && !Arrays.stream(table.need).allMatch(el -> el == 0)) {
            final Tuple<Integer> min = findMinMatrix(table.storage.length, table.need.length, table.cost);

            if (table.storage[min.x] != 0 && table.need[min.y] != 0) {
                //Если потребность заказчика равна запасу скалада
                if (table.storage[min.x] == table.need[min.y]) {
                    basePlan[min.x][min.y] = new Shipment(table.cost[min.x][min.y], min.x, min.y, table.storage[min.x]);
                    table.storage[min.x] = table.need[min.y] = 0;
                }
                //Иначе, если запас скалада больше потребности заказчика
                else if (table.storage[min.x] > table.need[min.y]) {
                    table.storage[min.x] -= table.need[min.y];
                    basePlan[min.x][min.y] = new Shipment(table.cost[min.x][min.y], min.x, min.y, table.need[min.y]);
                    table.need[min.y] = 0;
                }
                //Иначе, если потребность заказчика больше запаса склада
                else if (table.need[min.y] > table.storage[min.x]) {
                    table.need[min.y] -= table.storage[min.x];
                    basePlan[min.x][min.y] = new Shipment(table.cost[min.x][min.y], min.x, min.y, table.storage[min.x]);
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

    public static void calculateDiffs(TransportTaskTable table,
                                      int[] minDiffStorageValues,
                                      int[] minDiffCustomerValues) {
        //Подсчёт разности по строкам
        for (int i = 0; i < table.storage.length; ++i) {
            final Tuple<Integer> storageMinValues = findTwoMinValues(table.need.length,
                    table.cost[i],
                    table.need);

            if (table.storage[i] != 0) minDiffStorageValues[i] = storageMinValues.y - storageMinValues.x;
            else minDiffStorageValues[i] = -1;
        }

        //Подсчёт разности по столбцам
        for (int j = 0; j < table.need.length; ++j) {
            final Tuple<Integer> customerMinValues = findTwoMinValuesInCol(table.storage.length,
                    table.cost,
                    j,
                    table.storage);

            if (table.need[j] != 0) minDiffCustomerValues[j] = customerMinValues.y - customerMinValues.x;
            else minDiffCustomerValues[j] = -1;
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
    static Shipment[][] vogelApproximationMethod(TransportTaskTable table) {
        //Массив дельт для складов
        int[] diffStorageValues = new int[table.storage.length];

        //Массив дельт для заказчиков
        int[] diffCustomerValues = new int[table.need.length];

        //Матрица опорного плана
        Shipment[][] basePlan = new Shipment[table.storage.length][table.need.length];

        do {
            calculateDiffs(table, diffStorageValues, diffCustomerValues);

            final int indexMaxStorageDiff = getIndexMax(table.storage.length, diffStorageValues);
            final int indexMaxNeedDiff = getIndexMax(table.need.length, diffCustomerValues);

            if (diffStorageValues[indexMaxStorageDiff] >= diffCustomerValues[indexMaxNeedDiff]) {
                final int indexMinRow = getIndexMinRow(table.need.length, table.cost, indexMaxStorageDiff, table.need);
                lol(table, basePlan, indexMaxStorageDiff, indexMinRow);
            } else {
                final int indexMinCol = getIndexMinCol(table.storage.length, table.cost, indexMaxNeedDiff);
                lol(table, basePlan, indexMinCol, indexMaxNeedDiff);
            }

        } while (!isAllConditionsMet(table));

        return basePlan;
    }


    /**
     * Проверяет удовлетворённость всех условий для нахождения опорного плана.
     *
     * @param table таблица транспортной задачи
     * @return Истину, если все потребности удовлетворены и все склады пустые,
     * иначе - ложь
     */
    private static boolean isAllConditionsMet(TransportTaskTable table) {
        return Arrays.stream(table.storage).allMatch(el -> el == 0) && Arrays.stream(table.need).allMatch(el -> el == 0);
    }

    private static void lol(TransportTaskTable table, Shipment[][] basePlan, int i, int j) {
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
    static Shipment[][] doublePreferenceMethod(TransportTaskTable table) {
        //Маска матрицы опорного плана
        int[][] mask = new int[table.storage.length][table.need.length];

        //Матрица опорного плана
        Shipment[][] basePlan = new Shipment[table.storage.length][table.need.length];

        do {
            //Заполнение маски нулями
            fillMatrixWith(0, table.storage.length, table.need.length, mask);

            setRowPriorities(table, mask);
            setColPriorities(table, mask);

            Tuple<Integer> indexMaxPriorityCell = findMaxMatrixMask(table.storage.length, table.need.length, mask);

            int currMaxStorage = table.storage[indexMaxPriorityCell.x];
            int currMaxNeed = table.need[indexMaxPriorityCell.y];

            if (currMaxStorage != 0 && currMaxNeed != 0) {
                if (currMaxStorage >= currMaxNeed) {
                    basePlan[indexMaxPriorityCell.x][indexMaxPriorityCell.y] = new Shipment(
                            table.cost[indexMaxPriorityCell.x][indexMaxPriorityCell.y],
                            indexMaxPriorityCell.x,
                            indexMaxPriorityCell.y,
                            currMaxNeed);
                    table.storage[indexMaxPriorityCell.x] -= table.need[indexMaxPriorityCell.y];
                    table.need[indexMaxPriorityCell.y] = 0;
                } else {
                    basePlan[indexMaxPriorityCell.x][indexMaxPriorityCell.y] = new Shipment(
                            table.cost[indexMaxPriorityCell.x][indexMaxPriorityCell.y],
                            indexMaxPriorityCell.x,
                            indexMaxPriorityCell.y,
                            currMaxStorage);
                    table.need[indexMaxPriorityCell.y] -= table.storage[indexMaxPriorityCell.x];
                    table.storage[indexMaxPriorityCell.x] = 0;
                }
            }
        } while (!isAllConditionsMet(table));

        return basePlan;
    }

    private static void setColPriorities(TransportTaskTable table, int[][] mask) {
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

    private static void setRowPriorities(TransportTaskTable table, int[][] mask) {
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

    /**
     * Конвертирует матрицу опорного плана в ОЛС
     *
     * @param basePlan таблица опорного плана
     * @return ОЛС, получений в процессе конвертирования таблицы опорного плана
     */
    static LinkedList<Shipment> matrixToList(Shipment[][] basePlan) {
        return Arrays.stream(basePlan)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .collect(toCollection(LinkedList::new));
    }

    //Выполняет расчёт потенциалов для свободных клеток
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

    //Выполняет подсчёт потенциалов для базисных (занятых) клеток.
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
    Метод потенциалов.
    Приводит заданный опорный план basePlan к оптимальному плану.
    */
    static boolean isOptimal(TransportTaskTable table, Shipment[][] basePlan) {
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
     Возвращает сумму опорного плана матрицы опорного плана
     basePlan размера MxN с использованием матрицы стоимостей cost размера MxN.
     */
    static int calculateBasePlan(int m, int n, int[][] cost, Shipment[][] basePlan) {
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

            //Остальные случаи
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

        TransportTaskTable table = new TransportTaskTable(storage, need, cost);

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

        Shipment[][] basePlan;

        switch (in.nextByte()) {
            case 1 -> basePlan = northWestMethod(table);
            case 2 -> basePlan = minCostMethod(table);
            case 3 -> basePlan = vogelApproximationMethod(table);
            case 4 -> basePlan = doublePreferenceMethod(table);
            default -> throw new IllegalStateException("Unexpected value");
        }

        System.out.println(calculateBasePlan(storageAmount, customersAmount, table.cost, basePlan));

        assert basePlan != null;

        fixDegenerate(table, basePlan);
        while (!isOptimal(table, basePlan)) {
            steppingStone(table, basePlan);
            fixDegenerate(table, basePlan);
        }

        System.out.println(calculateBasePlan(storageAmount, customersAmount, table.cost, basePlan));
    }
}
