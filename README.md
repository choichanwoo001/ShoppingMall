# 📚 FastCampus BookStore

온라인 도서 쇼핑몰 웹 애플리케이션입니다. Spring Boot와 Thymeleaf를 기반으로 구축된 전자상거래 플랫폼으로, 도서 검색, 장바구니, 주문, 결제 등 온라인 서점의 핵심 기능을 제공합니다.

## 🚀 주요 기능

### 👥 사용자 기능
- **회원 관리**: 회원가입, 로그인, 회원정보 수정
- **도서 검색**: 키워드 검색, 카테고리별 검색, 인기 검색어
- **상품 상세**: 도서 상세정보, 리뷰 시스템, 최근 본 상품
- **장바구니**: 상품 추가/수정/삭제, 수량 관리
- **주문/결제**: 카카오페이 연동 결제, 주문 내역 조회
- **마이페이지**: 주문 내역, 리뷰 관리, 회원정보 관리

### 🔧 관리자 기능
- **상품 관리**: 도서 등록, 수정, 삭제, 재고 관리
- **주문 관리**: 주문 상태 관리, 주문 상세 조회
- **회원 관리**: 회원 목록 조회, 회원 상세 정보
- **재고 관리**: 재고 현황 조회, 재고 수량 수정

## 🛠 기술 스택

### Backend
- **Java 21**
- **Spring Boot 3.5.3**
- **Spring Data JPA**
- **Spring Session**
- **MySQL 8.0**
- **Gradle**
- **Lombok**

### Frontend
- **Thymeleaf**
- **HTML5/CSS3**
- **JavaScript**
- **Bootstrap**

### 외부 연동
- **카카오페이 API** - 결제 시스템

## 📁 프로젝트 구조

```
src/main/java/com/example/fastcampusbookstore/
├── controller/          # REST API 컨트롤러
│   ├── AdminController.java
│   ├── BookController.java
│   ├── CartController.java
│   ├── CategoryController.java
│   ├── KakaoPayController.java
│   ├── MainController.java
│   ├── MemberController.java
│   ├── OrderController.java
│   ├── PageController.java
│   ├── PopularKeywordController.java
│   ├── RecentViewController.java
│   └── ReviewController.java
├── entity/              # JPA 엔티티
│   ├── Admin.java
│   ├── Book.java
│   ├── Cart.java
│   ├── CategoryBottom.java
│   ├── CategoryMiddle.java
│   ├── CategoryTop.java
│   ├── Inventory.java
│   ├── Member.java
│   ├── Order.java
│   ├── OrderDetail.java
│   ├── RecentView.java
│   └── Review.java
├── repository/          # 데이터 접근 계층
├── service/            # 비즈니스 로직
├── dto/                # 데이터 전송 객체
│   ├── common/         # 공통 DTO
│   ├── request/        # 요청 DTO
│   └── response/       # 응답 DTO
└── FastCampusBookStoreApplication.java
```

## 🗄 데이터베이스 설계

### 주요 엔티티
- **Book**: 도서 정보 (ISBN, 제목, 저자, 가격, 이미지 등)
- **Member**: 회원 정보 (ID, 이름, 이메일, 전화번호, 주소 등)
- **Category**: 3단계 카테고리 구조 (대/중/소분류)
- **Cart**: 장바구니 (회원별 상품 담기)
- **Order/OrderDetail**: 주문 정보
- **Review**: 상품 리뷰
- **Inventory**: 재고 관리
- **RecentView**: 최근 본 상품

## 🚀 실행 방법

### 1. 사전 요구사항
- Java 21
- MySQL 8.0
- Gradle

### 2. 데이터베이스 설정
```sql
CREATE DATABASE fastcampus_bookstore;
```

### 3. 애플리케이션 설정
`src/main/resources/application.properties` 파일에서 데이터베이스 연결 정보를 수정하세요:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/fastcampus_bookstore?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. 애플리케이션 실행
```bash
# Gradle Wrapper 사용
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew build
java -jar build/libs/fastcampus-bookstore-0.0.1-SNAPSHOT.jar
```

### 5. 접속
- **사용자 페이지**: http://localhost:8080
- **관리자 페이지**: http://localhost:8080/admin

## 📋 API 문서

### 주요 API 엔드포인트

#### 도서 관련
- `GET /api/books` - 도서 목록 조회 (검색 포함)
- `GET /api/books/{bookId}` - 도서 상세 조회
- `GET /api/books/bestsellers` - 베스트셀러 조회

#### 회원 관련
- `POST /api/members/signup` - 회원가입
- `POST /api/members/login` - 로그인
- `PUT /api/members/{memberId}` - 회원정보 수정

#### 장바구니 관련
- `GET /api/cart` - 장바구니 조회
- `POST /api/cart` - 장바구니 상품 추가
- `PUT /api/cart/{cartId}` - 장바구니 상품 수정
- `DELETE /api/cart/{cartId}` - 장바구니 상품 삭제

#### 주문 관련
- `POST /api/orders` - 주문 생성
- `GET /api/orders` - 주문 목록 조회
- `GET /api/orders/{orderId}` - 주문 상세 조회

#### 결제 관련
- `POST /api/pay/kakao/ready` - 카카오페이 결제 준비

## 🎨 주요 화면

### 사용자 화면
- **메인 페이지**: 베스트셀러, 인기도서, 최근 본 상품
- **상품 목록**: 카테고리별, 검색 결과
- **상품 상세**: 상품 정보, 리뷰, 장바구니 추가
- **장바구니**: 상품 관리, 주문 진행
- **주문/결제**: 카카오페이 결제
- **마이페이지**: 주문 내역, 리뷰 관리

### 관리자 화면
- **관리자 메인**: 대시보드
- **상품 관리**: 도서 등록/수정/삭제
- **주문 관리**: 주문 상태 관리
- **회원 관리**: 회원 정보 조회
- **재고 관리**: 재고 현황 및 수정

## 🔐 보안 기능

- **비밀번호 암호화**: BCrypt를 사용한 비밀번호 해싱
- **세션 관리**: Spring Session을 통한 사용자 세션 관리
- **입력 검증**: Bean Validation을 통한 데이터 검증

## 📊 성능 최적화

- **페이징 처리**: 대용량 데이터의 효율적인 조회
- **지연 로딩**: JPA FetchType.LAZY를 통한 성능 최적화
- **인덱싱**: 데이터베이스 인덱스 최적화

## 🤝 기여 방법

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

⭐ 이 프로젝트가 도움이 되었다면 스타를 눌러주세요!
