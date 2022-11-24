package org.example.task_two.json_map_converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.example.task_two.ViolationType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Custom serializer that generates output from an input map to xml format string
  */
public class OutputJsonSerializer extends JsonSerializer<Output> {

    @Override
    public void serialize(Output value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        ToXmlGenerator xmlGen = (ToXmlGenerator) gen;
        xmlGen.writeStartObject();
        for (Map.Entry<ViolationType, BigDecimal> entry : value.getEntry().entrySet()) {
            xmlGen.writeObjectFieldStart("mv");
            writeAttributes(xmlGen, String.valueOf(entry.getKey()));
            xmlGen.writeRaw(entry.getValue().toString());
            xmlGen.writeEndObject();
        }
        xmlGen.writeEndObject();
    }

    private void writeAttributes(ToXmlGenerator gen, String key) throws IOException {
        gen.setNextIsAttribute(true);
        gen.writeFieldName("name");
        gen.writeString(key);
        gen.setNextIsAttribute(false);
    }
}