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
    public static Shipment[][] northWestCorner(TransportTaskTable table) {
        //������� �������� �����
        Shipment[][] basePlan = new Shipment[table.supply.length][table.demand.length];

        for (int i = 0, nw = 0; i < table.supply.length; ++i) {
            for (int j = nw; j < table.demand.length; ++j) {
                int quantity = Math.min(table.supply[i], table.demand[j]);

                if (quantity > 0) {
                    basePlan[i][j] = new Shipment(table.cost[i][j], i, j, quantity);

                    table.supply[i] -= quantity;
                    table.demand[j] -= quantity;

                    if (table.supply[i] == 0) {
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
        Shipment[][] basePlan = new Shipment[table.supply.length][table.demand.length];

        while (!Arrays.stream(table.supply).allMatch(el -> el == 0) && !Arrays.stream(table.demand).allMatch(el -> el == 0)) {
            final Tuple<Integer> min = findMinMatrix(table.supply.length, table.demand.length, table.cost);

            if (table.supply[min.x] != 0 && table.demand[min.y] != 0) {
                //���� ����������� ��������� ����� ������ ������
                if (table.supply[min.x] == table.demand[min.y]) {
                    basePlan[min.x][min.y] = new Shipment(table.cost[min.x][min.y], min.x, min.y, table.supply[min.x]);
                    table.supply[min.x] = table.demand[min.y] = 0;
                }
                //�����, ���� ����� ������ ������ ����������� ���������
                else if (table.supply[min.x] > table.demand[min.y]) {
                    table.supply[min.x] -= table.demand[min.y];
                    basePlan[min.x][min.y] = new Shipment(table.cost[min.x][min.y], min.x, min.y, table.demand[min.y]);
                    table.demand[min.y] = 0;
                }
                //�����, ���� ����������� ��������� ������ ������ ������
                else if (table.demand[min.y] > table.supply[min.x]) {
                    table.demand[min.y] -= table.supply[min.x];
                    basePlan[min.x][min.y] = new Shipment(table.cost[min.x][min.y], min.x, min.y, table.supply[min.x]);
                    table.supply[min.x] = 0;
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
        int[] diffStorageValues = new int[table.supply.length];

        //������ ����� ��� ����������
        int[] diffCustomerValues = new int[table.demand.length];

        Shipment[][] basePlan = new Shipment[table.supply.length][table.demand.length];

        do {
            calculateDiffs(table, diffStorageValues, diffCustomerValues);

            final int indexMaxStorageDiff = getIndexMax(table.supply.length, diffStorageValues);
            final int indexMaxNeedDiff = getIndexMax(table.demand.length, diffCustomerValues);

            if (diffStorageValues[indexMaxStorageDiff] >= diffCustomerValues[indexMaxNeedDiff]) {
                final int indexMinRow = getIndexMinRow(table.cost, indexMaxStorageDiff, table.demand);
                lol(table, basePlan, indexMaxStorageDiff, indexMinRow);
            } else {
                final int indexMinCol = getIndexMinCol(table.cost, indexMaxNeedDiff, table.supply);
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
        int[][] mask = new int[table.supply.length][table.demand.length];

        //������� �������� �����
        Shipment[][] basePlan = new Shipment[table.supply.length][table.demand.length];

        do {
            //���������� ����� ������
            fillMatrixWith(0, table.supply.length, table.demand.length, mask);

            setRowPriorities(table, mask);
            setColPriorities(table, mask);

            Tuple<Integer> indexMaxPriorityCell = findMaxMatrix(table.supply.length, table.demand.length, mask);

            int currMaxStorage = table.supply[indexMaxPriorityCell.x];
            int currMaxNeed = table.demand[indexMaxPriorityCell.y];

            if (currMaxStorage != 0 && currMaxNeed != 0) {
                if (currMaxStorage >= currMaxNeed) {
                    basePlan[indexMaxPriorityCell.x][indexMaxPriorityCell.y] = new Shipment(
                            table.cost[indexMaxPriorityCell.x][indexMaxPriorityCell.y],
                            indexMaxPriorityCell.x,
                            indexMaxPriorityCell.y,
                            currMaxNeed);
                    table.supply[indexMaxPriorityCell.x] -= table.demand[indexMaxPriorityCell.y];
                    table.demand[indexMaxPriorityCell.y] = 0;
                } else {
                    basePlan[indexMaxPriorityCell.x][indexMaxPriorityCell.y] = new Shipment(
                            table.cost[indexMaxPriorityCell.x][indexMaxPriorityCell.y],
                            indexMaxPriorityCell.x,
                            indexMaxPriorityCell.y,
                            currMaxStorage);
                    table.demand[indexMaxPriorityCell.y] -= table.supply[indexMaxPriorityCell.x];
                    table.supply[indexMaxPriorityCell.x] = 0;
                }
            }
        } while (!isAllConditionsMet(table));

        return basePlan;
    }
}
