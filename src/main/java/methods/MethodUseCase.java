package methods;

import model.Shipment;
import model.TransportTaskTable;
import utils.Tuple;

import java.util.Arrays;

import static utils.UtilUseCase.*;


public class MethodUseCase {

    /**
     * Возвращает сумму опорного плана, найденную с помощью метода "северо-западного" угла.
     *
     * @param table таблица транспортной задачи.
     * @return Матрицу опорного плана транспортной задачи.
     */
    public static Shipment[][] northWestMethod(TransportTaskTable table) {
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
     * Вычисляет сумму опорного плана, найденную с помощью метода минимальной стоимости.
     *
     * @param table таблица транспортной задачи.
     * @return Матрицу опорного плана.
     */
    public static Shipment[][] minCostMethod(TransportTaskTable table) {
        Shipment[][] basePlan = new Shipment[table.storage.length][table.need.length];

        while (!Arrays.stream(table.storage).allMatch(el -> el == 0) && !Arrays.stream(table.need).allMatch(el -> el == 0)) {
            final Tuple<Integer> min = findMinMatrix(table.storage.length, table.need.length, table.cost);

            if (table.storage[min.x] != 0 && table.need[min.y] != 0) {
                //Если потребность заказчика равна запасу склада
                if (table.storage[min.x] == table.need[min.y]) {
                    basePlan[min.x][min.y] = new Shipment(table.cost[min.x][min.y], min.x, min.y, table.storage[min.x]);
                    table.storage[min.x] = table.need[min.y] = 0;
                }
                //Иначе, если запас склада больше потребности заказчика
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


    /**
     * Вычисляет матрицу опорного плана, найденную с помощью метода "аппроксимации Фогеля"
     *
     * @param table таблица транспортной задачи.
     * @return Матрицу опорного плана.
     */
    public static Shipment[][] vogelApproximationMethod(TransportTaskTable table) {
        //Массив дельт для складов
        int[] diffStorageValues = new int[table.storage.length];

        //Массив дельт для заказчиков
        int[] diffCustomerValues = new int[table.need.length];

        Shipment[][] basePlan = new Shipment[table.storage.length][table.need.length];

        do {
            calculateDiffs(table, diffStorageValues, diffCustomerValues);

            final int indexMaxStorageDiff = getIndexMax(table.storage.length, diffStorageValues);
            final int indexMaxNeedDiff = getIndexMax(table.need.length, diffCustomerValues);

            if (diffStorageValues[indexMaxStorageDiff] >= diffCustomerValues[indexMaxNeedDiff]) {
                final int indexMinRow = getIndexMinRow(table.need.length, table.cost, indexMaxStorageDiff, table.need);
                lol(table, basePlan, indexMaxStorageDiff, indexMinRow);
            } else {
                final int indexMinCol = getIndexMinCol(table.storage.length, table.cost, indexMaxNeedDiff, table.storage);
                lol(table, basePlan, indexMinCol, indexMaxNeedDiff);
            }

        } while (!isAllConditionsMet(table));

        return basePlan;
    }


    /**
     * Вычисляет опорный план, найденный методом "двойного предпочтения".
     *
     * @param table таблица транспортной задачи.
     * @return Матрица опорного плана.
     */
    public static Shipment[][] doublePreferenceMethod(TransportTaskTable table) {
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
}
