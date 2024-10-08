## info 💁

### Docker Run
```shell
docker network create --driver bridge simple-board-02-network
docker compose -f compose-local.yml up -d --build
```
⚠️ 실행하기 전에
- redis, mariadb: `데이터 볼륨 마운트 경로` 확인
- mariadb: 환경 설정 파일 존재 확인 (`./simpleBoard02/.mariadb_local_env`)
- spring boot jar 파일 생성 전에 password.yml 파일 존재 확인 (`resources/password.yml`)

<br>

### 실행 순서
1. eureka server
2. gateway server
3. redis(, mariadb)
4. user, article application

<br>

### Redis
- host: localhost
- port: 6379

<br>

### Mariadb
- host: localhost
- port: 3306
- `prod profile`에서만 사용

<br>

### 1. Eureka Server
http://localhost:8761

<br>

### 2. Gateway Server
http://localhost:8080

<br>

### 3. Swagger
- user application
  - http://zhyun.kim:8080/
  - http://zhyun.kim:8080/api/user
  - http://zhyun.kim:8080/api/user/swagger-ui/index.html
- article application
  - http://zhyun.kim:8080/api/article
  - http://zhyun.kim:8080/api/article/swagger-ui/index.html

<br>

### 4. User Application
|                |  method  | url                                       |      필요한 권한      |
|:---------------|:--------:|:------------------------------------------|:----------------:|
| 회원가입           |  `POST`  | http://localhost:8080/api/user/sign-up    |                  |
| 회원탈퇴           |  `POST`  | http://localhost:8080/api/user/member/withdrawal | `MEMBER`,`ADMIN` |
| 로그인            | `POST` | http://localhost:8080/api/user/login      |                  |
| 로그아웃           | `POST` | http://localhost:8080/api/user/member/logout     | `MEMBER`,`ADMIN` |
| 모든 회원 조회       | `GET`    | http://localhost:8080/api/user/member/all        |     `ADMIN`      |
| 내 계정 조회        |  `GET`   | http://localhost:8080/api/user/member/{id}       | `MEMBER`,`ADMIN` |
| 내 계정 수정        |  `PUT`   | http://localhost:8080/api/user/member/{id}       | `MEMBER`,`ADMIN` |
| 계정 권한 수정       |  `PUT`   | http://localhost:8080/api/user/member/role       |     `ADMIN`      |
| 이메일 중복 확인 |  `GET`   | http://localhost:8080/api/user/check/duplicate-email      |                  |
| 닉네임 중복 확인 |  `GET`   | http://localhost:8080/api/user/check/duplicate-nickname      |                  |
| 이메일 인증코드 전송    |  `POST`  | http://localhost:8080/api/user/check/auth |                  |
| 메일 인증코드 검증     |  `GET`   | http://localhost:8080/api/user/check/auth|                  |

<br>

### 5. Article Application
|                     | method | url                                                         |        필요한 권한         |
|:--------------------|:------:|:------------------------------------------------------------|:---------------------:|
| 모든 유저의 게시글 전체 조회  | `GET`  | http://localhost:8080/api/article/all                       |                       |
| 특정 유저의 게시글 전체 조회  | `GET`  | http://localhost:8080/api/article/all/user/{userId}         |                       |
| 특정 유저의 게시글 단건 조회  | `GET`  | http://localhost:8080/api/article/{articleId}/user/{userId} |                       |
| 특정 유저의 게시글 등록      | `POST` | http://localhost:8080/api/article/save                      | 작성자(`MEMBER`,`ADMIN`) |
| 특정 유저의 게시글 수정      | `PUT`  | http://localhost:8080/api/article/update                    | 작성자(`MEMBER`,`ADMIN`) |
| 특정 유저의 게시글 삭제      | `POST` | http://localhost:8080/api/article/delete                    | 작성자(`MEMBER`,`ADMIN`) |
| 탈퇴 회원 게시글 삭제        | `POST` | http://localhost:8080/api/article/delete/withdrawal         |                       |

<br>

### 6. H2 Web Console
embedded 형태로, `local profile`에서만 사용
- user application db
- http://localhost:8080/h2/user
  - url
    ```
    jdbc:h2:./simpleBoard02/b-server-user/h2/member;AUTO_SERVER=true;mode=MYSQL;
    ```
  - username : `sa`
  - password 없음  <br><br>
- article application db
- http://localhost:8080/h2/article
  - url
    ```
    jdbc:h2:./simpleBoard02/b-server-article/h2/article;AUTO_SERVER=true;mode=MYSQL;
    ```
  - username : `sa`
  - password 없음

<br><br> 

