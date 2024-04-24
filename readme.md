## info 💁### 실행 순서1. eureka server2. gateway server3. redis4. user, article application<br>### Redis- host: localhost- port: 6379<br>### 1. Eureka Serverhttp://localhost:8761<br>### 2. Gateway Serverhttp://localhost:8080<br>### 3. User Application||method|url||:--|:--:|:--|| 회원가입| `POST` | http://localhost:8080/sign-up|| 회원탈퇴| `POST` | http://localhost:8080/withdrawal|| 로그아웃| `DELETE` | http://localhost:8080/logout|| 모든 회원 조회| `GET` | http://localhost:8080/user|| 내 계정 조회| `GET` | http://localhost:8080/user/{id}|| 내 계정 수정| `PUT` | http://localhost:8080/user/{id}|| 계정 권한 수정| `PUT` | http://localhost:8080/user/role|| 이메일, 닉네임 중복 확인| `GET` | http://localhost:8080/check|| 이메일 인증코드 전송| `POST` | http://localhost:8080/check/auth|| 메일 인증코드 검증| `GET` | http://localhost:8080/auth|<br>### 4. Article Application||method|url||:--|:--:|:--|| 모든 유저의 게시글 전체 조회| `GET` | http://localhost:8080/articles|| 특정 유저의 게시글 전체 조회| `GET` | http://localhost:8080/{userId}/articles|| 특정 유저의 게시글 단건 조회| `GET` | http://localhost:8080/{userId}/articles/{articleId}|| 특정 유저의 게시글 등록| `POST` | http://localhost:8080/{userId}/articles|| 특정 유저의 게시글 수정| `PUT` | http://localhost:8080/{userId}/articles/{articleId}|| 특정 유저의 게시글 삭제| `DELETE` | http://localhost:8080/{userId}/articles|| 탈퇴 회원 게시글 삭제| `DELETE` | http://localhost:8080/withdrawal/articles|<br><br> 