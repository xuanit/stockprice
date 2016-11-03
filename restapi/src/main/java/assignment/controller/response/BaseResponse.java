package assignment.controller.response;

import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuan on 11/2/2016.
 */
public class BaseResponse extends ResourceSupport {

    private List<String> errors;

    public void addError(String error) {
        if(errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean hasError(){
        return errors != null && errors.size() > 0;
    }
}
