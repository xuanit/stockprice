package assignment.model;

/**
 * Created by xuan on 11/6/2016.
 */
public class InvalidTickerException extends Exception {

    public InvalidTickerException(){super();}

    public InvalidTickerException(String message){
        super(message);
    }
}
