package com.dmitriy.springinvesttinkoff.models;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class MyPortfolio {
    String figi;
    String ticker;
    String instrumentType;
    String name;
    double balance;
    String currency;
    double valueUp;
    double valueBuy;
    double priceNow;
    double priceClose;
}
