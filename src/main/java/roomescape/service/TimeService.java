package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.request.TimeRequest;
import roomescape.dto.response.TimeResponse;
import roomescape.entity.ReservationTime;
import roomescape.exception.custom.DuplicatedException;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class TimeService {

    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaReservationRepository reservationRepository;

    public TimeService(JpaReservationTimeRepository reservationTimeRepository,
        JpaReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<TimeResponse> findAllReservationTimes() {
        return reservationTimeRepository.findAll().stream()
            .map(TimeResponse::from)
            .toList();
    }

    public List<TimeResponse> findAllTimesWithBooked(LocalDate date, Long themeId) {
        Set<Long> bookedTimeIds = reservationRepository.findByDateAndThemeId(date, themeId).stream()
            .map(reservation -> reservation.getTime().getId())
            .collect(Collectors.toSet());

        return reservationTimeRepository.findAllByOrderByStartAtAsc().stream()
            .map(time -> TimeResponse.from(time, bookedTimeIds.contains(time.getId())))
            .toList();
    }

    @Transactional
    public TimeResponse addReservationTime(TimeRequest request) {
        validateDuplicateTime(request);
        return TimeResponse.from(
            reservationTimeRepository.save(new ReservationTime(request.startAt())));
    }

    private void validateDuplicateTime(TimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new DuplicatedException("reservationTime");
        }
    }

    @Transactional
    public void removeReservationTime(Long id) {
        reservationTimeRepository.deleteById(id);
    }
}
