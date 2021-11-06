package model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * Класс, описывающий перевозку из склада к заказчику.
 */
@AllArgsConstructor
@EqualsAndHashCode
public class Shipment {
    public final int costPerUnit;
    public final int row, col;
    public int quantity;
}
