package assignment.service;

import assignment.datasource.DataHolder;
import assignment.model.DateClose;
import assignment.model.DayMovingAverage;
import assignment.model.DayMovingAverageList;
import assignment.model.Prices;
import assignment.datasource.QuandlDataSource;
import assignment.datasource.Response;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.client.ExpectedCount.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


/**
 * Created by xuan on 11/1/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClosePriceServiceTest {

    private ClosePriceService closePriceService;

    @Mock
    private DataHolder dataHolder;

    private Response response;

    private QuandlDataSource quandlDataSource;

    private MockRestServiceServer server;

    private Prices fbPrices;

    private Prices msftPrices;

    private Prices newAssetPrices;

    private LocalDate startDate;

    @Before
    public void init() throws QuandlDataSource.InvalidTicker {
        quandlDataSource = new QuandlDataSource(dataHolder);
        closePriceService = new ClosePriceService(quandlDataSource);
        BigDecimal startPrice = BigDecimal.ZERO;
        BigDecimal delta = BigDecimal.valueOf(25).divide(BigDecimal.valueOf(100));//delta is 0.25
        this.startDate = LocalDate.of(2016, Month.JANUARY, 01);


        List<DateClose> fbDateCloses = new ArrayList<>();
        for(int index = 0; index < 300; ++index){
            fbDateCloses.add(new DateClose(startDate.plusDays(index * 2), startPrice.add(delta.multiply(BigDecimal.valueOf(index)))));
        }
        fbPrices = new Prices("FB", fbDateCloses);
        when(this.dataHolder.getDataSet(fbPrices.getTicker())).thenAnswer(invocation -> {
            return wrap(this.fbPrices);
        });

        List<DateClose> msftDateCloses = new ArrayList<>();
        for(int index = 0; index < 300; ++index){
            msftDateCloses.add(new DateClose(startDate.plusDays(index * 2), startPrice.add(delta.multiply(BigDecimal.valueOf(index)))));
        }
        msftPrices = new Prices("MSFT", msftDateCloses);
        when(this.dataHolder.getDataSet(this.msftPrices.getTicker())).thenAnswer(invocation -> {
            return wrap(msftPrices);
        });


        List<DateClose> newAssetDateCloses = new ArrayList<>();
        for(int index = 0; index < 199; ++index){
            newAssetDateCloses.add(new DateClose(startDate.plusDays(index * 2), startPrice.add(delta.multiply(BigDecimal.valueOf(index)))));
        }
        newAssetPrices = new Prices("NEW", newAssetDateCloses);
        when(dataHolder.getDataSet(this.newAssetPrices.getTicker())).then(invocation ->{
            return wrap(newAssetPrices);
        });
    }

    private Object wrap(Prices prices) {
        return new Prices(prices.getTicker(), prices.getDateCloses());
    }

    @Test
    public void testGetClosePricesWithStartDateAndEndDateMatch() throws ParseException, QuandlDataSource.InvalidTicker {
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
    public void testGetClosePricesWithNoStartDateAndEndDateMatch() throws ParseException, QuandlDataSource.InvalidTicker {
        String ticker = fbPrices.getTicker();
        LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 2);
        LocalDate endDate = LocalDate.of(2016, Month.JANUARY, 4);

        Prices prices = closePriceService.getClosePrices(ticker, startDate, endDate);

        assertEquals(ticker, prices.getTicker());
        assertEquals(1, prices.getDateCloses().size());
        assertEquals(LocalDate.of(2016, Month.JANUARY, 3), prices.getDateCloses().get(0).getDate());
        assertEquals(BigDecimal.valueOf(25).divide(BigDecimal.valueOf(100)), prices.getDateCloses().get(0).getPrice());

    }

    @Test(expected = QuandlDataSource.InvalidTicker.class)
    public void testGetClosePricesWithInvalidTicker() throws ParseException, QuandlDataSource.InvalidTicker {
        LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);
        LocalDate endDate = LocalDate.of(2016, Month.JANUARY, 1);
        String dataSet = "INVALID";
        when(dataHolder.getDataSet("INVALID")).thenThrow(QuandlDataSource.InvalidTicker.class);
        closePriceService.getClosePrices(dataSet, startDate, endDate);
    }


    @Test
    public void testGet200dmaWithAvailableData() throws QuandlDataSource.InvalidTicker {
        String ticker = fbPrices.getTicker();
        LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);

        DayMovingAverage dayMovingAverage = closePriceService.get200DMA(ticker, startDate);

        assertEquals(ticker, dayMovingAverage.getTicker());
        assertEquals("24.875", dayMovingAverage.getAvg().toPlainString());
    }

    @Test
    public void testGet200dmaWithNotAvailableData() throws QuandlDataSource.InvalidTicker {
        String ticker = fbPrices.getTicker();
        LocalDate startDate = fbPrices.getDateCloses().get(fbPrices.getDateCloses().size() - 199).getDate();

        DayMovingAverage dayMovingAverage = closePriceService.get200DMA(ticker, startDate);

        assertNull(dayMovingAverage);
    }

    @Test
    public void testGetFirstStartDate() throws QuandlDataSource.InvalidTicker {
        String ticker = fbPrices.getTicker();
        LocalDate expectedFirstStartDate = fbPrices.getDateCloses().get(fbPrices.getDateCloses().size() - 200).getDate();

        LocalDate firstStartDate = closePriceService.getFirstStartDateHaving200DMA(ticker);

        assertEquals(expectedFirstStartDate, firstStartDate);
    }

    @Test
    public void testGetFirstStartDateForNewAsset() throws QuandlDataSource.InvalidTicker {
        String ticker = newAssetPrices.getTicker();

        LocalDate firstStartDate = closePriceService.getFirstStartDateHaving200DMA(ticker);

        assertNull(firstStartDate);
    }
    @Test
    public void testGetDayMovingAverageList() {
        String fbTicker = fbPrices.getTicker();
        String msftTicker = msftPrices.getTicker();

        DayMovingAverageList dayMovingAverageList = closePriceService.get200DMAList(Arrays.asList(fbTicker, msftTicker), startDate);

        assertEquals(startDate, dayMovingAverageList.getStartDate());
        assertEquals(2, dayMovingAverageList.getDayMovingAverages().size());
        DayMovingAverage fb200Dma = dayMovingAverageList.getDayMovingAverages().get(0);
        assertEquals(startDate, fb200Dma.getStartDate());
        assertEquals(fbTicker, fb200Dma.getTicker());
        assertEquals("24.875", fb200Dma.getAvg().toPlainString());
        DayMovingAverage msftDma = dayMovingAverageList.getDayMovingAverages().get(1);
        assertEquals(startDate, msftDma.getStartDate());
        assertEquals(msftTicker, msftDma.getTicker());
        assertEquals("24.875", msftDma.getAvg().toPlainString());
    }

    @Test
    public void testGetDayMovingAverageListWithInvalidTicker() throws QuandlDataSource.InvalidTicker {
        String dataSet = "INVALID";
        when(dataHolder.getDataSet("INVALID")).thenThrow(QuandlDataSource.InvalidTicker.class);
        DayMovingAverageList dmaList = closePriceService.get200DMAList(Arrays.asList("INVALID"), startDate);
        assertEquals(startDate, dmaList.getStartDate());
        assertEquals(1, dmaList.getDayMovingAverages().size());
        DayMovingAverage invalidDma = dmaList.getDayMovingAverages().get(0);
        assertEquals("INVALID", invalidDma.getTicker());
        assertNotNull(invalidDma.getErrorMessage());
        assertNull(invalidDma.getAvg());
    }

    @Test
    public void testGetDayMovingAverageForFirstAvailableStartDate() {
        String fbTicker = fbPrices.getTicker();
        LocalDate startDate = fbPrices.getDateCloses().get(this.fbPrices.getDateCloses().size() - 199).getDate();

        DayMovingAverageList dmaList = closePriceService.get200DMAList(Arrays.asList(fbTicker),startDate);

        assertEquals(1, dmaList.getDayMovingAverages().size());
        DayMovingAverage fbDma = dmaList.getDayMovingAverages().get(0);
        LocalDate firstStartDate = fbPrices.getDateCloses().get(fbPrices.getDateCloses().size() - 200).getDate();
        assertEquals(firstStartDate, fbDma.getStartDate());
        assertNotNull(fbDma.getAvg());
    }

    @Test
    public void testGetDayMovingAverageForNewAsset() {
        String newAssetTicker = newAssetPrices.getTicker();

        DayMovingAverageList dmaList = closePriceService.get200DMAList(Arrays.asList(newAssetTicker),LocalDate.now());

        assertEquals(1, dmaList.getDayMovingAverages().size());
        DayMovingAverage newAssetDma = dmaList.getDayMovingAverages().get(0);
        assertNull(newAssetDma.getStartDate());
        assertNull(newAssetDma.getAvg());
        assertNotNull(newAssetDma.getErrorMessage());
    }
}
