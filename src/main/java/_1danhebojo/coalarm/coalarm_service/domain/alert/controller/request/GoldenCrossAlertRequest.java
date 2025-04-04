package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoldenCrossAlertRequest extends BaseAlertRequest {
    @Null
    private Long goldenCrossId;

    @JsonProperty("short_ma")
    private Long shortMa;

    @JsonProperty("long_ma")
    private Long longMa;
}