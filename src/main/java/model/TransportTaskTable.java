package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Builder
public class TransportTaskTable {
    public int[] storage;
    public int[] need;
    public int[][] cost;
}
