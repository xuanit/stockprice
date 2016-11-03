package assignment.service;

import assignment.model.DateClose;
import assignment.model.DayMovingAverage;
import assignment.model.DayMovingAverageList;
import assignment.model.Prices;
import assignment.datasource.QuandlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by xuan on 11/1/2016.
 */
@Service
public class ClosePriceService {

    private static Logger logger = LoggerFactory.getLogger(ClosePriceService.class);

    @Autowired
    private QuandlDataSource quandlDataSource;

    public ClosePriceService(QuandlDataSource quandlDataSource) {
        this.quandlDataSource = quandlDataSource;
    }

    public Prices getClosePrices(String ticker, LocalDate startDate, LocalDate endDate) throws QuandlDataSource.InvalidTicker {
        return quandlDataSource.getClosePrices(ticker, startDate, endDate);
    }

    public DayMovingAverage get200DMA(String tickerSymbol, LocalDate startDate) throws QuandlDataSource.InvalidTicker {
        Prices prices = quandlDataSource.getClosePrices(tickerSymbol, startDate, 200);
        if(prices == null || prices.getDateCloses() == null || prices.getDateCloses().size() < 200) {
            return null;
        }
        BigDecimal sumOfPrices = BigDecimal.ZERO;
        for(int index = 0; index <  200; ++index){
            DateClose dateClose = prices.getDateCloses().get(index);
            sumOfPrices = sumOfPrices.add(dateClose.getPrice());
        }
        BigDecimal avg = sumOfPrices.divide(BigDecimal.valueOf(200));
        DayMovingAverage dayMovingAverage = new DayMovingAverage(prices.getTicker(), avg);
        return dayMovingAverage;
    }

    public LocalDate getFirstStartDateHaving200DMA(String ticker) throws QuandlDataSource.InvalidTicker {
        Prices prices = quandlDataSource.getClosePrices(ticker, 200);
        if(prices != null && prices.getDateCloses() != null && prices.getDateCloses().size() == 200) {
            List<DateClose> dateCloses = prices.getDateCloses();
            return dateCloses.get(0).getDate();
        }
        return  null;
    }

    public DayMovingAverageList get200DMAList(List<String> tickerSymbols, LocalDate startDate) {
        DayMovingAverageList dmaList = new DayMovingAverageList(startDate);
        for(String tickerSymbol: tickerSymbols) {
            if("".equals(tickerSymbol)) {
                continue;
            }
            LocalDate tickerStartDate = startDate;
            DayMovingAverage dma = null;
            try {
                dma = this.get200DMA(tickerSymbol, startDate);
                if(dma == null){
                    tickerStartDate = this.getFirstStartDateHaving200DMA(tickerSymbol);
                    dma = this.get200DMA(tickerSymbol, tickerStartDate);

                }
                if(dma != null) {
                    dma.setStartDate(tickerStartDate);
                }
            } catch (QuandlDataSource.InvalidTicker invalidTicker) {
                dma = new DayMovingAverage(tickerSymbol, null);
                dma.setErrorMessage("Invalid Ticker Symbol.");
            }
            dmaList.getDayMovingAverages().add(dma);
        }
        return dmaList;
    }
}
