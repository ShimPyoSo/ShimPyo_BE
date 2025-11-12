# 쉼표(Comma)
<img width="1920" height="1080" alt="1" src="https://github.com/user-attachments/assets/9587fd37-45ca-4a52-b472-d588e558511c" />
사용자의 취향과 상태에 맞춘 웰니스 관광지·코스를 추천하고, 코스 공유와 웰니스 지수로 개인화된 힐링 웰니스 여행을 지원하는 서비스  

[💭쉼표 방문하기](https://shimpyo.site/)  

***

<br>


## 📄 서비스 목표
**심신 회복을 위한 웰니스(Wellness) 활동으로 변화**하고 있는 최근 여행의 경향은 MZ세대의 관심을 받고 있습니다.  
기존 플랫폼들은 이러한 웰니스 특화 콘텐츠나 개인 맞춤형 추천 기능을 제공하지 못하는 한계가 있었고,  
이에 **쉼표**는사용자의 심리 상태를 진단하고 휴식 유형에 맞춘 웰니스 관광 코스를 추천함으로써 **개인화되고 만족도 높은 웰니스 관광 경험을 제공**합니다.

<br>
<br>

## 🕑 개발 기간
기획 - 2025.04.16 ~ 2025.06.25(2개월)  
<br>
개발 - 2025.06.30 ~ 2025.09.25(3개월)

<br>
<br>


## 🔧 기술 스택
<img src="https://img.shields.io/badge/JAVA-FE8427?style=for-the-badge"/> <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=FFFFFF"/> <img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=FFFFFF"/> <img src="https://img.shields.io/badge/mariadb-003545?style=for-the-badge&logo=mariadb&logoColor=FFFFFF"/> 
<img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=FFFFFF"/>  
<img src="https://img.shields.io/badge/elasticsearch-005571?style=for-the-badge&logo=elasticsearch&logoColor=FFFFFF"/>
<img src="https://img.shields.io/badge/AWS-005571?style=for-the-badge"/>
<img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=FFFFFF"/>
<img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=FFFFFF"/>
<img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=FFFFFF"/>
<img src="https://img.shields.io/badge/discord-5865F2?style=for-the-badge&logo=discord&logoColor=FFFFFF"/>
<img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=FFFFFF"/>

<br>
<br>
<br>

## 🪢 아키텍처 구조
<img width="1462" height="729" alt="쉼표아키텍처" src="https://github.com/user-attachments/assets/a6212de8-530e-4164-b36c-684808d259af" />

<br>
<br>
<br>

## 🎯 주요 기능
1. **관광지 정보 검색**: 관광지 목록과 키워드 검색으로 관광지를 탐색할 수 있습니다.
2. **관광지 필터링**: 관광지 목록에서 지역, 운영 시간 등 다양한 필터로 조건에 맞는 관광지를 탐색할 수 있습니다.
3. **관광 코스 추천 기능**: 스토리형 테스트를 통한 쉼표 유형(휴식 유형) 추론 및 유형, 지역, 일정에 맞춘 관광지를 추천합니다.
4. **관광 코스 수정 및 공유**: 자유로운 관광 코스 추가·수정이 가능하며 공유로 비회원에게도 코스 공유가 가능합니다.
5. **웰니스 지수**: 다양한 환경 데이터를 기반으로 오늘의 웰니스 지수 측정과 관광지 별 날씨,집중률을 반영한 7일간 예상 추이 제공합니다.
6. **관광지 후기**: 관광지 별 후기 조회 및 작성이 가능합니다.

<br>
<br>

## 👩‍💻 팀원
#### 송은영(Designer) - UI/UX 디자인 담당
#### 장현지(FrontEnd) - UI/UX 개발 담당
#### 유나영(BackEnd)  - AWS 배포 및 백엔드 기능 개발 담당
###### <span style="color:gray"><s>허지수(PM, ~25.06) - 초기 기획 참여</s></span>
###### <span style="color:gray"><s>최무리(BE, ~25.07) - 기획 및 초기 개발 참여(일반 회원가입, 관광지 상세 조회)</s></span>

<br>
<br>

## 🛢️ ERD
<img width="985" height="635" alt="image" src="https://github.com/user-attachments/assets/210fe577-d234-4c3e-962e-dfdbd9d6888e" />

<br>
<br>
<br>

## 📃 기능 목록
### 1. 사용자 인증 및 회원 가입

| 번호 | 기능 | 설명 |
| :--- | :--- | :--- |
| **1.1** | **로그인 (로컬/소셜)** | 아이디/비밀번호 또는 소셜 계정을 이용해 서비스에 접속하고 자동 로그인을 지원한다. |
| **1.2** | **회원가입 및 추가 정보** | 필수 정보를 입력하여 계정을 생성하고, 맞춤 추천을 위한 추가 정보를 입력한다. |
| **1.3** | **이메일 인증** | 가입 시 이메일로 인증 코드를 받아 계정을 활성화한다. |
| **1.4** | **아이디/비밀번호 찾기** | 계정 정보 분실 시 아이디를 찾거나 비밀번호를 재설정한다. |
| **1.5** | **토큰 재발급 / 로그아웃** | 보안을 유지하며 세션을 갱신하거나 안전하게 접속을 종료한다. |
| **1.6** | **중복 검사 / 회원 탈퇴** | 아이디 중복 여부를 검사하고, 사용자가 본인의 계정을 삭제한다. |

<br>

### 2. 마이페이지 및 개인화 설정

| 번호 | 기능 | 설명 |
| :--- | :--- | :--- |
| **2.1** | **닉네임 관리** | 닉네임을 변경하고, 변경 시 중복 여부를 검사한다. |
| **2.2** | **찜 목록 (장소/코스)** | 사용자가 찜한 개별 관광지 및 코스 목록을 조회한다. |
| **2.3** | **찜한 코스 상세 조회** | 찜한 코스의 상세 정보와 포함된 관광지를 확인한다. |
| **2.4** | **최근 본 관광지 조회** | 최근에 열람한 관광지 목록을 조회한다. |
| **2.5** | **내가 쓴 후기 관리** | 작성한 후기 목록을 조회하고 상세 내용을 확인하거나 삭제한다. |

<br>

### 3. 코스 및 장소 탐색/관리

| 번호 | 기능 | 설명 |
| :--- | :--- | :--- |
| **3.1** | **코스 수정 및 삭제** | 저장된 코스의 이름, 구성 관광지를 수정하거나 코스 자체를 삭제한다. |
| **3.2** | **코스 장소 추가** | 코스 편집 시 시스템이 추천하는 관광지 목록을 참고하여 장소를 추가한다. |
| **3.3** | **코스/관광지 찜하기** | 추천 코스나 개별 관광지를 개인의 찜 목록에 저장한다. |
| **3.4** | **키워드 검색 및 자동완성** | 지역/장소명을 기반으로 관광지를 검색하고 검색어 자동완성 기능을 제공한다. |
| **3.5** | **맞춤 코스 추천** | 사용자의 웰니스 유형별로 관광지 및 코스를 추천하여 출력한다. |
| **3.6** | **여행지 상세 정보** | 특정 관광지에 대한 위치, 설명, 후기 등의 상세 정보를 제공한다. |

<br>

### 4. 커뮤니티 및 콘텐츠

| 번호 | 기능 | 설명 |
| :--- | :--- | :--- |
| **4.1** | **후기 작성 및 조회** | 관광지에 대한 후기를 작성하고, 다른 사용자의 후기 목록을 열람한다. |
| **4.2** | **이미지 업로드** | 후기에 사용할 이미지를 서버에 등록한다. |

<br>
<br>

## ✨ 결과 화면

<table>
  <tr>
    <td>
      <b>1. 메인</b><br>
      <video src="https://github.com/user-attachments/assets/dbdcc48b-d31c-47a1-8264-cb1308705eaa" width="320" autoplay loop muted></video>
    </td>
    <td>
      <b>2. 일반 회원가입</b><br>
      <video src="https://github.com/user-attachments/assets/f88e84bc-88f7-4996-a4de-2f417be5d6b8" width="320" autoplay loop muted></video>
    </td>
  </tr>
  <tr>
    <td>
      <b>3. 소셜 회원가입</b><br>
      <video src="https://github.com/user-attachments/assets/b5386e09-7b88-4116-a32f-051cac72cd18" width="320" autoplay loop muted></video>
    </td>
    <td>
      <b>4. 계정 관리</b><br>
      <video src="https://github.com/user-attachments/assets/fc69c208-f29c-4112-8b6b-9eccd64a89e4" width="320" autoplay loop muted></video>
    </td>
  </tr>
  <tr>
    <td>
      <b>5. 필터링</b><br>
      <video src="https://github.com/user-attachments/assets/df55ba27-a234-4b79-8d7a-ab4104290a55" width="320" autoplay loop muted></video>
    </td>
    <td>
      <b>6. 상세페이지</b><br>
      <video src="https://github.com/user-attachments/assets/42f8b75d-bfed-44e9-be28-72ec8d47ecba" width="320" autoplay loop muted></video>
    </td>
  </tr>
  <tr>
    <td>
      <b>7. 검색</b><br>
      <video src="https://github.com/user-attachments/assets/cbadcc68-27e8-4d2e-9816-92a3aad294de" width="320" autoplay loop muted></video>
    </td>
    <td>
      <b>8. 쉼표(웰니스) 유형 테스트</b><br>
      <video src="https://github.com/user-attachments/assets/5ff657c2-92eb-4b62-ab07-a78661b1e1a1" width="320" autoplay loop muted></video>
    </td>
  </tr>
  <tr>
    <td>
      <b>9. 코스 수정</b><br>
      <video src="https://github.com/user-attachments/assets/39823e8b-8088-4429-923b-a149e66d8763" width="320" autoplay loop muted></video>
    </td>
    <td>
      <b>10. 코스 삭제</b><br>
      <video src="https://github.com/user-attachments/assets/94fb77b8-c525-45e2-8501-6de53ac269bf" width="320" autoplay loop muted></video>
    </td>
  </tr>
  <tr>
    <td>
      <b>11. 마이페이지</b><br>
      <video src="https://github.com/user-attachments/assets/92b71ebb-cc8c-4ebf-9fc5-2a77674dc85c" width="320" autoplay loop muted></video>
    </td>
    <td>
      <b>12. 후기 작성 및 삭제</b><br>
      <video src="https://github.com/user-attachments/assets/42fa0990-0af7-4c10-89f4-a39fe9810293" width="320" autoplay loop muted></video>
    </td>
  </tr>
  <tr>
    <td>
      <b>13. 코스 및 관광지 정보 공유</b><br>
      <video src="https://github.com/user-attachments/assets/67f180bc-a9a7-4ce7-a0ef-243b61ef4271" width="320" autoplay loop muted></video>
    </td>
    <td>
      <b>14. 자동 로그인</b><br>
      <video src="https://github.com/user-attachments/assets/e6194587-d40a-4a76-b927-ea02dfa5f0af" width="320" autoplay loop muted></video>
    </td>
  </tr>
</table>

