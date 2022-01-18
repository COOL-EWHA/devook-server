package com.ewha.devookserver.repository;

import com.ewha.devookserver.config.auth.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOauthId(String id);
    Member findMemberById(Long id);

    @Transactional
    void deleteMemberById(Long id);

    boolean existsById(Long id);
    boolean existsMemberByOauthId(String oauthid);
    boolean existsMemberByRefreshToken(String refreshToken);

    Member findMemberByRefreshToken(String refreshToken);
    Member findMemberByOauthId(String oauthid);

    Member findMemberByEmail(String email);

    @Query("select p from Member p where p.email=?1 and p.imageUrl like %?2%")
    Member returnGoogleMemberByEmail(String email, String imageUrl);

    boolean existsMemberByEmail(String email);

    int countMemberByEmail(String email);

}
