package main.services.interfaces;

public interface QueryService {

    String postsSelect = "FROM Post p WHERE p.isActive = 1 " +
            "AND p.moderationStatus = 'ACCEPTED'" +
            "AND p.time <= :dateNow";

    String postsCount = "SELECT COUNT(*) FROM Post p WHERE p.isActive = 1 " +
            "AND p.moderationStatus = 'ACCEPTED'" +
            "AND p.time <= :dateNow";
}
