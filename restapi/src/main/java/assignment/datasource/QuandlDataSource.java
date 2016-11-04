package assignment.datasource;

import assignment.model.DateClose;
import assignment.model.Prices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by xuan on 11/2/2016.
 */
@Component
public class QuandlDataSource {

    private static Logger logger = LoggerFactory.getLogger(QuandlDataSource.class);

    public static class InvalidTicker extends Exception{

    }

    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String COLUMN_INDEX = "column_index";
    public static final String LIMIT = "limit";
    public static final String ORDER = "order";
    public static final int CLOSE_COLUMN = 4;
    public static final int DATE_COLUMN = 0;
    public static final String ORDER_ASC = "asc";

    private DataHolder dataHolder;

    public QuandlDataSource(DataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }

    public Prices getClosePrices(String ticker, LocalDate startDate, LocalDate endDate) throws InvalidTicker {
        Prices prices = this.dataHolder.getDataSet(ticker);
        List<DateClose> dateCloses = prices.getDateCloses();
        DateClose dummyStartDateClose = new DateClose(startDate, BigDecimal.ZERO);
        DateClose dummyEndDateClose = new DateClose(endDate, BigDecimal.ZERO);
        int startCloseDateIndex = Collections.binarySearch(dateCloses, dummyStartDateClose);
        startCloseDateIndex = startCloseDateIndex >=0? startCloseDateIndex: Math.abs(startCloseDateIndex + 1);
        int endCloseDateIndex = Math.abs(Collections.binarySearch(dateCloses, dummyEndDateClose) + 1);
        List<DateClose> filteredDateCloses = dateCloses.subList(startCloseDateIndex, endCloseDateIndex);
        return new Prices(prices.getTicker(), filteredDateCloses);
    }

    public Prices getClosePrices(String ticker, LocalDate startDate, int limit) throws InvalidTicker {
        if(startDate == null) throw new NullPointerException("startDate is null");
        if(ticker == null) throw new NullPointerException("ticker is null");

        Prices prices = this.dataHolder.getDataSet(ticker);
        List<DateClose> dateCloses = prices.getDateCloses();
        DateClose startDateClose = new DateClose(startDate, BigDecimal.ZERO);
        int startCloseDateIndex = Math.abs(Collections.binarySearch(dateCloses, startDateClose));
        int endIndex = startCloseDateIndex + limit < dateCloses.size()? startCloseDateIndex + limit: dateCloses.size();
        List<DateClose> filteredDateCloses = dateCloses.subList(startCloseDateIndex, endIndex);
        return new Prices(prices.getTicker(), filteredDateCloses);
    }

    public Prices getClosePrices(String ticker, int limit) throws InvalidTicker {
        Prices prices = this.dataHolder.getDataSet(ticker);
        List<DateClose> dateCloses = prices.getDateCloses();
        int startIndex = dateCloses.size() > limit? dateCloses.size() - limit: 0;
        List<DateClose> filteredDateClose = dateCloses.subList(startIndex, dateCloses.size());
        return new Prices(prices.getTicker(), filteredDateClose);
    }
}
