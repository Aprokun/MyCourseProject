package methods;

import model.Shipment;
import model.TransportTaskTable;
import utils.Tuple;

import java.util.Arrays;

import static utils.UtilUseCase.*;


public class MethodUseCase {

    /**
     * ���������� ����� �������� �����, ��������� � ������� ������ "������-���������" ����.
     *
     * @param table ������� ������������ ������.
     * @return ������� �������� ����� ������������ ������.
     */
    public static Shipment[][] northWestMethod(TransportTaskTable table) {
        //������� �������� �����
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
     * ��������� ����� �������� �����, ��������� � ������� ������ ����������� ���������.
     *
     * @param table ������� ������������ ������.
     * @return ������� �������� �����.
     */
    public static Shipment[][] minCostMethod(TransportTaskTable table) {
        Shipment[][] basePlan = new Shipment[table.storage.length][table.need.length];

        while (!Arrays.stream(table.storage).allMatch(el -> el == 0) && !Arrays.stream(table.need).allMatch(el -> el == 0)) {
            final Tuple<Integer> min = findMinMatrix(table.storage.length, table.need.length, table.cost);

            if (table.storage[min.x] != 0 && table.need[min.y] != 0) {
                //���� ����������� ��������� ����� ������ ������
                if (table.storage[min.x] == table.need[min.y]) {
                    basePlan[min.x][min.y] = new Shipment(table.cost[min.x][min.y], min.x, min.y, table.storage[min.x]);
                    table.storage[min.x] = table.need[min.y] = 0;
                }
                //�����, ���� ����� ������ ������ ����������� ���������
                else if (table.storage[min.x] > table.need[min.y]) {
                    table.storage[min.x] -= table.need[min.y];
                    basePlan[min.x][min.y] = new Shipment(table.cost[min.x][min.y], min.x, min.y, table.need[min.y]);
                    table.need[min.y] = 0;
                }
                //�����, ���� ����������� ��������� ������ ������ ������
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
     * ��������� ������� �������� �����, ��������� � ������� ������ "������������� ������"
     *
     * @param table ������� ������������ ������.
     * @return ������� �������� �����.
     */
    public static Shipment[][] vogelApproximationMethod(TransportTaskTable table) {
        //������ ����� ��� �������
        int[] diffStorageValues = new int[table.storage.length];

        //������ ����� ��� ����������
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
     * ��������� ������� ����, ��������� ������� "�������� ������������".
     *
     * @param table ������� ������������ ������.
     * @return ������� �������� �����.
     */
    public static Shipment[][] doublePreferenceMethod(TransportTaskTable table) {
        //����� ������� �������� �����
        int[][] mask = new int[table.storage.length][table.need.length];

        //������� �������� �����
        Shipment[][] basePlan = new Shipment[table.storage.length][table.need.length];

        do {
            //���������� ����� ������
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
