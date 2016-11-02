package assignment.controller;

import assignment.datasource.QuandlDataSource;
import assignment.model.Prices;
import assignment.service.ClosePriceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Created by xuan on 11/1/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ClosePriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClosePriceService service;

    @Test
    public void getGetClosePricesShouldReturn200() throws Exception {
        LocalDate startDate = LocalDate.of(2016, Month.OCTOBER, 31);
        LocalDate endDate = LocalDate.of(2016, Month.NOVEMBER, 1);
        Prices prices = new Prices();
        prices.setTicker("FB");
        DecimalFormat bigDecimalFortmat = new DecimalFormat();
        bigDecimalFortmat.setParseBigDecimal(true);
        prices.getCloseDates().add(new CloseDate(LocalDate.of(2016, Month.OCTOBER, 31), (BigDecimal)bigDecimalFortmat.parse("9.99")));
        prices.getCloseDates().add(new CloseDate(LocalDate.of(2016, Month.NOVEMBER, 01), (BigDecimal)bigDecimalFortmat.parse("1.00")));
        when(service.getClosePrices("FB", startDate, endDate)).thenReturn(prices);
        this.mockMvc.perform(get("/api/v2/FB/closePrice?startDate=2016-10-31&endDate=2016-11-01"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"Prices\":{\"Ticker\":\"FB\",\"DateClose\":[\"2016-10-31\",\"9.99\"],\"DateClose\":[\"2016-11-01\",\"1.00\"]}}"));
    }

    @Test
    public void testGetClosePriceWithDateRangeShouldReturn400Error() throws Exception {
        this.mockMvc.perform(get("/api/v2/FB/closePrice?startDate=2016-10-33&endDate=2016-11-01"))
                        .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
        this.mockMvc.perform(get("/api/v2/FB/closePrice?startDate=2016-10-30&endDate=201611-01"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
        this.mockMvc.perform(get("/api/v2/FB/closePrice?startDate=2016-10-30&endDate=2016-10-29"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void testGetClosePriceWithInvalidTickerReturn400Error() throws Exception {
        LocalDate startDate = LocalDate.of(2016, Month.OCTOBER, 31);
        LocalDate endDate = LocalDate.of(2016, Month.NOVEMBER, 1);
        when(service.getClosePrices("INVALIDTICKER", startDate, endDate)).thenThrow(QuandlDataSource.InvalidTicker.class);
        this.mockMvc.perform(get("/api/v2/INVALIDTICKER/closePrice?startDate=2016-10-31&endDate=2016-11-01"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }
}
