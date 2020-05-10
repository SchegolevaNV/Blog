package main.repositories;

import main.model.Post;
import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer>
{
    Post findById (int id);
    List<Post> findByUserOrderByTimeAsc(User user);
}
