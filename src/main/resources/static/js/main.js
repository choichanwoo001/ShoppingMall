// 검색 기능
document.querySelector('.search-btn').addEventListener('click', async function() {
    const searchInput = document.querySelector('.search-input');
    const searchTerm = searchInput.value.trim();

    if (searchTerm) {
        try {
            // 책 검색 API 호출
            const response = await fetch(`/api/books?keyword=${encodeURIComponent(searchTerm)}&page=0&size=30`);
            const data = await response.json();

            if (response.ok && data.success) {
                // 검색 결과로 책 목록 업데이트
                updateBookGrid(data.data.content);

                // 페이지 제목 변경
                const title = document.querySelector('.content-left h2');
                if (title) {
                    title.textContent = `"${searchTerm}" 검색 결과`;
                }
            }
        } catch (error) {
            console.error('검색 오류:', error);
            alert('검색 중 오류가 발생했습니다.');
        }

        searchInput.value = '';
    }
});

// 엔터키로 검색
document.querySelector('.search-input').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        document.querySelector('.search-btn').click();
    }
});

// 책 목록 업데이트
function updateBookGrid(books) {
    const bookGrid = document.querySelector('.book-grid');
    if (!bookGrid || !books) return;

    bookGrid.innerHTML = books.map(book => `
        <div class="book-card" onclick="goToProductDetail(${book.bookId})">
            <div class="book-image">${book.bookImage || '책 이미지'}</div>
            <div class="book-info">
                <div class="book-title">${book.bookName}</div>
                <div class="book-author">${book.author}</div>
                <div class="book-price">${book.price ? book.price.toLocaleString() + '원' : '가격 정보 없음'}</div>
            </div>
        </div>
    `).join('');
}

// 상품 상세 페이지로 이동
function goToProductDetail(bookId) {
    // 최근 본 상품에 추가
    addToRecentViews(bookId);
    window.location.href = `/product?id=${bookId}`;
}

// 최근 본 상품에 추가
async function addToRecentViews(bookId) {
    try {
        await fetch('/api/recent-views', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({ bookId: bookId })
        });
    } catch (error) {
        console.error('최근 본 상품 추가 오류:', error);
    }
}

// 카테고리 클릭 시 해당 카테고리 상품 로드
document.querySelectorAll('.category-item').forEach(category => {
    category.addEventListener('click', async function(e) {
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

        // 카테고리별 책 목록 로드
        await loadCategoryBooks(categoryName);
    });
});

// 카테고리별 책 목록 로드
async function loadCategoryBooks(categoryName) {
    try {
        // 실제로는 카테고리 ID를 먼저 조회해야 함
        const categoriesResponse = await fetch('/api/categories');
        const categoriesData = await categoriesResponse.json();

        if (categoriesData.success) {
            // 카테고리 이름으로 ID 찾기 (간단 구현)
            const categoryId = findCategoryId(categoriesData.data.categories, categoryName);

            if (categoryId) {
                // 카테고리별 책 조회
                const booksResponse = await fetch(`/api/books/category/${categoryId}?page=0&size=30`);
                const booksData = await booksResponse.json();

                if (booksData.success) {
                    updateBookGrid(booksData.data.content);

                    const title = document.querySelector('.content-left h2');
                    if (title) {
                        title.textContent = `${categoryName} 베스트`;
                    }
                }
            }
        }
    } catch (error) {
        console.error('카테고리 로드 오류:', error);
        alert('카테고리 로드 중 오류가 발생했습니다.');
    }
}

// 카테고리 ID 찾기 헬퍼 함수
function findCategoryId(categories, categoryName) {
    for (let topCategory of categories) {
        if (topCategory.categoryName === categoryName) {
            return topCategory.categoryId;
        }
        if (topCategory.subCategories) {
            for (let middleCategory of topCategory.subCategories) {
                if (middleCategory.categoryName === categoryName) {
                    return middleCategory.categoryId;
                }
                if (middleCategory.subCategories) {
                    for (let bottomCategory of middleCategory.subCategories) {
                        if (bottomCategory.categoryName === categoryName) {
                            return bottomCategory.categoryId;
                        }
                    }
                }
            }
        }
    }
    return null;
}

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', async function() {
    // 로그인 상태 확인
    await checkLoginStatus();

    // 베스트셀러 로드
    await loadBestsellers();
});

// 로그인 상태 확인
async function checkLoginStatus() {
    try {
        const response = await fetch('/api/members/login-status', {
            credentials: 'include'
        });
        const data = await response.json();

        if (data.success && data.data.isLoggedIn) {
            // 로그인 상태일 때 사용자 메뉴 업데이트
            const userMenu = document.querySelector('.user-menu');
            const member = data.data.member;

            userMenu.innerHTML = `
                <span>안녕하세요, ${member.memberName}님</span>
                <a href="/cart">장바구니</a>
                <a href="/mypage">마이페이지</a>
                <a href="#" onclick="logout()">로그아웃</a>
            `;
        }
    } catch (error) {
        console.error('로그인 상태 확인 오류:', error);
    }
}

// 베스트셀러 로드
async function loadBestsellers() {
    try {
        const response = await fetch('/api/books/bestsellers');
        const data = await response.json();

        if (data.success && data.data.length > 0) {
            // 베스트셀러 데이터를 책 목록 형태로 변환
            const books = data.data.map(bestseller => ({
                bookId: bestseller.bookId,
                bookName: bestseller.bookName,
                author: bestseller.author,
                publisher: bestseller.publisher,
                price: bestseller.price,
                bookImage: bestseller.bookImage
            }));

            updateBookGrid(books);
        }
    } catch (error) {
        console.error('베스트셀러 로드 오류:', error);
    }
}

// 로그아웃 기능
async function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        try {
            const response = await fetch('/api/members/logout', {
                method: 'POST',
                credentials: 'include'
            });

            if (response.ok) {
                location.reload();
            }
        } catch (error) {
            console.error('로그아웃 오류:', error);
            alert('로그아웃 중 오류가 발생했습니다.');
        }
    }
}