package main.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import main.services.bodies.ErrorsBody;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseBody
{
    private Integer id;
    private boolean result;
    private ErrorsBody errors;
}
