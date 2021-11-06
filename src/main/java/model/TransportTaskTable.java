package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;

/**
 *  ласс, описывающий таблицу транспортной задачи.
 */
@AllArgsConstructor
@Setter
@Builder
public class TransportTaskTable {
    public int[] supply;
    public int[] demand;
    public int[][] cost;
}
