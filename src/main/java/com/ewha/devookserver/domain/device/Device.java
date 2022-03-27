package com.ewha.devookserver.domain.device;

import static javax.persistence.GenerationType.IDENTITY;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Setter
@Getter
@Entity
@RequiredArgsConstructor
@Table(name = "device")
public class Device {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  public Long deviceIdx;

  public Long userIdx;
  public String deviceId;

  @CreationTimestamp
  public Timestamp createdAt;

  @Builder
  public Device(Long userIdx, String deviceId) {
    this.userIdx = userIdx;
    this.deviceId = deviceId;
  }
}
