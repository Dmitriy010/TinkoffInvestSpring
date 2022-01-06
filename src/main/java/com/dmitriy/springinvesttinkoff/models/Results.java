package com.dmitriy.springinvesttinkoff.models;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Results {
    String name;
    double todayPercent;
    double todayProfit;
    double balanceBuy;
    double balanceNow;
    double profitPercent;
    double profit;
    String currency;
}
