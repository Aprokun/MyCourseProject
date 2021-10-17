import model.TransportTaskTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BasePlanMethodsTest4 {
    private TransportTaskTable table;

    @BeforeEach
    void setUp() {
        int[] storage = new int[]{10, 6, 2};
        int[] need = new int[]{1, 9, 8};
        int[][] cost = new int[][]{
                {4, 1, 7},
                {2, 9, 2},
                {13, 4, 11}
        };
        table = new TransportTaskTable(storage, 3, need, 3, cost);
    }

    @Test
    void northWestMethodTest() {
        int expected = 47;
        int actual = Main.calculateBasePlan(3, 3, table.cost, Main.northWestMethod(table));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void minCostMethod() {
        int expected = 50;
        int actual = Main.calculateBasePlan(3, 3, table.cost, Main.minCostMethod(table));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void vogelApproximationMethod() {
        int expected = 45;
        int actual = Main.calculateBasePlan(3, 3, table.cost, Main.vogelApproximationMethod(table));
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void doublePreferenceMethod() {
        int expected = 47;
        int actual = Main.calculateBasePlan(3, 3, table.cost, Main.doublePreferenceMethod(table));
        Assertions.assertEquals(expected, actual);
    }
}