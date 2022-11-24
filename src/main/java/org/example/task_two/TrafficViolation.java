package org.example.task_two;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

// Setters are not mandatory since Jackson uses reflection to access the fields

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrafficViolation {

    // Not needed since we only need the type of violation and fine amount
    // Fewer fields - faster performance

    /*@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-dd-MM HH:mm:ss")
    private LocalDateTime date_time;
    private String first_name;
    private String last_name;*/

    @JsonProperty("type")
    private ViolationType type;
    private BigDecimal fine_amount;

    /*public LocalDateTime getDate_time() {
        return date_time;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }*/

    public ViolationType getType() {
        return type;
    }

    public BigDecimal getFine_amount() {
        return fine_amount;
    }
}

