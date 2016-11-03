package assignment.controller;

import assignment.model.DayMovingAverage;
import assignment.model.DayMovingAverageList;
import assignment.service.ClosePriceService;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Created by xuan on 11/3/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DayMovingAverageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClosePriceService service;

    @Test
    public void testGetDayMovingAverageList() throws Exception {
        LocalDate startDate = LocalDate.of(2016, Month.OCTOBER, 31);
        DayMovingAverageList dayMovingAverageList = new DayMovingAverageList(startDate);
        dayMovingAverageList.getDayMovingAverages().add(new DayMovingAverage("FB", BigDecimal.valueOf(9).divide(BigDecimal.TEN), startDate));
        when(this.service.get200DMAList(Arrays.asList("FB"), startDate))
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
        when(this.service.get200DMAList(Arrays.asList("FB", "MSFT"), startDate))
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
}
