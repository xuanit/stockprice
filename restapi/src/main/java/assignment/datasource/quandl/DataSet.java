package assignment.datasource.quandl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by xuan on 11/1/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSet {

    @JsonProperty("dataset_code")
    private String dataSetCode;

    @JsonProperty("database_code")
    private String databaseCode;

    private Long id;

    private String name;

    @JsonProperty("data")
    private List<List<String>>  data;

    public String getDataSetCode() {
        return dataSetCode;
    }

    public void setDataSetCode(String dataSetCode) {
        this.dataSetCode = dataSetCode;
    }

    public String getDatabaseCode() {
        return databaseCode;
    }

    public void setDatabaseCode(String databaseCode) {
        this.databaseCode = databaseCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }
}
