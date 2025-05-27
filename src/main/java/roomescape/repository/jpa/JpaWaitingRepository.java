package roomescape.repository.jpa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.entity.Waiting;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByMemberId(Long memberId);

    long countByDateAndThemeIdAndTimeIdAndCreatedAtLessThan(LocalDate date, Long themeId, Long timeId,
        LocalDateTime createdAt);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId,
        Long memberId);
}
