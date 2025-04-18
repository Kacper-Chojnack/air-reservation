package com.example.airreservation.repository;

import com.example.airreservation.model.schedule.RecurringFlightSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringFlightScheduleRepository extends JpaRepository<RecurringFlightSchedule, Long> {

    List<RecurringFlightSchedule> findByActiveTrue();
}