package model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Shipment {
    public final int costPerUnit;
    public final int row, col;
    public int quantity;
}
