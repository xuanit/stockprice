package assignment.service;

import assignment.controller.DateClose;
import assignment.model.DayMovingAverage;
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

    static final String API = "https://www.quandl.com/api/v3/datasets/WIKI/";

    private static final int DATE_DELTA = 200 * 2;

    @Autowired
    private QuandlDataSource quandlDataSource;

    public ClosePriceService(QuandlDataSource quandlDataSource) {
        this.quandlDataSource = quandlDataSource;
    }

    public Prices getClosePrices(String ticker, LocalDate startDate, LocalDate endDate) throws QuandlDataSource.InvalidTicker {
        return quandlDataSource.getClosePrices(ticker, startDate, endDate);
    }

    public DayMovingAverage get200DMA(String ticker, LocalDate startDate) throws QuandlDataSource.InvalidTicker {
        Prices prices = quandlDataSource.getClosePrices(ticker, startDate, 200);
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
}
