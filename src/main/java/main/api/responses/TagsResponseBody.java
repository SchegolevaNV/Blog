package main.api.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import main.api.responses.bodies.TagsBody;

import java.util.List;

@Data
@AllArgsConstructor
public class TagsResponseBody
{
    List<TagsBody> tags;
}
