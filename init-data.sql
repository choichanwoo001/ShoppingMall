-- FastCampus BookStore DB 초기화용 샘플 데이터

-- 1. 관리자 데이터 (1개)
INSERT INTO Admins (admin_id, admin_name, email, password, role, created_at, updated_at) VALUES
('admin001', '관리자', 'admin@bookstore.com', 'password', '슈퍼관리자', NOW(), NOW());

-- 2. 회원 데이터 (1개 - Choi3495)
INSERT INTO Members (member_id, member_name, email, phone, address, password, member_grade, member_status, join_date, last_login_date, created_at, updated_at) VALUES
('Choi3495', '최독서', 'choi3495@email.com', '010-3495-3495', '서울시 강남구 테헤란로 123', 'choi3495!', 'VIP', '활성', '2023-01-15 10:30:00', '2024-12-20 14:20:00', NOW(), NOW());

-- 3. 대분류 (5개)
INSERT INTO Categories_Top (category_name, sort_order, is_active, created_at, updated_at) VALUES
('소설', 1, TRUE, NOW(), NOW()),
('인문사회', 2, TRUE, NOW(), NOW()),
('과학기술', 3, TRUE, NOW(), NOW()),
('경제경영', 4, TRUE, NOW(), NOW()),
('기타', 5, TRUE, NOW(), NOW());

-- 4. 중분류 (각 대분류당 2-3개씩, 총 12개)
INSERT INTO Categories_Middle (category_id2, category_name, sort_order, is_active, created_at, updated_at) VALUES
-- 소설 (3개)
(1, '한국소설', 1, TRUE, NOW(), NOW()),
(1, '외국소설', 2, TRUE, NOW(), NOW()),
(1, '에세이', 3, TRUE, NOW(), NOW()),

-- 인문사회 (3개)
(2, '철학', 1, TRUE, NOW(), NOW()),
(2, '역사', 2, TRUE, NOW(), NOW()),
(2, '정치사회', 3, TRUE, NOW(), NOW()),

-- 과학기술 (2개)
(3, '과학', 1, TRUE, NOW(), NOW()),
(3, 'IT컴퓨터', 2, TRUE, NOW(), NOW()),

-- 경제경영 (2개)
(4, '경제', 1, TRUE, NOW(), NOW()),
(4, '경영', 2, TRUE, NOW(), NOW()),

-- 기타 (2개)
(5, '자기계발', 1, TRUE, NOW(), NOW()),
(5, '취미실용', 2, TRUE, NOW(), NOW());

-- 5. 소분류 (각 중분류당 2개씩, 총 24개)
INSERT INTO Categories_Bottom (category_id2, category_name, sort_order, is_active, created_at, updated_at) VALUES
-- 한국소설
(1, '현대소설', 1, TRUE, NOW(), NOW()),
(1, '고전소설', 2, TRUE, NOW(), NOW()),

-- 외국소설
(2, '영미소설', 1, TRUE, NOW(), NOW()),
(2, '기타외국소설', 2, TRUE, NOW(), NOW()),

-- 에세이
(3, '국내에세이', 1, TRUE, NOW(), NOW()),
(3, '해외에세이', 2, TRUE, NOW(), NOW()),

-- 철학
(4, '동양철학', 1, TRUE, NOW(), NOW()),
(4, '서양철학', 2, TRUE, NOW(), NOW()),

-- 역사
(5, '한국사', 1, TRUE, NOW(), NOW()),
(5, '세계사', 2, TRUE, NOW(), NOW()),

-- 정치사회
(6, '정치', 1, TRUE, NOW(), NOW()),
(6, '사회', 2, TRUE, NOW(), NOW()),

-- 과학
(7, '자연과학', 1, TRUE, NOW(), NOW()),
(7, '의학', 2, TRUE, NOW(), NOW()),

-- IT컴퓨터
(8, '프로그래밍', 1, TRUE, NOW(), NOW()),
(8, '컴퓨터일반', 2, TRUE, NOW(), NOW()),

-- 경제
(9, '경제이론', 1, TRUE, NOW(), NOW()),
(9, '경제실무', 2, TRUE, NOW(), NOW()),

-- 경영
(10, '경영전략', 1, TRUE, NOW(), NOW()),
(10, '마케팅', 2, TRUE, NOW(), NOW()),

-- 자기계발
(11, '성공학', 1, TRUE, NOW(), NOW()),
(11, '인간관계', 2, TRUE, NOW(), NOW()),

-- 취미실용
(12, '요리', 1, TRUE, NOW(), NOW()),
(12, '여행', 2, TRUE, NOW(), NOW());

-- (이하 도서, 재고, 장바구니, 주문, 주문상세, 리뷰, 베스트셀러 등 전체 쿼리)
-- ... (사용자가 제공한 전체 INSERT 쿼리 붙여넣기) 