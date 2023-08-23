package com.imss.sivimss.valesalida.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
public class ActualizarMultipleRequest {

    @JsonProperty
    private ArrayList<String> updates;

}
