package assignment.datasource;

import assignment.model.InvalidTickerException;
import assignment.model.Prices;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Created by xuan on 11/6/2016.
 */
public interface DataSource {

    /**
     * gets close prices for ticker form startDate to endDate.
     * @param ticker ticker to get close prices for.
     * @param startDate startDate. Close prices are got from start date to end date.
     * @param endDate endDate. Close prices are got from start date to end date.
     * @return Price close prices.
     * @throws InvalidTickerException invalid ticker.
     */
    Prices getClosePrices(String ticker, LocalDate startDate, LocalDate endDate) throws InvalidTickerException;

    /**
     * get up to the provided number of close prices for ticker from start date.
     * @param ticker ticker to get close prices for.
     * @param startDate start date. a number of close prices are got beginning with start data.
     * @param limit number of close prices to get.
     * @return Prices. close prices.
     * @throws InvalidTickerException invalid ticker.
     */
    Prices getClosePrices(String ticker, LocalDate startDate, int limit) throws InvalidTickerException;

    /**
     * get up to the privided number of latest close prices for ticker.
     * @param ticker the ticket to get close prices for.
     * @param limit number of latest close prices to get.
     * @return Prices. close prices.
     * @throws InvalidTickerException invalid ticker
     */
    Prices getClosePrices(String ticker, int limit) throws InvalidTickerException;
}
