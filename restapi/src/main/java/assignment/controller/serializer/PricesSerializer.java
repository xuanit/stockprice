package assignment.controller.serializer;

import assignment.controller.CloseDate;
import assignment.model.Prices;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Created by xuan on 11/1/2016.
 */
@Component
public class PricesSerializer extends JsonSerializer<Prices> {

    @Override
    public void serialize(Prices value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeObjectField("Ticker", value.getTicker());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        for(CloseDate closeDate : value.getCloseDates()) {
            gen.writeFieldName("DateClose");
            gen.writeStartArray();
            gen.writeString(closeDate.getDate().toString());
            gen.writeObject(decimalFormat.format(closeDate.getPrice()));
            gen.writeEndArray();
        }
        gen.writeEndObject();
    }

}
