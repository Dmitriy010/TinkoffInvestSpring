package dto;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class StockPriceDto {
    String figi;
    Double priceNow;
    Double priceClose;
}
