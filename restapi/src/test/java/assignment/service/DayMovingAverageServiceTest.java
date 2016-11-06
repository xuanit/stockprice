package assignment.service;

import assignment.datasource.DataHolder;
import assignment.datasource.DataSource;
import assignment.datasource.DefaultDataSource;
import assignment.datasource.quandl.Response;
import assignment.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DayMovingAverageServiceTest {

    @Mock
    private DataHolder dataHolder;

    private Response response;

    private DataSource quandlDataSource;

    private Prices fbPrices;

    private Prices msftPrices;

    private Prices newAssetPrices;

    private LocalDate startDate;

    private DayMovingAverageService dayMovingAverageService;

    @Before
    public void init() throws InvalidTickerException {
        quandlDataSource = new DefaultDataSource(dataHolder);
        dayMovingAverageService = new DayMovingAverageService(quandlDataSource);
        BigDecimal startPrice = BigDecimal.ZERO;
        BigDecimal delta = BigDecimal.valueOf(25).divide(BigDecimal.valueOf(100));//delta is 0.25
        this.startDate = LocalDate.of(2016, Month.JANUARY, 01);


        List<DateClose> fbDateCloses = new ArrayList<>();
        for(int index = 0; index < 300; ++index){
            fbDateCloses.add(new DateClose(startDate.plusDays(index * 2), startPrice.add(delta.multiply(BigDecimal.valueOf(index)))));
        }
        fbPrices = new Prices("FB", fbDateCloses, "etag");
        when(this.dataHolder.getDataSet(fbPrices.getTicker())).thenAnswer(invocation -> {
            return wrap(this.fbPrices);
        });

        List<DateClose> msftDateCloses = new ArrayList<>();
        for(int index = 0; index < 300; ++index){
            msftDateCloses.add(new DateClose(startDate.plusDays(index * 2), startPrice.add(delta.multiply(BigDecimal.valueOf(index)))));
        }
        msftPrices = new Prices("MSFT", msftDateCloses, "etag");
        when(this.dataHolder.getDataSet(this.msftPrices.getTicker())).thenAnswer(invocation -> {
            return wrap(msftPrices);
        });


        List<DateClose> newAssetDateCloses = new ArrayList<>();
        for(int index = 0; index < 199; ++index){
            newAssetDateCloses.add(new DateClose(startDate.plusDays(index * 2), startPrice.add(delta.multiply(BigDecimal.valueOf(index)))));
        }
        newAssetPrices = new Prices("NEW", newAssetDateCloses, "etag");
        when(dataHolder.getDataSet(this.newAssetPrices.getTicker())).then(invocation ->{
            return wrap(newAssetPrices);
        });
    }

    private Object wrap(Prices prices) {
        return new Prices(prices.getTicker(), prices.getDateCloses(), prices.getEtag());
    }

    @Test
    public void testGet200dmaWithAvailableData() throws InvalidTickerException {
        String ticker = fbPrices.getTicker();
        LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 1);

        DayMovingAverage dayMovingAverage = dayMovingAverageService.get200DMA(ticker, startDate).get();

        Assert.assertEquals(ticker, dayMovingAverage.getTicker());
        Assert.assertEquals("24.875", dayMovingAverage.getAvg().toPlainString());
    }

    @Test
    public void testGet200dmaWithAvailableDataAndStartDateNoneMatch() throws InvalidTickerException {
        String ticker = fbPrices.getTicker();
        LocalDate startDate = LocalDate.of(2016, Month.JANUARY, 2);

        DayMovingAverage dayMovingAverage = dayMovingAverageService.get200DMA(ticker, startDate).get();

        Assert.assertEquals(ticker, dayMovingAverage.getTicker());
        Assert.assertEquals("25.125", dayMovingAverage.getAvg().toPlainString());
    }

    @Test
    public void testGet200dmaWithNotAvailableData() throws InvalidTickerException {
        String ticker = fbPrices.getTicker();
        LocalDate startDate = fbPrices.getDateCloses().get(fbPrices.getDateCloses().size() - 199).getDate();

        Optional<DayMovingAverage> optDayMovingAverage = dayMovingAverageService.get200DMA(ticker, startDate);

        Assert.assertFalse(optDayMovingAverage.isPresent());
    }

    @Test
    public void testGet200dmaWithStartDateOverLastCloseDate() throws InvalidTickerException {
        String ticker = fbPrices.getTicker();
        LocalDate startDate = fbPrices.getDateCloses().get(fbPrices.getDateCloses().size() - 1).getDate();

        Optional<DayMovingAverage> optDayMovingAverage = dayMovingAverageService.get200DMA(ticker, startDate.plusDays(1));

        Assert.assertFalse(optDayMovingAverage.isPresent());
    }

    @Test
    public void testGetFirstStartDate() throws InvalidTickerException {
        String ticker = fbPrices.getTicker();
        LocalDate expectedFirstStartDate = fbPrices.getDateCloses().get(fbPrices.getDateCloses().size() - 200).getDate();

        LocalDate firstStartDate = dayMovingAverageService.getFirstStartDateHaving200DMA(ticker).get();

        Assert.assertEquals(expectedFirstStartDate, firstStartDate);
    }

    @Test
    public void testGetFirstStartDateForNewAsset() throws InvalidTickerException {
        String ticker = newAssetPrices.getTicker();

        Optional<LocalDate> optFirstStartDate = dayMovingAverageService.getFirstStartDateHaving200DMA(ticker);

        Assert.assertFalse(optFirstStartDate.isPresent());
    }

    @Test
    public void testGetDayMovingAverageList() {
        String fbTicker = fbPrices.getTicker();
        String msftTicker = msftPrices.getTicker();

        DayMovingAverageList dayMovingAverageList = dayMovingAverageService.get200DMAList(Arrays.asList(fbTicker, msftTicker), startDate);

        Assert.assertEquals(startDate, dayMovingAverageList.getStartDate());
        Assert.assertEquals(2, dayMovingAverageList.getDayMovingAverages().size());
        DayMovingAverage fb200Dma = dayMovingAverageList.getDayMovingAverages().get(0);
        Assert.assertEquals(startDate, fb200Dma.getStartDate());
        Assert.assertEquals(fbTicker, fb200Dma.getTicker());
        Assert.assertEquals("24.875", fb200Dma.getAvg().toPlainString());
        DayMovingAverage msftDma = dayMovingAverageList.getDayMovingAverages().get(1);
        Assert.assertEquals(startDate, msftDma.getStartDate());
        Assert.assertEquals(msftTicker, msftDma.getTicker());
        Assert.assertEquals("24.875", msftDma.getAvg().toPlainString());
    }

    @Test
    public void testGetDayMovingAverageListWithInvalidTicker() throws InvalidTickerException {
        String dataSet = "INVALID";
        Mockito.when(dataHolder.getDataSet("INVALID")).thenThrow(InvalidTickerException.class);
        DayMovingAverageList dmaList = dayMovingAverageService.get200DMAList(Arrays.asList("INVALID"), startDate);
        Assert.assertEquals(startDate, dmaList.getStartDate());
        Assert.assertEquals(1, dmaList.getDayMovingAverages().size());
        DayMovingAverage invalidDma = dmaList.getDayMovingAverages().get(0);
        Assert.assertEquals("INVALID", invalidDma.getTicker());
        Assert.assertNotNull(invalidDma.getErrorMessage());
        Assert.assertNull(invalidDma.getAvg());
    }

    @Test
    public void testGetDayMovingAverageForFirstAvailableStartDate() {
        String fbTicker = fbPrices.getTicker();
        LocalDate startDate = fbPrices.getDateCloses().get(fbPrices.getDateCloses().size() - 199).getDate();

        DayMovingAverageList dmaList = dayMovingAverageService.get200DMAList(Arrays.asList(fbTicker), startDate);

        Assert.assertEquals(1, dmaList.getDayMovingAverages().size());
        DayMovingAverage fbDma = dmaList.getDayMovingAverages().get(0);
        LocalDate firstStartDate = fbPrices.getDateCloses().get(fbPrices.getDateCloses().size() - 200).getDate();
        Assert.assertEquals(firstStartDate, fbDma.getStartDate());
        Assert.assertNotNull(fbDma.getAvg());
    }

    @Test
    public void testGetDayMovingAverageForNewAsset() {
        String newAssetTicker = newAssetPrices.getTicker();

        DayMovingAverageList dmaList = dayMovingAverageService.get200DMAList(Arrays.asList(newAssetTicker), LocalDate.now());

        Assert.assertEquals(1, dmaList.getDayMovingAverages().size());
        DayMovingAverage newAssetDma = dmaList.getDayMovingAverages().get(0);
        Assert.assertNull(newAssetDma.getStartDate());
        Assert.assertNull(newAssetDma.getAvg());
        Assert.assertNotNull(newAssetDma.getErrorMessage());
    }
}