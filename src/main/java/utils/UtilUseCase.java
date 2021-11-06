package utils;

import model.Shipment;
import model.TransportTaskTable;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

public class UtilUseCase {
    public static final Scanner in = new Scanner(System.in);

    /**
     * Тип транспортной задачи.
     * 1 - закрытая задача;
     * 0 - задача с фиктивным потребителем;
     * -1 - задачи с фиктивным складом;
     */
    public static int PROBLEM_TYPE;


    /**
     * Ввод массива.
     *
     * @param a массив.
     * @param n длина массива.
     */
    public static void inputArray(int[] a, final int n) {
        for (int i = 0; i < n; ++i) a[i] = in.nextInt();
    }


    /**
     * Ввод матрицы.
     *
     * @param m количество строк.
     * @param n количество столбцов.
     * @param a матрица.
     */
    public static void inputMatrix(final int m, final int n, int[][] a) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                a[i][j] = in.nextInt();
            }
        }
    }

    /**
     * Строит цикл для перевозки
     *
     * @param basePlan опорный план транспортной задачи.
     * @param s        перевозка, для которой осуществляется построение цикла.
     * @return Массив перевозок цикла перерасчёта, иначе - пустой массив.
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
     * Проверка опорного плана на вырожденность.
     *
     * @param basePlan опорный план.
     * @return Истину, если опорный план является вырожденным, иначе - ложь.
     */
    public static boolean isDegenerate(Shipment[][] basePlan) {
        long count = Arrays.stream(basePlan).mapToLong(ints -> Arrays.stream(ints).filter(Objects::nonNull).count()).sum();
        return count < basePlan[0].length + basePlan.length - 1;
    }


    /**
     * Исправляет возникшую вырожденность опорного плана транспортной задачи.
     *
     * @param table    таблица транспортной задачи.
     * @param basePlan опорный план транспортной задачи.
     */
    public static void fixDegenerate(TransportTaskTable table, Shipment[][] basePlan) {
        final int eps = 0;

        if (table.supply.length + table.demand.length - 1 != matrixToList(basePlan).size()) {
            for (int i = 0; i < table.supply.length; i++) {
                for (int j = 0; j < table.demand.length; j++) {
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
    public static void steppingStone(TransportTaskTable table, Shipment[][] basePlan) {
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


    /**
     * Маска матрицы a размера MxN, где уже использованный ранее элемент равен 1, иначе - 0
     */
    static int[][] mask = new int[50][50];


    /**
     * Поиск минимального элемента в матрице.
     *
     * @param m количество строк матрицы.
     * @param n количество столбцов матрицы.
     * @param a матрица.
     * @return Кортеж из индексов по строке и столбцу минимального
     * элемента матрицы.
     */
    public static Tuple<Integer> findMinMatrix(final int m, final int n, final int[][] a) {
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


    /**
     * Поиск двух минимальных элементов в массиве. Для транспортной задачи.
     *
     * @param n    размера массива.
     * @param a    массив.
     * @param need вспомогательный массив потребностей.
     * @return Кортеж из двух минимальных элементов (x, y), где x &lt; y.
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
     * Поиск двух минимальных элементов в столбце матрицы.
     *
     * @param m       количество строк матрицы.
     * @param a       матрица.
     * @param col     столбец, в котором осуществляется поиск.
     * @param storage вспомогательный массив складов.
     * @return Кортеж из двух минимальных элементов (x, y), где x &lt; y.
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
     * Поиск индекса максимального элемента массива.
     *
     * @param n длина массива.
     * @param a массив.
     * @return Индекс максимального элемента массива.
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
     * Подсчёт разниц двух минимальных элементов для строк/столбцов.
     * Используется в методе аппроксимации Фогеля. После использования заполняет
     * массивы, передающиеся в качестве параметров.
     *
     * @param table                 таблица транспортной задачи.
     * @param minDiffStorageValues  массив разниц строк.
     * @param minDiffCustomerValues массив разниц столбцов.
     */
    public static void calculateDiffs(TransportTaskTable table,
                                      int[] minDiffStorageValues,
                                      int[] minDiffCustomerValues) {
        //Подсчёт разности по строкам
        for (int i = 0; i < table.supply.length; ++i) {
            final Tuple<Integer> storageMinValues = findTwoMinValues(table.demand.length,
                    table.cost[i],
                    table.demand);

            if (table.supply[i] != 0) minDiffStorageValues[i] = storageMinValues.y - storageMinValues.x;
            else minDiffStorageValues[i] = -1;
        }

        //Подсчёт разности по столбцам
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
     * Поиск минимального элемента в строке.
     *
     * @param cost   матрица стоимостей.
     * @param row    индекс строки.
     * @param demand вспомогательный массив потребностей.
     * @return Индекс минимального элемента в строке
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
     * Поиск минимального элемента в столбце.
     *
     * @param cost   матрица стоимостей.
     * @param col    индекс столбца.
     * @param supply вспомогательный массив складов.
     * @return Индекс минимального элемента в столбце.
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
     * Проверяет удовлетворённость всех условий для нахождения опорного плана.
     *
     * @param table таблица транспортной задачи
     * @return Истину, если все потребности удовлетворены и все склады пустые,
     * иначе - ложь
     */
    public static boolean isAllConditionsMet(TransportTaskTable table) {
        return Arrays.stream(table.supply).allMatch(el -> el == 0) && Arrays.stream(table.demand).allMatch(el -> el == 0);
    }

    /**
     * Вспомогательная функция для vogelApproximationMethod.
     *
     * @param table    таблица транспортной задачи.
     * @param basePlan матрица опорного плана.
     * @param i        индекс склада.
     * @param j        индекс потребителя.
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
     * Поиск максимального элемента в матрице.
     * После определения этого элемента, возвращает его и затем удаляет.
     *
     * @param m количество строк матрицы.
     * @param n количество столбцов матрицы.
     * @param a матрица.
     * @return Кортеж индексов максимального элемента матрицы.
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
     * Увеличивает приоритеты элементов строк.
     * Используется в методе двойного предпочтения.
     *
     * @param table таблица опорного плана.
     * @param mask  маска приоритетов.
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
     * Увеличивает приоритеты элементов столбцов.
     * Используется в методе двойного предпочтения.
     *
     * @param table таблица опорного плана.
     * @param mask  маска приоритетов.
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

    /**
     * Выполняет расчёт потенциалов для свободных клеток.
     *
     * @param table     таблица транспортной задачи.
     * @param basePlan  матрица опорного плана.
     * @param u         потенциалы складов.
     * @param v         потенциалы потребителей.
     * @param deltaCost матрица потенциалов свободных клеток.
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
     * Выполняет подсчёт потенциалов для базисных (занятых) клеток.
     *
     * @param table    таблица транспортной задачи.
     * @param basePlan матрица опорного плана.
     * @param u        потенциалы складов.
     * @param v        потенциалы потребителей.
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
     * Метод потенциалов.
     * Приводит заданный опорный план к оптимальному плану.
     * @param table    таблица транспортной задачи.
     * @param basePlan матрица опорного плана.
     * @return Истина, если данный опорный план является оптимальным,
     * иначе - ложь.
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
     * Вычисляет общую стоимость всех перевозок матрицы опорного плана.
     *
     * @param table    таблица транспортной задачи.
     * @param basePlan матрица опорного плана.
     * @return Стоимость перевозок матрицы опорного плана.
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
     * Проверка закрытости/открытости транспортной задачи.
     *
     * @param sumOfAllSupplies сумма всех запасов.
     * @param sumOfAllDemands  сумма всех потребностей.
     * @return Возвращает 1, если данная транспортная задача является закрытой;
     * Возвращает 0, если транспортная задача является открытой и требует дополнительного склада;
     * Возвращает -1, иначе, если является открытой и требует дополнительного заказчика.
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
     * Приводит открытую транспортную задачу к закрытой.
     *
     * @param table            таблица транспортной задачи.
     * @param sumOfAllSupplies сумма всех запасов.
     * @param sumOfAllDemands  сумма всех потребностей.
     */
    public static void getBalanced(TransportTaskTable table, int sumOfAllSupplies, int sumOfAllDemands) {
        //Если у нас запасов больше, чем потребностей
        if (PROBLEM_TYPE == 0) fictionalDemandCase(sumOfAllSupplies - sumOfAllDemands, table);
            //Если у нас потребностей больше, чем есть на складах
        else if (PROBLEM_TYPE == -1) fictionalSupplyCase(sumOfAllDemands - sumOfAllSupplies, table);
    }

    /**
     * Добавление фиктивного потребителя.
     *
     * @param newDemand количество потребностей нового потребителя.
     * @param table     таблица транспортной задачи.
     */
    private static void fictionalDemandCase(int newDemand, TransportTaskTable table) {
        table.setDemand(add(table.demand, newDemand));
        for (int i = 0; i < table.supply.length; i++) table.cost[i] = add(table.cost[i], 0);
    }


    /**
     * Добавление фиктивного склада.
     *
     * @param newSupply количество запасов нового склада.
     * @param table     таблица транспортной задачи.
     */
    private static void fictionalSupplyCase(int newSupply, TransportTaskTable table) {
        table.setSupply(add(table.supply, newSupply));

        List<int[]> newCost = Arrays.stream(table.cost).collect(Collectors.toList());

        newCost.add(new int[table.demand.length]);

        table.setCost(newCost.toArray(new int[0][0]));
    }

    /**
     * Заполняет каждую ячейку матрицы.
     *
     * @param num число, которым заполняется ячейка матрица.
     * @param m   количество строк матрицы.
     * @param n   количество столбцов матрицы.
     * @param a   матрица.
     */
    public static void fillMatrixWith(final int num, final int m, final int n, int[][] a) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                a[i][j] = num;
            }
        }
    }

    /**
     * Выводит в консоль маршруты перевозок матрицы опорного плана.
     *
     * @param m        количество строк матрицы.
     * @param n        количество столбцов матрицы.
     * @param basePlan матрица опорного плана.
     */
    void printRoutes(final int m, final int n, final Shipment[][] basePlan) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                if (basePlan[i][j] != null) {
                    System.out.println(i + " склад ---" + basePlan[i][j].quantity + "---> " + j + " магазин");
                }
            }
        }
    }
}
