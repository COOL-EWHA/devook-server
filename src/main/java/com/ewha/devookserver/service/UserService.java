package com.ewha.devookserver.service;

import com.ewha.devookserver.domain.user.Member;
import com.ewha.devookserver.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private final MemberRepository memberRepository;

    public Member findMemberInfo(Long id){
        return memberRepository.findMemberById(id);
    }

    public boolean isUserExists(Long id){
        return memberRepository.existsById(id);
    }

    public void deleteMember(Long id){
        memberRepository.deleteMemberById(id);
    }

    public boolean checkRightRefreshToken(String refreshToken){
        if(memberRepository.findMemberByRefreshToken(refreshToken)==null)return false;
        return true;
    }

    public Member returnRefreshTokenMember(String refreshToken){
        System.out.println(refreshToken);
        return memberRepository.findMemberByRefreshToken(refreshToken);
    }

    public boolean isMemberExistByEmail(String email){
        if(memberRepository.existsMemberByEmail(email)){
            return true;
        }
        else return false;
    }

    public Member returnEmailUSer(String email){
        return memberRepository.findMemberByEmail(email);
    }

    public Member ifGoogleUser(String email){
        return memberRepository.returnGoogleMemberByEmail(email, "google");
    }

}
