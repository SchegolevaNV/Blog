package main.repositories;

import main.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CaptchaCodeRepository extends JpaRepository<CaptchaCode, Integer> {

    CaptchaCode findByCode(String code);
    List<CaptchaCode> findByTimeBefore(LocalDateTime time);

    @Modifying
    @Query("DELETE from CaptchaCode cc WHERE cc.id = ?1")
    void deleteById(int id);
}
