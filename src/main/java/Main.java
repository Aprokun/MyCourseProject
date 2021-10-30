import model.Shipment;
import model.TransportTaskTable;

import java.util.Arrays;

import static methods.MethodUseCase.*;
import static utils.UtilUseCase.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("������� ������������ ������");

        System.out.println("������� ���������� �����������");
        int supplyQuantity = in.nextInt();

        System.out.println("������� �������� �������");
        final int[] supply = new int[supplyQuantity];
        inputArray(supply, supplyQuantity);

        System.out.println("������� ���������� ����������");
        int demandQuantity = in.nextInt();

        System.out.println("������� ����������� ����������");
        final int[] demand = new int[demandQuantity];
        inputArray(demand, demandQuantity);

        System.out.println("������� ������� ����������");
        final int[][] cost = new int[supplyQuantity][demandQuantity];
        inputMatrix(supplyQuantity, demandQuantity, cost);

        TransportTaskTable table = new TransportTaskTable(supply.clone(), demand.clone(), cost.clone());

        final int sumAllSuplies = Arrays.stream(supply).sum();
        final int sumAllDemands = Arrays.stream(demand).sum();

        PROBLEM_TYPE = checkOpenClose(sumAllSuplies, sumAllDemands);

        //���� ������ ��������� ����
        if (PROBLEM_TYPE != 1) getBalanced(table, sumAllSuplies, sumAllDemands);

        System.out.println("""
                �������� ����� �������
                1.����� ������-��������� ����
                2.����� ������������ ��������
                3.����� ������������� ������
                4.����� �������� ������������""");

        Shipment[][] basePlan;

        switch (in.nextByte()) {
            case 1 -> basePlan = northWestCorner(table);
            case 2 -> basePlan = minCostMethod(table);
            case 3 -> basePlan = vogelApproximationMethod(table);
            case 4 -> basePlan = doublePreferenceMethod(table);
            default -> throw new IllegalStateException("Unexpected value");
        }

        System.out.println(calculateBasePlan(table, basePlan));

        while (isDegenerate(basePlan)) fixDegenerate(table, basePlan);

        while (!isOptimal(table, basePlan)) {
            steppingStone(table, basePlan);
            fixDegenerate(table, basePlan);
        }

        System.out.println(calculateBasePlan(table, basePlan));
    }
}
