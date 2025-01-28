package main.api.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiRequestBody(@JsonProperty("post_id") int postId,
                             @JsonProperty("parent_id") Integer parentId,
                             String text,
                             String decision,
                             Long timestamp,
                             Byte active,
                             String title,
                             List<String> tags,
                             String email,
                             String photo,
                             Integer removePhoto,
                             String password,
                             String name) {
}
