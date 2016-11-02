package assignment.controller;

import assignment.model.Prices;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuan on 11/1/2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClosePriceResponse extends ResourceSupport {

    private List<String> errors;

    private Prices prices;

    public Prices getPrices() {
        return prices;
    }

    public void setPrices(Prices prices) {
        this.prices = prices;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        if(errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
    }
}
