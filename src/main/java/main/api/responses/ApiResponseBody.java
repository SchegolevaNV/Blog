package main.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import main.api.responses.bodies.ErrorsBody;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseBody
{
    private Integer id;
    private Boolean result;
    private ErrorsBody errors;
}
