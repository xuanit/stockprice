package assignment.datasource;

import assignment.model.DateClose;
import assignment.model.InvalidTickerException;
import assignment.model.Prices;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Created by xuan on 11/2/2016.
 */
@Component
public class DefaultDataSource implements DataSource {

    private static Logger logger = LoggerFactory.getLogger(DefaultDataSource.class);

    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String COLUMN_INDEX = "column_index";
    public static final String LIMIT = "limit";
    public static final String ORDER = "order";
    public static final int CLOSE_COLUMN = 4;
    public static final int DATE_COLUMN = 0;
    public static final String ORDER_ASC = "asc";

    private DataHolder dataHolder;

    public DefaultDataSource(DataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }

    @Override
    public Prices getClosePrices(String ticker, LocalDate startDate, LocalDate endDate) throws InvalidTickerException {
        if(ticker == null) throw new NullPointerException("ticker is null");
        if(startDate == null) throw new NullPointerException("startDate is null");
        if(endDate == null) throw new NullPointerException("endDate is null");
        if(startDate.isAfter(endDate)) throw new IllegalArgumentException("startDate is after endDate");
        if(logger.isDebugEnabled()) {
            logger.debug("get close prices of {} from {} to {}", ticker, startDate, endDate);
        }

        Prices prices = this.dataHolder.getDataSet(ticker);

        if(logger.isDebugEnabled()) {
            logger.debug("got prices #{}", prices);
        }

        List<DateClose> dateCloses = prices.getDateCloses();
        DateClose dummyStartDateClose = new DateClose(startDate, BigDecimal.ZERO);
        DateClose dummyEndDateClose = new DateClose(endDate, BigDecimal.ZERO);
        int startCloseDateIndex = Collections.binarySearch(dateCloses, dummyStartDateClose);
        startCloseDateIndex = startCloseDateIndex >=0? startCloseDateIndex: Math.abs(startCloseDateIndex + 1);
        int endCloseDateIndex = Math.abs(Collections.binarySearch(dateCloses, dummyEndDateClose) + 1);
        List<DateClose> filteredDateCloses =  filteredDateCloses = dateCloses.subList(startCloseDateIndex, endCloseDateIndex);
        return new Prices(prices.getTicker(), filteredDateCloses, prices.getEtag());
    }

    @Override
    public Prices getClosePrices(String ticker, LocalDate startDate, int limit) throws InvalidTickerException {
        if(startDate == null) throw new NullPointerException("startDate is null");
        if(ticker == null) throw new NullPointerException("ticker is null");
        if(logger.isDebugEnabled()) {
            logger.debug("get {} close prices of {} from {}", limit, ticker, startDate);
        }

        Prices prices = this.dataHolder.getDataSet(ticker);

        if(logger.isDebugEnabled()) {
            logger.debug("got prices #{}", prices);
        }
        List<DateClose> dateCloses = prices.getDateCloses();
        DateClose startDateClose = new DateClose(startDate, BigDecimal.ZERO);
        int startCloseDateIndex = Collections.binarySearch(dateCloses, startDateClose);
        startCloseDateIndex = startCloseDateIndex >=0? startCloseDateIndex: Math.abs(startCloseDateIndex + 1);
        int endIndex = startCloseDateIndex + limit < dateCloses.size()? startCloseDateIndex + limit: dateCloses.size();
        List<DateClose> filteredDateCloses = dateCloses.subList(startCloseDateIndex, endIndex);
        return new Prices(prices.getTicker(), filteredDateCloses, prices.getEtag());
    }

    @Override
    public Prices getClosePrices(String ticker, int limit) throws InvalidTickerException {
        if(ticker == null) throw new NullPointerException("ticker is null");
        if(logger.isDebugEnabled()) {
            logger.debug("get {} latest close price of {}", limit, ticker);
        }
        Prices prices = this.dataHolder.getDataSet(ticker);

        if(logger.isDebugEnabled()) {
            logger.debug("got prices #{}", prices);
        }

        List<DateClose> dateCloses = prices.getDateCloses();
        int startIndex = dateCloses.size() > limit? dateCloses.size() - limit: 0;
        List<DateClose> filteredDateClose = dateCloses.subList(startIndex, dateCloses.size());
        return new Prices(prices.getTicker(), filteredDateClose, prices.getEtag());
    }
}
