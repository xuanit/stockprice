package assignment.datasource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by xuan on 11/1/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    @JsonProperty("quandl_error")
    private  Error error;

    @JsonProperty("dataset")
    private DataSet dataset;

    public DataSet getDataset() {
        return dataset;
    }

    public void setDataset(DataSet dataset) {
        this.dataset = dataset;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
