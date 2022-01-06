package com.dmitriy.springinvesttinkoff.service;

import com.dmitriy.springinvesttinkoff.currency.Currency;
import com.dmitriy.springinvesttinkoff.exceptions.StockNotFoundException;
import com.dmitriy.springinvesttinkoff.models.MyPortfolio;
import com.dmitriy.springinvesttinkoff.models.Results;
import com.dmitriy.springinvesttinkoff.models.Stock;
import dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrumentList;
import ru.tinkoff.invest.openapi.model.rest.Orderbook;
import ru.tinkoff.invest.openapi.model.rest.Portfolio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TinkoffStockService implements StockService{
    private final OpenApi openApi;
    @Async
    public CompletableFuture<MarketInstrumentList> getMarketInstrumentTicker(String ticker){
        var context = openApi.getMarketContext();
        return context.searchMarketInstrumentsByTicker(ticker);
    }


    @Override
    public Stock getStockByTicker(String ticker) {
        var listCF = getMarketInstrumentTicker(ticker);
        var list = listCF.join().getInstruments();
        if (list.isEmpty()){
            throw new StockNotFoundException(String.format("Stock %S not found", ticker));
        }
        var item = list.get(0);
        return new Stock(
                item.getTicker(),
                item.getFigi(),
                item.getName(),
                item.getType().getValue(),
                Currency.valueOf(item.getCurrency().getValue()),
                "Tinkoff"
        );
    }
    @Override
    public StocksDto getStocksByTickers(TickersDto tickers){
        List<CompletableFuture<MarketInstrumentList>> marketInstruments = new ArrayList<>();
        tickers.getTickers().forEach(ticker->marketInstruments.add(getMarketInstrumentTicker(ticker)));
        List<Stock> stocks = marketInstruments.stream()
                .map(CompletableFuture::join)
                .map(mi->{
                    if(!mi.getInstruments().isEmpty()){
                        return mi.getInstruments().get(0);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .map(mi->new Stock(
                        mi.getTicker(),
                        mi.getFigi(),
                        mi.getName(),
                        mi.getType().getValue(),
                        Currency.valueOf(mi.getCurrency().getValue()),
                        "Tinkoff"
                ))
                .collect(Collectors.toList());
        return new StocksDto(stocks);
    }
    @Async
    public CompletableFuture<Optional<Orderbook>> getOrderBookByFigi(String figi){
        var orderbook = openApi.getMarketContext().getMarketOrderbook(figi,0);
        return orderbook;
    }
    @Override
    public StocksPricesDto getPrices(FigiesDto figiesDto){
        List<CompletableFuture<Optional<Orderbook>>> orderBooks = new ArrayList<>();
        figiesDto.getFigies().forEach(figi->orderBooks.add(getOrderBookByFigi(figi)));
        var listPrices = orderBooks.stream()
                .map(CompletableFuture::join)
                .map(o->o.orElseThrow(()->new StockNotFoundException("Stock not found")))
                .map(orderbook -> new StockPriceDto(
                        orderbook.getFigi(),
                        orderbook.getLastPrice().doubleValue(),
                        orderbook.getClosePrice().doubleValue()
                ))
                .collect(Collectors.toList());
        return new StocksPricesDto(listPrices);
    }
    @Override
    public StocksPricesDto getPricesProfile(PortfolioDto portfolioDto){
        List<CompletableFuture<Optional<Orderbook>>> orderBooks = new ArrayList<>();
        portfolioDto.getPortfolioDto().forEach(el->orderBooks.add(getOrderBookByFigi(el.getFigi())));
        var listPricesProfile = orderBooks.stream()
                .map(CompletableFuture::join)
                .map(o->o.orElseThrow(()->new StockNotFoundException("Stock not found")))
                .map(orderbook -> new StockPriceDto(
                        orderbook.getFigi(),
                        orderbook.getLastPrice().doubleValue(),
                        orderbook.getClosePrice().doubleValue()
                ))
                .collect(Collectors.toList());
        return new StocksPricesDto(listPricesProfile);
    }
    @Override
    public PortfolioDto getPortfolioDto() throws ExecutionException, InterruptedException {
        Portfolio portfolio;
        List<MyPortfolio> myPortfolios = new ArrayList<>();
            portfolio = openApi.getPortfolioContext().getPortfolio(openApi.getUserContext().getAccounts().get().getAccounts().get(0).getBrokerAccountId()).join();
          portfolio.getPositions().forEach(el->myPortfolios.add(
                    new MyPortfolio(el.getFigi(),
                            el.getTicker(),
                            el.getInstrumentType().getValue(),
                            el.getName(),
                            el.getBalance().doubleValue(),
                            String.valueOf(getStockByTicker(el.getTicker()).getCurrency()),
                            el.getExpectedYield().getValue().doubleValue(),
                            el.getAveragePositionPrice().getValue().doubleValue(),
                            0.0,
                            0.0
                            )));

        return new PortfolioDto(myPortfolios);
    }
    @Override
    public List<MyPortfolio> getPortfolioDtoPrice(PortfolioDto portfolioDto, StocksPricesDto stocksPricesDto){
        List<MyPortfolio> portfolioPrice = new ArrayList<>();
        for(MyPortfolio port:portfolioDto.getPortfolioDto()) {
            for (StockPriceDto price : stocksPricesDto.getPrices()) {
                if (port.getFigi().equals(price.getFigi())) {
                    portfolioPrice.add(new MyPortfolio(port.getFigi(),
                            port.getTicker(),
                            port.getInstrumentType(),
                            port.getName(),
                            port.getBalance(),
                            port.getCurrency(),
                            port.getValueUp(),
                            port.getValueBuy(),
                            price.getPriceNow(),
                            price.getPriceClose()));
                }
            }
        }
        return portfolioPrice;
    }
    @Override
    public ResultsDto getResultsProfile(PortfolioDto portfolioDto, StocksPricesDto stocksPricesDto){
        List<MyPortfolio> portfolioDtoPrice = getPortfolioDtoPrice(portfolioDto, stocksPricesDto);
        List<Results> resultsList = new ArrayList<>();

        portfolioDtoPrice.forEach(el->resultsList.add(new Results(
                el.getName(),
                (1-el.getPriceClose()/el.getPriceNow())*100,
                (1-el.getPriceClose()/el.getPriceNow())*el.getPriceClose()*el.getBalance(),
                el.getValueBuy()*el.getBalance(),
                el.getPriceNow()*el.getBalance(),
                el.getPriceNow()/el.getValueBuy(),
                el.getPriceNow()*el.getBalance()-
                        el.getValueBuy()*el.getBalance(),
                el.getCurrency()
                 )));
         return new ResultsDto(resultsList);

    }
}
