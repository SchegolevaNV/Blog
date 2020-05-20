package main.api.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiRequestBody
{
    private int post_id;
    private Integer parent_id;
    private String text;
    private String decision;
}
