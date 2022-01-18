package com.ewha.devookserver.domain.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@NoArgsConstructor
@Entity
@Table(name="userToken")
public class UserToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userTokenIdx;
    private String userEmail;
    private String userToken;

    @UpdateTimestamp
    private Timestamp tokenModified;
}
