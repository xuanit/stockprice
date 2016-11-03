package assignment.controller;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Created by xuan on 11/2/2016.
 */
@Component
public class StringToLocalDateConverter {

    private static final String DATE_FORMAT_ERROR =  "%s is not recognzied.Please provide YYYY-MM-DD";

    public LocalDate convert(String dateString, String fieldName, BaseResponse response){
        LocalDate date = null;
        try{
            date = LocalDate.parse(dateString);
        }catch (DateTimeParseException ex){
            response.addError(String.format(DATE_FORMAT_ERROR, "startDate"));
        }
        return date;
    }
}
