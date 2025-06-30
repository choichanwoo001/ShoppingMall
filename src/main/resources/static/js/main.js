// 메인 페이지 JavaScript

// 검색 기능
document.querySelector('.search-btn').addEventListener('click', function() {
    const searchInput = document.querySelector('.search-input');
    const searchTerm = searchInput.value.trim();

    if (searchTerm) {
        // 실제로는 검색 결과 페이지로 이동하거나 AJAX 요청을 보내야 함
        alert(`"${searchTerm}"으로 검색합니다.`);

        // 검색어를 인기 검색어에 추가 (데모용)
        addToPopularSearches(searchTerm);

        // 검색어 초기화
        searchInput.value = '';
    }
});

// 엔터키로 검색
document.querySelector('.search-input').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        document.querySelector('.search-btn').click();
    }
});

// 인기 검색어에 추가 (데모용)
function addToPopularSearches(searchTerm) {
    const popularItems = document.querySelectorAll('.popular-item');
    const lastItem = popularItems[popularItems.length - 1];

    // 새로운 검색어가 이미 있는지 확인
    for (let item of popularItems) {
        if (item.textContent.includes(searchTerm)) {
            return; // 이미 있으면 추가하지 않음
        }
    }

    // 마지막 항목을 새 검색어로 교체
    if (lastItem) {
        const rankNumber = lastItem.querySelector('.rank-number');
        const textNode = lastItem.childNodes[lastItem.childNodes.length - 1];
        textNode.textContent = searchTerm;
    }
}

// 최근 본 상품에 추가
function addToRecentlyViewed(book) {
    const recentBooks = document.querySelector('.sidebar-section:last-child');
    const recentBooksList = recentBooks.querySelectorAll('.recent-book');

    // 이미 있는 책인지 확인
    for (let item of recentBooksList) {
        const title = item.querySelector('.recent-book-title').textContent;
        if (title === book.title) {
            // 이미 있으면 맨 앞으로 이동
            recentBooks.insertBefore(item, recentBooks.children[1]);
            return;
        }
    }

    // 새 책 요소 생성
    const newRecentBook = document.createElement('div');
    newRecentBook.className = 'recent-book';
    newRecentBook.innerHTML = `
        <div class="recent-book-image">이미지</div>
        <div class="recent-book-info">
            <div class="recent-book-title">${book.title}</div>
            <div class="recent-book-author">${book.author}</div>
        </div>
    `;

    // 맨 앞에 추가
    recentBooks.insertBefore(newRecentBook, recentBooks.children[1]);

    // 3개 이상이면 마지막 항목 제거
    const allRecentBooks = recentBooks.querySelectorAll('.recent-book');
    if (allRecentBooks.length > 3) {
        allRecentBooks[allRecentBooks.length - 1].remove();
    }
}

// 책 클릭 시 최근 본 상품에 추가
document.querySelectorAll('.book-card').forEach(card => {
    card.addEventListener('click', function() {
        const title = this.querySelector('.book-title').textContent;
        const author = this.querySelector('.book-author').textContent;

        addToRecentlyViewed({ title, author });

        // 상품 상세 페이지로 이동
        window.location.href = 'product.html';
    });
});

// 카테고리 클릭 시 해당 카테고리 상품 로드 (데모용)
document.querySelectorAll('.category-item').forEach(category => {
    category.addEventListener('click', function(e) {
        e.preventDefault();
        const categoryName = this.textContent;

        // 모든 카테고리에서 active 클래스 제거
        document.querySelectorAll('.category-item').forEach(cat => {
            cat.style.color = '';
            cat.style.borderBottomColor = '';
        });

        // 선택된 카테고리에 스타일 적용
        this.style.color = 'var(--accent-color)';
        this.style.borderBottomColor = 'var(--accent-color)';

        // 실제로는 해당 카테고리의 책들을 로드해야 함
        alert(`${categoryName} 카테고리의 책들을 로드합니다.`);

        // 카테고리별 책 목록 로드 (데모용)
        loadCategoryBooks(categoryName);
    });
});

