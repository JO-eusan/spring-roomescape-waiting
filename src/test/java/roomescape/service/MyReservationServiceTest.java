package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.entity.Member;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.entity.Waiting;
import roomescape.exception.custom.AuthenticatedException;
import roomescape.exception.custom.NotFoundException;
import roomescape.repository.jpa.JpaMemberRepository;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;
import roomescape.repository.jpa.JpaWaitingRepository;

@DataJpaTest
class MyReservationServiceTest {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private JpaThemeRepository themeRepository;
    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;
    @Autowired
    private JpaMemberRepository memberRepository;
    @Autowired
    private JpaReservationRepository reservationRepository;
    @Autowired
    private JpaWaitingRepository waitingRepository;

    private MyReservationService myReservationService;

    @BeforeEach
    void setUp() {
        myReservationService = new MyReservationService(reservationRepository, waitingRepository);
    }

    @Test
    @DisplayName("사용자 자신의 예약 및 예약 대기를 조회할 수 있다.")
    void findReservationsAndWaitingsByMemberId() {
        Member member = saveMember(1L);
        Theme theme1 = saveTheme(1L);
        Theme theme2 = saveTheme(2L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);
        LocalDateTime createdAt = LocalDateTime.of(2025, 4, 28, 1, 0);

        em.flush();
        em.clear();

        Reservation reservation = new Reservation(member, date, time, theme1);
        reservationRepository.save(reservation);

        Waiting waiting = new Waiting(member, date, time, theme2, createdAt);
        waitingRepository.save(waiting);

        assertThat(myReservationService.findReservationsAndWaitingsByMemberId(member)).hasSize(2);
    }

    @Test
    @DisplayName("사용자가 예약 대기를 취소할 수 있다.")
    void removeWaiting() {
        Member member1 = saveMember(1L);
        Member member2 = saveMember(2L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);
        LocalDateTime createdAt = LocalDateTime.of(2025, 4, 28, 1, 0);

        em.flush();
        em.clear();

        Reservation reservation = new Reservation(member1, date, time, theme);
        reservationRepository.save(reservation);

        Waiting waiting = new Waiting(member2, date, time, theme, createdAt);
        waitingRepository.save(waiting);

        myReservationService.removeWaiting(member2, waiting.getId());

        assertThat(waitingRepository.findById(waiting.getId()).isPresent()).isFalse();
    }

    @Test
    @DisplayName("사용자가 예약 대기가 없는 정보를 취소하면 예외가 발생한다.")
    void removeWaitingOfNotExisted() {
        Member member1 = saveMember(1L);
        Member member2 = saveMember(2L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);
        LocalDateTime createdAt = LocalDateTime.of(2025, 4, 28, 1, 0);

        em.flush();
        em.clear();

        Reservation reservation = new Reservation(member1, date, time, theme);
        reservationRepository.save(reservation);

        Waiting waiting = new Waiting(member2, date, time, theme, createdAt);
        waitingRepository.save(waiting);

        assertThatThrownBy(() -> myReservationService.removeWaiting(member2, waiting.getId() + 1))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("waiting");
    }

    @Test
    @DisplayName("다른 사용자가 예약 대기를 삭제하면 예외가 발생한다.")
    void removeWaitingWithAnotherMember() {
        Member member1 = saveMember(1L);
        Member member2 = saveMember(2L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2025, 4, 28);
        LocalDateTime createdAt = LocalDateTime.of(2025, 4, 28, 1, 0);

        em.flush();
        em.clear();

        Reservation reservation = new Reservation(member1, date, time, theme);
        reservationRepository.save(reservation);

        Waiting waiting = new Waiting(member2, date, time, theme, createdAt);
        waitingRepository.save(waiting);

        assertThatThrownBy(() -> myReservationService.removeWaiting(member1, waiting.getId()))
            .isInstanceOf(AuthenticatedException.class)
            .hasMessageContaining("삭제 권한 없음");
    }

    private Member saveMember(Long tmp) {
        Member member = Member.createUser("이름" + tmp, "이메일" + tmp, "비밀번호" + tmp);
        memberRepository.save(member);

        return member;
    }

    private Theme saveTheme(Long tmp) {
        Theme theme = new Theme("이름" + tmp, "설명" + tmp, "썸네일" + tmp);
        themeRepository.save(theme);

        return theme;
    }

    private ReservationTime saveTime(LocalTime reservationTime) {
        ReservationTime time = new ReservationTime(reservationTime);
        reservationTimeRepository.save(time);

        return time;
    }
}
