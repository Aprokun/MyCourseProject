import methods.MethodUseCase;
import model.TransportTaskTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.UtilUseCase;

class BasePlanMethodsTest2 {
    private TransportTaskTable table;

    @BeforeEach
    void setUp() {
        int[] storage = new int[] {5, 9, 17, 4};
        int[] need = new int[] {10, 17, 3, 5};
        int[][] cost = new int[][]{
                {13, 24, 5, 9},
                {9, 1, 4, 19},
                {4, 8, 4, 10},
                {13, 20, 7, 17}
        };
        table = new TransportTaskTable(storage, need, cost);
    }

    @Test
    void northWestMethodTest() {
        int expected = 308;
        int actual = UtilUseCase.calculateBasePlan(table, MethodUseCase.northWestCorner(table));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void minCostMethod() {
        int expected = 218;
        int actual = UtilUseCase.calculateBasePlan(table, MethodUseCase.minCostMethod(table));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void vogelApproximationMethod() {
        int expected = 188;
        int actual = UtilUseCase.calculateBasePlan(table, MethodUseCase.vogelApproximationMethod(table));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void doublePreferenceMethod() {
        int expected = 191;
        int actual = UtilUseCase.calculateBasePlan(table, MethodUseCase.doublePreferenceMethod(table));
        Assertions.assertEquals(expected, actual);
    }
}