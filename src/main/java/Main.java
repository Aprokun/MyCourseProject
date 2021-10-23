import model.Shipment;
import model.TransportTaskTable;

import java.util.Arrays;

import static methods.MethodUseCase.*;
import static utils.UtilUseCase.*;

public class Main {
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

        TransportTaskTable table = new TransportTaskTable(storage, need, cost);

        final int sumAllStorages = Arrays.stream(storage).sum();
        final int sumAllNeeds = Arrays.stream(need).sum();

        PROBLEM_TYPE = checkOpenClose(sumAllStorages, sumAllNeeds);

        //���� ������ ��������� ����
        if (PROBLEM_TYPE != 1) getBalanced(table, sumAllStorages, sumAllNeeds);

        System.out.println("""
                �������� ����� �������
                1.����� ������-��������� ����
                2.����� ������������ ��������
                3.����� ������������� ������
                4.����� �������� ������������""");

        Shipment[][] basePlan;

        switch (in.nextByte()) {
            case 1 -> basePlan = northWestMethod(table);
            case 2 -> basePlan = minCostMethod(table);
            case 3 -> basePlan = vogelApproximationMethod(table);
            case 4 -> basePlan = doublePreferenceMethod(table);
            default -> throw new IllegalStateException("Unexpected value");
        }

        System.out.println(calculateBasePlan(storageAmount, customersAmount, table.cost, basePlan));

        fixDegenerate(table, basePlan);

        while (!isOptimal(table, basePlan)) {
            steppingStone(table, basePlan);
            fixDegenerate(table, basePlan);
        }

        System.out.println(calculateBasePlan(storageAmount, customersAmount, table.cost, basePlan));
    }
}
