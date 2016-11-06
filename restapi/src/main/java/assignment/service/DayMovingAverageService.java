package assignment.service;

import assignment.datasource.DataSource;
import assignment.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DayMovingAverageService {

    private static Logger logger = LoggerFactory.getLogger(DayMovingAverageService.class);

    private final DataSource dataSource;

    private static final int NUM_OF_DAYS_CALCULATING_200DMA = 200;

    public DayMovingAverageService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * gets 200 day moving average price for ticker beginning with startDate.
     * @param ticker ticker that 200 day moving average price is calculated for.
     * @param startDate the startDate to calculate 200 day moving average.
     * @return optional of 200 day moving average price if there is enough data. Otherwise, empty optional is returned.
     * @throws InvalidTickerException invalid ticker.
     */
    public Optional<DayMovingAverage> get200DMA(String ticker, LocalDate startDate) throws InvalidTickerException {
        Prices prices = dataSource.getClosePrices(ticker, startDate, NUM_OF_DAYS_CALCULATING_200DMA);
        if (prices.getDateCloses().size() < NUM_OF_DAYS_CALCULATING_200DMA) {
            return Optional.empty();
        }
        DayMovingAverage dayMovingAverage = calculateDayMovingAverage(prices);
        dayMovingAverage.setStartDate(startDate);
        return Optional.ofNullable(dayMovingAverage);
    }

    private DayMovingAverage calculateDayMovingAverage(Prices prices) {
        assert prices != null;
        assert prices.getDateCloses() != null && prices.getDateCloses().size() == NUM_OF_DAYS_CALCULATING_200DMA;

        BigDecimal sumOfPrices = BigDecimal.ZERO;
        for (int index = 0; index < NUM_OF_DAYS_CALCULATING_200DMA; ++index) {
            DateClose dateClose = prices.getDateCloses().get(index);
            sumOfPrices = sumOfPrices.add(dateClose.getPrice());
        }
        BigDecimal avg = sumOfPrices.divide(BigDecimal.valueOf(NUM_OF_DAYS_CALCULATING_200DMA));
        DayMovingAverage dayMovingAverage = new DayMovingAverage(prices.getTicker(), avg);
        return dayMovingAverage;
    }

    /**
     * gets first start date that have enough data for 200 day moving average calculation..
     * @param ticker ticker to calculate 200 day moving average.
     * @return Optional of the start date. If there is no start date that has enough data, empty optional is returned.
     * @throws InvalidTickerException invalid ticker.
     */
    public Optional<LocalDate> getFirstStartDateHaving200DMA(String ticker) throws InvalidTickerException {
        Prices prices = dataSource.getClosePrices(ticker, NUM_OF_DAYS_CALCULATING_200DMA);
        if (prices != null && prices.getDateCloses() != null && prices.getDateCloses().size() == NUM_OF_DAYS_CALCULATING_200DMA) {
            List<DateClose> dateCloses = prices.getDateCloses();
            return Optional.of(dateCloses.get(0).getDate());
        }
        return Optional.empty();
    }

    private Optional<DayMovingAverage> getLatest200dma(String ticker) throws InvalidTickerException {
        Prices prices = dataSource.getClosePrices(ticker, NUM_OF_DAYS_CALCULATING_200DMA);
        if (prices == null || prices.getDateCloses() == null || prices.getDateCloses().size() != NUM_OF_DAYS_CALCULATING_200DMA) {
            return Optional.empty();
        }
        DayMovingAverage dma = calculateDayMovingAverage(prices);
        dma.setStartDate(prices.getDateCloses().get(0).getDate());
        return Optional.of(dma);
    }

    /**
     * gets a list of 200 day moving average prices of the provided tickers from startData.
     * @param tickerSymbols list of ticker to calculate 200 day moving average prices for.
     * @param startDate the beginning date from which day moving average prices are calculated.
     * @return DayMovingAverageList
     */
    public DayMovingAverageList get200DMAList(List<String> tickerSymbols, LocalDate startDate) {
        DayMovingAverageList dmaList = new DayMovingAverageList(startDate);
        for (String tickerSymbol : tickerSymbols) {
            if ("".equals(tickerSymbol)) {
                continue;
            }

            Optional<DayMovingAverage> optDayMovingAverage = Optional.empty();
            try {
                optDayMovingAverage = this.get200DMA(tickerSymbol, startDate);
                if (!optDayMovingAverage.isPresent()) {
                    optDayMovingAverage = this.getLatest200dma(tickerSymbol);
                }
                if (!optDayMovingAverage.isPresent()) {
                    optDayMovingAverage = Optional.of(new DayMovingAverage(tickerSymbol, null));
                    optDayMovingAverage.get().setErrorMessage("There is not enough data for 200 day moving average calculation");
                }
            } catch (InvalidTickerException ex) {
                optDayMovingAverage = Optional.of(new DayMovingAverage(tickerSymbol, null));
                optDayMovingAverage.get().setErrorMessage("Invalid Ticker.");
            }
            dmaList.getDayMovingAverages().add(optDayMovingAverage.get());
        }
        return dmaList;
    }
}