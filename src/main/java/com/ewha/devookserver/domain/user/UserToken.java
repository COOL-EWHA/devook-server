package com.ewha.devookserver.domain.user;

import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "userToken")
public class UserToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userTokenIdx;
  private String userEmail;
  private String userToken;

  @UpdateTimestamp
  private Timestamp tokenModified;
}
