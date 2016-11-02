package assignment.service;

import assignment.model.Prices;
import assignment.datasource.QuandlDataSource;
import assignment.datasource.Response;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Month;

import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


/**
 * Created by xuan on 11/1/2016.
 */
public class ClosePriceServiceTest {

    private ClosePriceService closePriceService;

    private RestTemplate restTemplate;

    private Response response;

    private QuandlDataSource quandlDataSource;

    private MockRestServiceServer server;

    @Before
    public void init() {
        this.restTemplate = new RestTemplate();
        this.server = MockRestServiceServer.bindTo(restTemplate).build();
        this.quandlDataSource = new QuandlDataSource(this.restTemplate);
        this.closePriceService = new ClosePriceService(this.quandlDataSource);
    }

    @Test
    public void testGetClosePrices() throws ParseException, QuandlDataSource.InvalidTicker {
        String dataSet = "FB";
        this.server.expect(manyTimes(), requestTo("https://www.quandl.com/api/v3/datasets/WIKI/FB.json?column_index=4&start_date=2016-10-31&end_date=2016-11-01"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{" +
                        "\"dataset\" : {" +
                        "\"id\" : 9775687," +
                        "\"dataset_code\" : \"FB\"," +
                        "\"database_code\" : \"WIKI\"," +
                        "\"name\" : \"Facebook Inc. (FB) Prices, Dividends, Splits and Trading Volume\"," +
                        "\"description\" : \"description\"," +
                        "\"refreshed_at\" : \"2016-10-31T21:47:31.671Z\"," +
                        "\"newest_available_date\" : \"2016-10-31\"," +
                        "\"oldest_available_date\" : \"2012-05-18\"," +
                        "\"column_names\" : [\"Date\", \"Close\"]," +
                        "\"frequency\" : \"daily\"," +
                        "\"type\" : \"Time Series\"," +
                        "\"premium\" : false," +
                        "\"limit\" : null," +
                        "\"transform\" : null," +
                        "\"column_index\" : 4," +
                        "\"start_date\" : \"2016-10-31\"," +
                        "\"end_date\" : \"2016-11-01\"," +
                        "\"data\" : [[\"2016-10-31\", 130.99]]," +
                        "\"collapse\" : null," +
                        "\"order\" : null," +
                        "\"database_id\" : 4922" +
                        "}" +
                        "}", MediaType.APPLICATION_JSON));

        LocalDate startDate = LocalDate.of(2016, Month.OCTOBER, 31);
        LocalDate endDate = LocalDate.of(2016, Month.NOVEMBER, 1);

        Prices prices = this.closePriceService.getClosePrices(dataSet, startDate, endDate);

        server.verify();
        assertEquals(dataSet, prices.getTicker());
        assertEquals(1, prices.getCloseDates().size());
        assertEquals(LocalDate.of(2016, Month.OCTOBER, 31), prices.getCloseDates().get(0).getDate());
        DecimalFormat bigDecimalFormat = new DecimalFormat();
        bigDecimalFormat.setParseBigDecimal(true);
        assertEquals(bigDecimalFormat.parse("130.99"), prices.getCloseDates().get(0).getPrice());

    }

    @Test(expected = QuandlDataSource.InvalidTicker.class)
    public void testGetClosePricesWithInvalidTicker() throws ParseException, QuandlDataSource.InvalidTicker {
        LocalDate startDate = LocalDate.of(2016, Month.OCTOBER, 31);
        LocalDate endDate = LocalDate.of(2016, Month.NOVEMBER, 1);
        String dataSet = "INVALID";
        this.server.expect(manyTimes(), requestTo("https://www.quandl.com/api/v3/datasets/WIKI/INVALID.json?column_index=4&start_date=2016-10-31&end_date=2016-11-01"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{" +
                        "\"quandl_error\" : {" +
                        "\"code\" : \"QECx02\"," +
                        "\"message\" : \"You have submitted an incorrect Quandl code. Please check your Quandl codes and try again.\"" +
                        "}" +
                        "}"));
        this.closePriceService.getClosePrices(dataSet, startDate, endDate);

    }
}
