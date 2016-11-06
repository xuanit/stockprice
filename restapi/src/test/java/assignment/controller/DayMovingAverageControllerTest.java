package assignment.controller;

import assignment.model.DayMovingAverage;
import assignment.model.DayMovingAverageList;
import assignment.model.InvalidTickerException;
import assignment.service.DayMovingAverageService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Created by xuan on 11/3/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cache.type=none")
@AutoConfigureMockMvc
public class DayMovingAverageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DayMovingAverageService dayMovingAverageService;

    @Test
    public void testGetDayMovingAverageList() throws Exception {
        LocalDate startDate = LocalDate.of(2016, Month.OCTOBER, 31);
        DayMovingAverageList dayMovingAverageList = new DayMovingAverageList(startDate);
        dayMovingAverageList.getDayMovingAverages().add(new DayMovingAverage("FB", BigDecimal.valueOf(9).divide(BigDecimal.TEN), startDate));
        when(this.dayMovingAverageService.get200DMAList(Arrays.asList("FB"), startDate))
                .thenReturn(dayMovingAverageList);
        mockMvc.perform(get("/api/v2/200dma?startDate=2016-10-31&ticker=FB"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"200dmas\":{\"StartDate\":\"2016-10-31\",\"Data\":[{\"Ticker\":\"FB\",\"Avg\":\"0.9\",\"StartDate\":\"2016-10-31\"}]}}"));
    }

    @Test
    public void testGetDayMovingAverageListWithTwoTicker() throws Exception {
        LocalDate startDate = LocalDate.of(2016, Month.OCTOBER, 31);
        DayMovingAverageList dayMovingAverageList = new DayMovingAverageList(startDate);
        dayMovingAverageList.getDayMovingAverages().add(new DayMovingAverage("FB", BigDecimal.valueOf(9).divide(BigDecimal.TEN), startDate));
        dayMovingAverageList.getDayMovingAverages().add(new DayMovingAverage("MSFT", BigDecimal.valueOf(9).divide(BigDecimal.TEN), startDate));
        when(this.dayMovingAverageService.get200DMAList(Arrays.asList("FB", "MSFT"), startDate))
                .thenReturn(dayMovingAverageList);
        mockMvc.perform(get("/api/v2/200dma?startDate=2016-10-31&ticker=FB,MSFT"))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"200dmas\":{\"StartDate\":\"2016-10-31\",\"Data\":[{\"Ticker\":\"FB\",\"Avg\":\"0.9\",\"StartDate\":\"2016-10-31\"}"
                        + ",{\"Ticker\":\"MSFT\",\"Avg\":\"0.9\",\"StartDate\":\"2016-10-31\"}]}}"));
    }

    @Test
    public void testGetDayMovingAverageListWithInvalidStartDate() throws Exception {
        mockMvc.perform(get("/api/v2/200dma?startDate=2016-10-34&ticker=FB,MSFT"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGet200DMANormally() throws Exception {
        LocalDate startDate = LocalDate.of(2016, Month.OCTOBER, 31);
        String ticker = "FB";
        BigDecimal avg = BigDecimal.valueOf(99).divide(BigDecimal.TEN);
        DayMovingAverage dayMovingAverage = new DayMovingAverage(ticker, avg);
        dayMovingAverage.setStartDate(startDate);
        when(this.dayMovingAverageService.get200DMA(ticker, startDate))
                .thenReturn(Optional.of(dayMovingAverage));
        this.mockMvc.perform(get("/api/v2/FB/200dma?startDate=2016-10-31"))
                .andExpect(status().isOk())
                .andExpect(content().string("{" +
                        "\"200dma\":{" +
                        "\"Ticker\":\"FB\"," +
                        "\"Avg\":\"9.9\"" +
                        "}" +
                        "}"));
    }

    @Test
    public void testGet200DMAInvalidStartDate() throws Exception {
        this.mockMvc.perform(get("/api/v2/FB/200dma?startDate=2016-10-33"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGet200DMAInvalidTicker() throws Exception {
        LocalDate startDate = LocalDate.of(2016, Month.OCTOBER, 31);
        when(this.dayMovingAverageService.get200DMA("INVALID", startDate))
                .thenThrow(new InvalidTickerException());
        this.mockMvc.perform(get("/api/v2/INVALID/200dma?startDate=2016-10-31"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGet200DMAHavingSuggestedStartDateInErrorMessage() throws Exception {
        LocalDate startDate = LocalDate.of(2016, Month.OCTOBER, 31);
        LocalDate firstStartDate = LocalDate.of(2016, Month.OCTOBER, 30);
        when(this.dayMovingAverageService.get200DMA("FB", startDate))
                .thenReturn(Optional.empty());
        when((this.dayMovingAverageService.getFirstStartDateHaving200DMA("FB")))
                .thenReturn(Optional.of(firstStartDate));
        this.mockMvc.perform(get("/api/v2/FB/200dma?startDate=2016-10-31"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(Matchers.containsString(firstStartDate.toString())));
    }
}