// 카테고리별 책 목록 로드 (데모용)
function loadCategoryBooks(category) {
    const bookGrid = document.querySelector('.book-grid');
    const title = document.querySelector('.content-left h2');

    title.textContent = `${category} 베스트`;

    // 실제로는 서버에서 해당 카테고리의 책들을 가져와야 함
    // 여기서는 데모용으로 기존 책들의 제목만 변경
    const bookCards = bookGrid.querySelectorAll('.book-card');
    const sampleBooks = {
        '소설/에세이': ['채식주의자', '미움받을 용기', '어린왕자', '데미안', '1984', '코스모스'],
        '인문/사회': ['사피엔스', '총균쇠', '21세기 자본', '정의란 무엇인가', '역사의 연구', '문명의 충돌'],
        '경제/경영': ['부의 추월차선', '주식투자 무작정 따라하기', '경제학 콘서트', '넛지', '상속을 통한 성장', '스타트업'],
        '과학/기술': ['코스모스', '이기적 유전자', '총명한 투자자', 'AI 시대의 미래', '양자역학', '상대성이론'],
        '예술/문화': ['서양미술사', '클래식 음악의 이해', '영화의 이해', '한국의 미', '세계의 건축', '현대미술'],
        '건강/취미': ['운동의 과학', '요리의 기본', '여행의 즐거움', '독서의 기술', '명상과 힐링', '가드닝'],
        '어학/사전': ['영어회화 100일의 기적', '일본어 첫걸음', '중국어 기초', '한영사전', '토익 문법', 'HSK 어휘']
    };

    const books = sampleBooks[category] || sampleBooks['소설/에세이'];

    bookCards.forEach((card, index) => {
        if (books[index]) {
            card.querySelector('.book-title').textContent = books[index];
        }
    });
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 로그인 상태 확인 (데모용)
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';

    if (isLoggedIn) {
        // 로그인 상태일 때 사용자 메뉴 업데이트
        const userMenu = document.querySelector('.user-menu');
        const userName = localStorage.getItem('userName') || '사용자';

        userMenu.innerHTML = `
            <span>안녕하세요, ${userName}님</span>
            <a href="cart.html">장바구니</a>
            <a href="mypage.html">마이페이지</a>
            <a href="#" onclick="logout()">로그아웃</a>
        `;
    }

    // 배너 이미지 슬라이더 (추가 기능 - 실제로는 배너 섹션이 있을 때 사용)
    // initBannerSlider();

    // 실시간 인기 검색어 업데이트 (데모용)
    // setInterval(updatePopularSearches, 30000); // 30초마다 업데이트
});

// 로그아웃 기능
function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        localStorage.removeItem('isLoggedIn');
        localStorage.removeItem('userName');
        location.reload();
    }
}

// 실시간 인기 검색어 업데이트 (데모용)
function updatePopularSearches() {
    const keywords = ['AI', '블록체인', '메타버스', '환경', '건강', '투자', '여행', '요리', '운동', '언어'];
    const popularItems = document.querySelectorAll('.popular-item');

    popularItems.forEach((item, index) => {
        const randomKeyword = keywords[Math.floor(Math.random() * keywords.length)];
        const textNode = item.childNodes[item.childNodes.length - 1];
        if (Math.random() < 0.3) { // 30% 확률로 변경
            textNode.textContent = randomKeyword;
        }
    });
}

// 스크롤 시 헤더 고정 효과
let lastScrollTop = 0;
window.addEventListener('scroll', function() {
    const header = document.querySelector('.header');
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;

    if (scrollTop > lastScrollTop && scrollTop > 100) {
        // 스크롤 다운
        header.style.transform = 'translateY(-100%)';
    } else {
        // 스크롤 업
        header.style.transform = 'translateY(0)';
    }

    lastScrollTop = scrollTop;
});

// 헤더에 transition 효과 추가
document.querySelector('.header').style.transition = 'transform 0.3s ease-in-out';