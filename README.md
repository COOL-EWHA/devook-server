Devook의 서버 레포지토리입니다. 프로젝트 전반에 대한 설명은 [이곳](https://github.com/COOL-EWHA/devook-web)을 참조해주세요.

## 백엔드 기술 스택

**언어:** JAVA

**프레임워크:** Springboot

**데이터베이스:** MySQL

**배포:** 서버(AWS EC2, Nginx, Route53, S3, CodeDeploy), DB(AWS RDS)






##  기능



- **사용자 인증**
    - 구글, 애플 기반 OAuth 회원가입/로그인을 지원합니다. 
- **북마크 등록, 수정, 삭제**
    - 사용자는 url 을 등록하여 북마크를 직접 등록할 수 있습니다. 서버는 웹 크롤러를 통해 얻은 해당 북마크 제목 정보를 인공지능 서버에 전송하여 카테고리 정보를 얻고, 데이터베이스에 저장합니다. 
    - 사용자는 저장된 북마크의 메모와 읽기 기한을 수정할 수 있습니다. 
- **추천 글 목록 반환**
    - 사용자 맞춤 추천글, 개별 글 추천글로 총 2가지 종류의 추천글을 리턴합니다.
- **알림 생성, 전송**
    - 읽기 기한이 설정된 글에 스케쥴러 함수를 사용해 매일 오전 9시, 오후 9시에 알림을 생성하여 데이터베이스에 저장합니다. 
    - Onesignal의 `Create-Notification` API를 사용하여 사용자의 디바이스에 알림을 전송합니다. 


## 데이터베이스 구조

![database](https://user-images.githubusercontent.com/67693142/170476716-5e74f8e3-02d7-4b05-949d-4c9f65f24598.png)


## 서버 구조

Devook의 서버는 프로덕션, 테스트 서버로 나누어 구성되어 있습니다. 테스트 서버에서 확인 완료된 API는 Github Action을 사용하여 프로덕션 서버에 배포합니다. 프로덕션 서버의 배포 과정 및 구조는 아래와 같습니다.


![서버구조 001](https://user-images.githubusercontent.com/67693142/170476668-f1d960dd-2d9e-488f-939a-ecee611c17b1.jpeg)


##  파일 구조

`Controller`, `Service`, `Repository`, `Domain`, `Dto` 로 구분하여 구성하였습니다.
