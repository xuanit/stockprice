package assignment.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by xuan on 11/1/2016.
 */
public class Prices {

    private final String ticker;

    private final List<DateClose> dateCloses;

    public Prices(String ticker, List<DateClose> dateCloses) {
        this.ticker = ticker;
        this.dateCloses = dateCloses;
    }

    public String getTicker() {
        return ticker;
    }

    public List<DateClose> getDateCloses() {
        return Collections.unmodifiableList(this.dateCloses);
    }
}
