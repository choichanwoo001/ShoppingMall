# 📚 FastCampus BookStore

Spring Boot 기반의 온라인 도서 쇼핑몰 웹 애플리케이션입니다. 
도서 검색, 장바구니, 주문, 리뷰 등 전자상거래의 핵심 기능을 제공합니다.

---

## 🚀 주요 기능

### 👤 사용자
- 회원가입, 로그인, 회원정보 수정
- 도서 검색(키워드, 카테고리, 인기 검색어)
- 도서 상세/리뷰/최근 본 상품
- 장바구니(추가/수정/삭제, 수량 관리)
- 주문/주문 내역 조회
- 마이페이지(주문 내역, 리뷰 관리, 회원정보 관리)

### 🔧 관리자
- 도서 등록/수정/삭제, 재고 관리
- 주문 상태 관리, 주문 상세 조회
- 회원 목록/상세 조회

---

## 🛠 기술 스택

### Backend
- Java 21
- Spring Boot 3.5.3
- Spring Data JPA
- Spring Session
- MySQL 8.0
- Gradle
- Lombok

### Frontend
- Thymeleaf
- HTML5/CSS3
- JavaScript
- Bootstrap

---

## 📁 프로젝트 구조

- `src/main/java/com/example/fastcampusbookstore/`
  - `controller/` : REST/웹 컨트롤러
  - `service/` : 비즈니스 로직
  - `repository/` : JPA 리포지토리
  - `entity/` : JPA 엔티티
  - `dto/` : 요청/응답/공통 DTO
- `src/main/resources/templates/` : Thymeleaf 템플릿(화면)
- `src/main/resources/static/` : 정적 리소스(JS, CSS, 이미지)
- `src/test/` : 테스트 코드, 테스트 설정

---

## ⚙️ 실행/개발 환경 및 설정

### 1. 필수 소프트웨어
- Java 21
- MySQL 8.0
- Gradle 8.x
- (권장) IntelliJ IDEA, VSCode 등

### 2. 데이터베이스 준비
```sql
CREATE DATABASE fastcampus_bookstore;
```

### 3. 환경설정 파일
`src/main/resources/application.properties`에서 DB 접속 정보를 수정하세요.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fastcampus_bookstore?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

## 🏁 실행 방법

1. 의존성 설치 및 빌드
   ```bash
   ./gradlew build
   ```
2. 애플리케이션 실행
   ```bash
   ./gradlew bootRun
   ```
3. [http://localhost:8080](http://localhost:8080) 접속

---

## 🧪 테스트 환경 및 자동화

- **테스트 자동화**: `src/test/resources/data.sql`에 샘플 데이터가 복사되어 있어, `./gradlew test` 실행 시 테스트 DB에 자동으로 데이터가 세팅됩니다.
- 테스트 DB 분리: `src/test/resources/application-test.properties`에서 별도 DB 사용 가능
- JPA 단위 테스트, 통합 테스트 작성 가능
- 테스트 실행:
  ```bash
  ./gradlew test
  ```

---

## 🗂️ DB 초기 데이터 (샘플)

- **DB 초기화용 전체 쿼리**는 `init-data.sql` 파일을 참고하세요. (운영/개발 환경에서 직접 실행)
- **테스트 자동화용 쿼리**는 `src/test/resources/data.sql`에 복사되어 있습니다. (테스트 실행 시 자동 적용)

> 아래 SQL 쿼리를 MySQL에 실행하면, 관리자/회원/카테고리/도서/재고/장바구니/주문/리뷰/베스트셀러 등 샘플 데이터가 자동으로 세팅됩니다.

- **관리자 계정:**
  - ID: `admin001`
  - PW: `password`
- **회원 계정:**
  - ID: `Choi3495`
  - PW: `choi3495!`

---

## 📝 기타

- 이미지/샘플 PDF 등은 `/src/main/resources/static/images/`, `/pdf/` 경로에 위치
- 추가 데이터/기능/테스트는 자유롭게 확장 가능
