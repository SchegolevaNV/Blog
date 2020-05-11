package main.repositories;

import main.model.Post;
import main.model.PostVote;
import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Integer>
{
    PostVote findByPostAndUser(Post post, User user);

    @Modifying
    @Query("DELETE from PostVote v WHERE v.id = ?1")
    void deleteById(int id);
}
