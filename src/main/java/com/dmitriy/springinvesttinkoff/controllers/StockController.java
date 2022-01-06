package com.dmitriy.springinvesttinkoff.controllers;

import com.dmitriy.springinvesttinkoff.models.MyPortfolio;
import com.dmitriy.springinvesttinkoff.models.Stock;
import com.dmitriy.springinvesttinkoff.service.StockService;
import dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;


@RestController
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    @GetMapping("/stocks/{ticker}")
    public Stock getStock(@PathVariable String ticker){
       return stockService.getStockByTicker(ticker);
    }

    @PostMapping("/stocks/getStocksByTickers")
    public StocksDto getStocksByTickers(@RequestBody TickersDto tickersDto){
        return stockService.getStocksByTickers(tickersDto);
    }

    @PostMapping("/stocks/prices")
    public StocksPricesDto getPrices(@RequestBody FigiesDto figiesDto){
        return stockService.getPrices(figiesDto);
    }



    @GetMapping("/stocks/portfolioPrices")
    public List<MyPortfolio>  getPortfolioPrices() throws ExecutionException, InterruptedException {
        PortfolioDto portfolioDto = stockService.getPortfolioDto();
        return stockService.getPortfolioDtoPrice(
                portfolioDto,
                stockService.getPricesProfile(portfolioDto));
    }
    @GetMapping("/stocks/portfolioResults")
    public ResultsDto  getPortfolioResults() throws ExecutionException, InterruptedException {
        PortfolioDto portfolioDto = stockService.getPortfolioDto();
        return stockService.getResultsProfile( portfolioDto,
                stockService.getPricesProfile(portfolioDto));
    }

}
