package assignment;

import assignment.model.Prices;
import assignment.controller.serializer.PricesSerializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;

/**
 * Created by xuan on 11/1/2016.
 */
@SpringBootApplication
public class Application {

    public static void main( String[] args){
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public PropertyNamingStrategy propertyNamingStrategy()
    {
        return PropertyNamingStrategy.UPPER_CAMEL_CASE;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder(PricesSerializer pricesSerializer) {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.propertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
        builder.serializerByType(Prices.class, pricesSerializer);
        return builder;
    }
}
