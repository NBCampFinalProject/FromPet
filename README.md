<div align="center">
<h2>[2023] 반려동물 커뮤니티 From Pet 어플 제작 🐕</h2>
사용자는 반려동물의 정보를 입력하여 회원가입을 진행하며 자신과 가까운 사용자를 조회하며 채팅 & 커뮤니티 & 지도를 이용할 수 있는 어플입니다~! <br> 사람 대 사람으로 이루어지는 어플이기 때문에 개발자가 사용자 입장에서 많은 이용을 해보며 런칭하게 되었습니다. <br> 여러 반려동물의 정보와 상대방과 채팅을 하면서 친해져봐요 🙌
</div>

<br/>


## 목차
  - [팀원 소개](#팀원-구성)
  - [개발환경](#1-개발환경)
  - [프로젝트 소개](#2-프로젝트-소개)
  - [페이지별 기능](#3-페이지별-기능)
  - [이용 방법](#이용방법)
 

<br/>


## 팀원 구성

<div align="center">

| **박세준** | **사석현** | **이승현** | **이수진** |
| :------: |  :------: | :------: | :------: |
| [<img src= "https://github.com/NBCampFinalProject/FromPet/assets/106301222/3a256a71-5a3f-4fa2-8e69-81086de97bd8" height=180 width=180> <br/> @boradorying](https://github.com/boradorying) | [<img src="https://github.com/NBCampFinalProject/FromPet/assets/106301222/9c8d5ad1-4a24-4ea4-b374-c8c9161fa6f3" height=180 width=180> <br/> @4seokhyeon](https://github.com/4seokhyeon)| [<img src="https://github.com/NBCampFinalProject/FromPet/assets/106301222/11049fcc-d48f-4cfa-a839-f85f1d3f12fc" height=180 width=180> <br/> @shyr0809](https://github.com/shyr0809)|[<img src="https://github.com/NBCampFinalProject/FromPet/assets/106301222/3826a521-c98c-4f73-b944-25c704bd87b1" height=180 width=180> <br/> @sooj36](https://github.com/sooj36) |
</div>

## 1. 개발환경 
 - 안드로이드 스튜디오
 - 데이터 베이스 및 서버: FireBase
 - 데일리 스크럼 및 회의록 : [Notion](https://www.notion.so/12-b5e63529398b49e68ab10b59e636e6ea)
 - 디자인 : [Figma](https://www.figma.com/file/78mamyOeOlT9VOeaRywoH7/12%EC%A1%B0-%EC%99%80%EC%9D%B4%EC%96%B4%ED%94%84%EB%A0%88%EC%9E%84?type=design&node-id=59-12&mode=design&t=l6CIi1VZS6vIykLs-0)
<br/>

 - 프로젝트 지속기간: 2023.10 -2023.11.14

 - 개발 엔진 및 언어: Android Studio & Kotlin
 

<br/>


## 2. 프로젝트 소개
- FromPet은 반려동물과 함께 소중한 시간을 보내고 있는 사람들을 타겟팅한 반려동물 커뮤니티 어플입니다.
- 귀여운 반려 동물을 카드에서 발견하면 상대방에게 좋아요를 보냄으로써 더욱 깊은 이야기를 할 수 있게 채팅이 구현되어있습니다!
- 또한 내가 보고싶은 반려동물의 종류와 


## 3. 페이지별 기능

### [초기화면]
* * *
- 첫 화면은 splash 화면이 뷰페이저로 나타납니다.
    - 로그인이 되어 있지 않은 경우 : Login 화면
    - 로그인이 되어 있는 경우 : Home 화면
    - 소셜 로그인(구글로그인), 이메일 기반 회원가입 및 로그인
 

<div align="left">

<img width="30%" src="https://github.com/NBCampFinalProject/FromPet/assets/106301222/aac2a14e-4de0-41cf-99a6-b863652eb26c.gif"/> 
</div>

### [회원가입]
* * *
- 이메일 형식으로 회원가입을 진행 할 수 있으며 이메일 형식 & 비밀번호 6자리 이상으로 가입이 가능합니다.
- 또한 소셜 로그인 기능도 추가해놓았기 때문에 구글 로그인 버튼을 누르면 빠르게 회원가입&로그인이 가능합니다.

  

! <img width="30%" src="https://github.com/NBCampFinalProject/FromPet/assets/106301222/cfae4ae3-a6c8-42ce-919b-d40c86db1361.gif"/>
<br>


### [프로필 설정]
* * *
- 첫 회원가입을 하게 되면 자동으로 회원정보를 입력하는 창이 뜨게 됩니다.
- 이때 회원 정보를 저장하지 않거나 넘어갈 수 없게 하여 예외가 생길 수도 있는 부분을 배제 하였습니다.
- 작성이 완료되면 자동적으로 홈화면으로 넘어갑니다.

 <img width="30%" src="https://github.com/NBCampFinalProject/FromPet/assets/106301222/9b47174b-0b71-434e-a465-decc35e99cba.gif"/> 
<br>

### [로그인]
* * *
- 로그인은 소셜 로그인와 이메일 로그인 둘을 지원합니다.
- 로그인을 하면 회원 정보 여부에 따라서 화면이 전환됩니다.
- 회원 정보가 없을 경우 : 회원정보 입력 화면
- 회원 정보가 있을 경우 : 홈 화면
<div align="center">
<img width="30%" src="https://github.com/NBCampFinalProject/FromPet/assets/106301222/86e81ee8-55c7-42ae-b242-b0e645717624.gif"/>
<img width="30%" src="https://github.com/NBCampFinalProject/FromPet/assets/106301222/b360d26a-f31c-416d-8489-feccb93a4b24.gif"/>
</div>

### [로그아웃]
* * *
<img width="30%" src="https://github.com/NBCampFinalProject/FromPet/assets/106301222/65918bae-5399-4668-af2a-c4a2cc13dc5f.git"/>
<br>

### [홈 화면]
* * *                             
<img width="30%" src=/>



## 이용방법
- 홈화면에서 마음에 드는 반려동물을 좋아요 해보세요! <br/>
그럼 상대방에게 좋아요 표시가 됩니다! 하지만 너무 연연하지 말아요 상대방이 마음에 들지 않으면 거절 할 수도 있답니다...🫢 좌절은 No~ No~

-  반려 동물 커뮤니티 어플입니다. <br/>
  홈화면에서 유저를 카드뷰로 확인할 수 있으며 위의 화면과 같이 자신이 원하는 상대방의 대한 정보를 필터링하여 식별 할 수 있습니다.🙏<br/>
- 상대방과 채팅을 하며 친해져 보세요! <br/>
  매칭된 상대방과 여러정보를 공유하며 채팅으로써 친해져보세요! 물론 상대방이 맘에 들지 않으면 나가기로 친구를 아예 끊을 수도 있습니다! 🫨<br/>
- FromPet만의 커뮤니티를 이용해 보세요 ! 💬<br/>
  상단에는 매칭된 유저의 등수와 쌍을 확인할수가 있고 하단에는 반려동물의 타입에 따라서 카테고리 정보를 각각 조회해 볼 수 있습니다! 🌈 <br/> 사용자의 반려동물 타입 요구가 많아지면 추후에 업데이트를 할 예정입니다!🤔

