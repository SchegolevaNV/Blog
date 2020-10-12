package main.repositories;

import main.model.Post;
import main.model.TagToPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TagToPostRepository extends JpaRepository<TagToPost, Integer>
{
    @Modifying
    @Query("DELETE from TagToPost t WHERE t.post = ?1")
    void deleteByPost (Post post);
}
