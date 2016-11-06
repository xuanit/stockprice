package assignment.service;

import assignment.datasource.*;
import assignment.datasource.quandl.Response;
import assignment.model.*;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;


/**
 * Created by xuan on 11/1/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClosePriceServiceTest {

    private ClosePriceService closePriceService;

    @Mock
    private DataHolder dataHolder;

    private Response response;

    private DataSource dataSource;

    private Prices fbPrices;

    private LocalDate startDate;

    @Before
    public void init() throws InvalidTickerException {
        dataSource = new DefaultDataSource(dataHolder);
        closePriceService = new ClosePriceService(dataSource);
        BigDecimal startPrice = BigDecimal.ZERO;
        BigDecimal delta = BigDecimal.valueOf(25).divide(BigDecimal.valueOf(100));//delta is 0.25
        this.startDate = LocalDate.of(2016, Month.JANUARY, 01);


        List<DateClose> fbDateCloses = new ArrayList<>();
        for(int index = 0; index < 300; ++index){
            fbDateCloses.add(new DateClose(startDate.plusDays(index * 2), startPrice.add(delta.multiply(BigDecimal.valueOf(index)))));
        }
        fbPrices = new Prices("FB", fbDateCloses, "etag");
        when(this.dataHolder.getDataSet(fbPrices.getTicker())).thenAnswer(invocation -> {
            return new Prices(this.fbPrices.getTicker(), this.fbPrices.getDateCloses(), "etag");
        });
    }

    @Test
    public void testGetClosePricesWithStartDateAndEndDateMatch() throws ParseException, InvalidTickerException {
        String ticker = fbPrices.getTicker();
        LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        LocalDate endDate = LocalDate.of(2016, Month.JANUARY, 3);

        Prices prices = closePriceService.getClosePrices(ticker, startDate, endDate);

        assertEquals(ticker, prices.getTicker());
        assertEquals(2, prices.getDateCloses().size());
        assertEquals(LocalDate.of(2016, Month.JANUARY, 1), prices.getDateCloses().get(0).getDate());
        assertEquals((Object)BigDecimal.ZERO.setScale(2), prices.getDateCloses().get(0).getPrice());
        assertEquals(LocalDate.of(2016, Month.JANUARY, 3), prices.getDateCloses().get(1).getDate());
        assertEquals(BigDecimal.valueOf(25).divide(BigDecimal.valueOf(100)), prices.getDateCloses().get(1).getPrice());
    }

    @Test
    public void testGetClosePricesWithNoStartDateAndEndDateMatch() throws ParseException, InvalidTickerException {
        String ticker = fbPrices.getTicker();
        LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 2);
        LocalDate endDate = LocalDate.of(2016, Month.JANUARY, 4);

        Prices prices = closePriceService.getClosePrices(ticker, startDate, endDate);

        assertEquals(ticker, prices.getTicker());
        assertEquals(1, prices.getDateCloses().size());
        assertEquals(LocalDate.of(2016, Month.JANUARY, 3), prices.getDateCloses().get(0).getDate());
        assertEquals(BigDecimal.valueOf(25).divide(BigDecimal.valueOf(100)), prices.getDateCloses().get(0).getPrice());

    }

    @Test(expected = InvalidTickerException.class)
    public void testGetClosePricesWithInvalidTicker() throws ParseException, InvalidTickerException {
        LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        LocalDate endDate = LocalDate.of(2016, Month.JANUARY, 1);
        String dataSet = "INVALID";
        when(dataHolder.getDataSet("INVALID")).thenThrow(InvalidTickerException.class);
        closePriceService.getClosePrices(dataSet, startDate, endDate);
    }

    public void testGetClosePriceWithStartDateInFuture() throws InvalidTickerException {
        LocalDate startDate = fbPrices.getDateCloses().get(fbPrices.getDateCloses().size() - 1).getDate().plusDays(1);
        Prices prices = closePriceService.getClosePrices(fbPrices.getTicker(), startDate, startDate.plusDays(1));
        assertEquals(fbPrices.getTicker(), prices.getTicker());
        assertEquals(0, prices.getDateCloses().size());
    }

    public void testGetClosePricesWithEndDateInPast() throws InvalidTickerException {
        LocalDate endDate = fbPrices.getDateCloses().get(0).getDate().minusDays(1);
        Prices prices = closePriceService.getClosePrices(fbPrices.getTicker(), endDate.minusDays(1), endDate);
        assertEquals(fbPrices.getTicker(), prices.getTicker());
        assertEquals(0, prices.getDateCloses().size());
    }

}
