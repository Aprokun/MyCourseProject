import model.TransportTaskTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Tuple;
import utils.UtilUseCase;

import java.util.Arrays;

class UtilFunctionsTest {

    @Test
    void fillMatrixWithTest() {
        final int[][] expected = new int[][] {
            {2, 2, 2, 2},
            {2, 2, 2, 2},
            {2, 2, 2, 2},
            {2, 2, 2, 2}
        };

        int[][] actual = new int[4][4];

        UtilUseCase.fillMatrixWith(2, 4, 4, actual);

        Assertions.assertTrue(Arrays.stream(actual).allMatch(ints -> Arrays.stream(ints).allMatch(v -> v == 2)));
    }

    @Test
    void isEmptyColTest() {
    }

    @Test
    void findMinMatrixTest() {
    }

    @Test
    void findTwoMinValuesTest() {
        int[] arr = new int[] {4, 9, 10, 2, 4, 5, 1};
        int[] arrMask = new int[] {1, 1, 0, 1, 1, 0, 1};

        Tuple<Integer> expected = new Tuple<>(1, 2);
        Tuple<Integer> actual = UtilUseCase.findTwoMinValues(7, arr, arrMask);

        Assertions.assertEquals(expected.x, actual.x);
        Assertions.assertEquals(expected.y, actual.y);
    }

    @Test
    void findTwoMinValuesInCol() {
    }

    @Test
    void getIndexMaxTest() {
        int[] arr1 = new int[] {2, 5, 7, 1, 2, 3, 5, 0};
        int[] arr2 = new int[] {6, 7, 2, 1, 13, 6, 0, 0};

        int expected1 = 2, expected2 = 4;
        int actual1 = UtilUseCase.getIndexMax(8, arr1), actual2 = UtilUseCase.getIndexMax(8, arr2);

        Assertions.assertEquals(expected1, actual1);
        Assertions.assertEquals(expected2, actual2);
    }

    @Test
    void calculateDiffsTest() {
        int[] need = new int[] {9, 10, 6};
        int[] storage = new int[] {15, 5, 5};
        int[][] cost = new int[][] {
                {5, 2, 4},
                {11, 13, 1},
                {8, 9, 3}
        };
        TransportTaskTable table = new TransportTaskTable(storage, need, cost);

        int[] expectedMinDiffStorageValues = new int[] {2, 10, 5};
        int[] expectedMinDiffCustomerValues = new int[] {3, 7, 2};

        int[] actualMinDiffStorageValues = new int[3];
        int[] actualMinDiffCustomerValues = new int[3];

        UtilUseCase.calculateDiffs(table, actualMinDiffStorageValues, actualMinDiffCustomerValues);

        Assertions.assertArrayEquals(expectedMinDiffCustomerValues, actualMinDiffCustomerValues);
        Assertions.assertArrayEquals(expectedMinDiffStorageValues, actualMinDiffStorageValues);
    }

    @Test
    void getIndexMinRowTest() {
    }

    @Test
    void getIndexMinColTest() {
    }

    @Test
    void findMaxMatrixMaskTest() {
    }

    @Test
    void recalculateBasePlanTest() {
    }

    @Test
    void isOptimalBasePlanTest() {
        int[][] deltaCost1 = new int[][] {
                {1, 0, 0},
                {18, 0, 3},
                {9, 7, -7},
                {0, -1, 1}
        };
        int[][] deltaCost2 = new int[][] {
                {3, 0},
                {7, 7},
                {0, 0}
        };

        Tuple<Integer> expected1 = new Tuple<>(2, 2);
        Tuple<Integer> expected2 = new Tuple<>(null, null);

        Tuple<Integer> actual1 = UtilUseCase.isOptimalBasePlan(4, 3, deltaCost1);
        Tuple<Integer> actual2 = UtilUseCase.isOptimalBasePlan(3, 2, deltaCost2);

        Assertions.assertAll(() -> {
            Assertions.assertEquals(expected1.x, actual1.x);
            Assertions.assertEquals(expected1.y, actual1.y);

            Assertions.assertEquals(expected2.x, actual2.x);
            Assertions.assertEquals(expected2.y, actual2.y);
        });
    }

    @Test
    void isAllTrueTest() {
        boolean[] arr1 = new boolean[] {true, true, true, false, true};
        boolean[] arr2 = new boolean[] {true, true, true, true, true, true};
        boolean[] arr3 = new boolean[] {false, true, true, true};

        Assertions.assertAll(() -> {
            Assertions.assertFalse(UtilUseCase.isAllTrue(arr1, 5));
            Assertions.assertTrue(UtilUseCase.isAllTrue(arr2, 6));
            Assertions.assertFalse(UtilUseCase.isAllTrue(arr3, 4));
        });
    }

    @Test
    void isDegeneratePlanTest() {
    }

    @Test
    void checkOpenCloseTest() {
    }

    @Test
    void toCloseProblemTest() {
    }
}