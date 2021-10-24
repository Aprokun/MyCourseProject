package model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class Shipment {
    public final int costPerUnit;
    public final int row, col;
    public int quantity;
}
