package assignment.datasource.quandl;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by xuan on 11/2/2016.
 */
public class Error {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
