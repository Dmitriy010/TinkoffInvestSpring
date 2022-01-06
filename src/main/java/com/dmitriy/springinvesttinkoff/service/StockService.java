package com.dmitriy.springinvesttinkoff.service;

import com.dmitriy.springinvesttinkoff.models.MyPortfolio;
import com.dmitriy.springinvesttinkoff.models.Stock;
import dto.*;

import java.util.List;
import java.util.concurrent.ExecutionException;


public interface StockService {
    Stock getStockByTicker(String Ticker);
    StocksDto getStocksByTickers(TickersDto tickers);
    StocksPricesDto getPrices(FigiesDto figiesDto);
    StocksPricesDto getPricesProfile(PortfolioDto portfolioDto);
    PortfolioDto getPortfolioDto() throws ExecutionException, InterruptedException;
    List<MyPortfolio> getPortfolioDtoPrice(PortfolioDto portfolioDto, StocksPricesDto stocksPricesDto);
    ResultsDto getResultsProfile(PortfolioDto portfolioDto, StocksPricesDto stocksPricesDto);
}
