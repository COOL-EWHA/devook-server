package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.post.Alarm;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {

  List<Alarm> findAllByUserIdx(Long userIdx);

  Alarm findByAlarmIdx(Long alarmIdx);
}
