package main.api.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiRequestBody
{
    @JsonProperty("post_id")
    private int postId;

    @JsonProperty("parent_id")
    private Integer parentId;

    private String text;
    private String decision;
    private Long timestamp;
    private Byte active;
    private String title;
    private List<String> tags;
}
