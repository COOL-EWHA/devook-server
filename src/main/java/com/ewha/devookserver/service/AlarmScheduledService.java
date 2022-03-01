package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.user.Member;
import com.ewha.devookserver.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AlarmScheduledService {

  private final MemberRepository memberRepository;
  private final AlarmService alarmService;

  @Scheduled(cron = "00 30 23 * * *")
  public void generateUsageAlarm() {

    List<Member> memberList = memberRepository.findAll();

    for (Member member : memberList) {
      alarmService.saveUsageAlert(member.getId());
    }
  }

  @Scheduled(cron = "00 30 11 * * *")
  public void generateTitleAlarm() {

    List<Member> memberList = memberRepository.findAll();

    for (Member member : memberList) {
      alarmService.saveTitlePost(member.getId());
    }
  }
}
