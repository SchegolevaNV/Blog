package main.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tags")
public class Tag
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private String name;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "tag2post",
            joinColumns = {@JoinColumn(name = "tag_id")},
            inverseJoinColumns = {@JoinColumn(name = "post_id")})
    private List<Post> tagsPosts;

    @OneToMany(mappedBy = "tag")
    private List<TagToPost> tagToPosts;

    public Tag(@NotNull String name) {
        this.name = name;
    }
}
