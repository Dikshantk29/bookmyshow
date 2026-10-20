package com.bookmyshow.repository;

import com.bookmyshow.entity.SeatStatus;
import com.bookmyshow.entity.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    List<ShowSeat> findByShowId(Long showId);
    long countByShowIdAndStatus(Long showId, SeatStatus status);
    List<ShowSeat> findByStatusAndLockedUntilBefore(SeatStatus status, LocalDateTime time);

    /** Row-level DB lock so two users can never grab the same seat at the same time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ShowSeat s where s.show.id = :showId and s.seatNumber in :nums")
    List<ShowSeat> findForUpdate(@Param("showId") Long showId, @Param("nums") Collection<String> nums);
}
