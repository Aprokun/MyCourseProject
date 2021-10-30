import methods.MethodUseCase;
import model.TransportTaskTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.UtilUseCase;

class BasePlanMethodsTest3 {
    private TransportTaskTable table;

    @BeforeEach
    void setUp() {
        int[] storage = new int[] {17, 5, 3};
        int[] need = new int[]{10, 5, 5};
        int[][] cost = new int[][]{
                {4, 14, 20},
                {20, 9, 2},
                {13, 7, 11}
        };
        table = new TransportTaskTable(storage, need, cost);
    }

    @Test
    void northWestMethodTest() {
        int expected = 156;
        int actual = UtilUseCase.calculateBasePlan(table, MethodUseCase.northWestCorner(table));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void minCostMethod() {
        int expected = 99;
        int actual = UtilUseCase.calculateBasePlan(table, MethodUseCase.minCostMethod(table));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void vogelApproximationMethod() {
        int expected = 99;
        int actual = UtilUseCase.calculateBasePlan(table, MethodUseCase.vogelApproximationMethod(table));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void doublePreferenceMethod() {
        int expected = 99;
        int actual = UtilUseCase.calculateBasePlan(table, MethodUseCase.doublePreferenceMethod(table));
        Assertions.assertEquals(expected, actual);
    }
}