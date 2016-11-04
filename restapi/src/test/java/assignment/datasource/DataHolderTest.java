package assignment.datasource;

import assignment.model.DateClose;
import assignment.model.Prices;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Created by xuan on 11/4/2016.
 */
@RunWith(JUnit4.class)
public class DataHolderTest {

    private RestTemplate restTemplate;

    private MockRestServiceServer server;

    private DataHolder dataHolder;

    @Before
    public void init(){
        this.restTemplate = new RestTemplate();
        this.server = MockRestServiceServer.bindTo(restTemplate).build();
        this.dataHolder = new DataHolder(restTemplate);
    }

    @Test
    public void testGetDataSetNormally() throws QuandlDataSource.InvalidTicker {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.ETAG, "W/\"620779fe7c51b507e53324df535dcf96\"");
        this.server.expect(manyTimes(), requestTo("https://www.quandl.com/api/v3/datasets/WIKI/FB.json?column_index=4&order=asc"))
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
                        "\"data\" : [[\"2016-10-30\", 129.99],[\"2016-10-31\", 130.99]]," +
                        "\"collapse\" : null," +
                        "\"order\" : null," +
                        "\"database_id\" : 4922" +
                        "}" +
                        "}", MediaType.APPLICATION_JSON).headers(httpHeaders));

        Prices prices = this.dataHolder.getDataSet("FB");

        this.server.verify();
        assertEquals("FB", prices.getTicker());
        assertEquals(2, prices.getDateCloses().size());
        DateClose firstDateClose = prices.getDateCloses().get(0);
        assertEquals(LocalDate.of(2016, Month.OCTOBER, 30), firstDateClose.getDate());
        assertEquals(BigDecimal.valueOf(12999).divide(BigDecimal.valueOf(100)), firstDateClose.getPrice());
        DateClose secondDateClose = prices.getDateCloses().get(1);
        assertEquals(LocalDate.of(2016, Month.OCTOBER, 31), secondDateClose.getDate());
        assertEquals(BigDecimal.valueOf(13099).divide(BigDecimal.valueOf(100)), secondDateClose.getPrice());
    }

    @Test(expected = QuandlDataSource.InvalidTicker.class)
    public void testGetDateSetWithInvalidTicker() throws QuandlDataSource.InvalidTicker {
        this.server.expect(manyTimes(), requestTo("https://www.quandl.com/api/v3/datasets/WIKI/INVALID.json?column_index=4&order=asc"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{" +
                                "\"quandl_error\" : {" +
                                "\"code\" : \"QECx02\"," +
                                "\"message\" : \"You have submitted an incorrect Quandl code. Please check your Quandl codes and try again.\"" +
                                "}" +
                                "}"));

        this.dataHolder.getDataSet("INVALID");
    }
}
