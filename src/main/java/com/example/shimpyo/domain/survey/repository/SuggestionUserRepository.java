package com.example.shimpyo.domain.survey.repository;

import com.example.shimpyo.domain.survey.entity.SuggestionUser;
import com.example.shimpyo.domain.user.dto.LikedCourseResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;

@Repository
public interface SuggestionUserRepository extends JpaRepository<SuggestionUser, Long> {
    @Query("""
                select new com.example.shimpyo.domain.user.dto.LikedCourseResponseDto(
                    s.id,
                    s.title,
                    s.wellnessType,
                    s.token,
                    (
                        select st.tourist.image
                        from SuggestionTourist st
                        where st.suggestion = s
                        order by st.id asc
                        limit 1
                    )
                )
                from SuggestionUser us
                join us.suggestion s
                join s.suggestionTourists st
                join st.tourist t
                where us.user.id = :userId
            """)
    List<LikedCourseResponseDto> findLikedCoursesByUserId(@Param("userId") Long userId);
}
