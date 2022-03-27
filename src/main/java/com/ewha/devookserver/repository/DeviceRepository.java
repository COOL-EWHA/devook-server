package com.ewha.devookserver.repository;

import com.ewha.devookserver.domain.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
  void deleteDeviceByUserIdxAndDeviceId(Long userIdx, String deviceId);
  Boolean existsDeviceByUserIdxAndDeviceId(Long userIdx, String deviceId);
}
