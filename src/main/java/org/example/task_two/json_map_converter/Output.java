package org.example.task_two.json_map_converter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.example.task_two.ViolationType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * Class used to set a resulting map for writing an output
  */
@JacksonXmlRootElement(localName = "moving_violations")
@JsonSerialize(using = OutputJsonSerializer.class)
public class Output {

    private Map<ViolationType, BigDecimal> entry;

    public Map<ViolationType, BigDecimal> getEntry() {
        return Collections.unmodifiableMap(entry);
    }

    public void setEntry(Map<ViolationType, BigDecimal> entry) {
        this.entry = entry;
    }
}
