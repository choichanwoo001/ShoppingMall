// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    setupSearchFunctionality();
    loadPopularKeywords();
    loadRecentViews();
    
    // 주기적으로 최근 본 상품 새로고침 (30초마다)
    setInterval(loadRecentViews, 30000);
});

// 검색 기능 설정
function setupSearchFunctionality() {
    const searchBtn = document.querySelector('.search-btn');
    const searchInput = document.querySelector('.search-input');

    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const searchTerm = searchInput?.value.trim();
            if (searchTerm) {
                window.location.href = `/search?keyword=${encodeURIComponent(searchTerm)}`;
            }
        });
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchBtn?.click();
            }
        });
    }
}

function loadPopularKeywords() {
    fetch('/api/bookspopular-keywords/top?size=5')
        .then(res => res.json())
        .then(list => {
            const ul = document.getElementById('popular-keyword-list');
            if (!ul) return;
            ul.innerHTML = '';
            if (list.length === 0) {
                ul.innerHTML = '<li style="color:#aaa; font-size:0.9rem;">아직 인기 검색어가 없습니다.</li>';
                return;
            }
            list.forEach((keyword, idx) => {
                const li = document.createElement('li');
                li.textContent = `${idx + 1}. ${keyword}`;
                li.style.padding = '0.25rem 0';
                li.style.fontSize = '0.95rem';
                li.style.cursor = 'pointer';
                li.onclick = () => {
                    window.location.href = `/search?keyword=${encodeURIComponent(keyword)}`;
                };
                ul.appendChild(li);
            });
        });
}

// 중분류 카테고리 표시
function showMiddleCategories(topCategoryId) {
    // 모든 중분류 드롭다운 숨기기
    document.querySelectorAll('.middle-dropdown').forEach(dropdown => {
        dropdown.style.display = 'none';
    });

    const middleDropdown = document.getElementById(`middle-${topCategoryId}`);
    if (middleDropdown) {
        middleDropdown.style.display = 'block';
    }
}

// 중분류 카테고리 숨김
function hideMiddleCategories(topCategoryId) {
    const middleDropdown = document.getElementById(`middle-${topCategoryId}`);
    if (middleDropdown) {
        middleDropdown.style.display = 'none';

        // 해당 중분류의 모든 소분류도 숨김
        const bottomDropdowns = middleDropdown.querySelectorAll('.bottom-dropdown');
        bottomDropdowns.forEach(bottom => {
            bottom.style.display = 'none';
        });
    }
}

// 소분류 카테고리 표시
function showBottomCategories(middleCategoryId) {
    // 다른 소분류 드롭다운 숨기기
    document.querySelectorAll('.bottom-dropdown').forEach(dropdown => {
        dropdown.style.display = 'none';
    });

    const bottomDropdown = document.getElementById(`bottom-${middleCategoryId}`);
    if (bottomDropdown) {
        bottomDropdown.style.display = 'block';
    }
}

// 소분류 카테고리 숨김
function hideBottomCategories(middleCategoryId) {
    const bottomDropdown = document.getElementById(`bottom-${middleCategoryId}`);
    if (bottomDropdown) {
        bottomDropdown.style.display = 'none';
    }
}

// 전역 클릭 이벤트로 드롭다운 닫기
document.addEventListener('click', function(e) {
    if (!e.target.closest('.category-dropdown')) {
        document.querySelectorAll('.middle-dropdown, .bottom-dropdown').forEach(dropdown => {
            dropdown.style.display = 'none';
        });
    }
});

// ESC 키로 드롭다운 닫기
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.middle-dropdown, .bottom-dropdown').forEach(dropdown => {
            dropdown.style.display = 'none';
        });
    }
});

// 전역 함수로 노출 (HTML에서 사용)
window.showMiddleCategories = showMiddleCategories;
window.hideMiddleCategories = hideMiddleCategories;
window.showBottomCategories = showBottomCategories;
window.hideBottomCategories = hideBottomCategories;

// 최근 본 상품 동적 로드
function loadRecentViews() {
    const recentViewSection = document.getElementById('recent-view-section');
    const recentViewList = document.getElementById('recent-view-list');
    if (!recentViewSection || !recentViewList) return;

    fetch('/api/recent-views?limit=5')
        .then(res => {
            if (res.status === 401) {
                // 비로그인 시 안내
                recentViewList.innerHTML = '<div class="no-data-small">로그인하시면 최근 본 상품을 확인할 수 있습니다.</div>';
                return null;
            }
            return res.json();
        })
        .then(data => {
            if (!data) return;
            if (!data.success || !Array.isArray(data.data) || data.data.length === 0) {
                recentViewList.innerHTML = '<div class="no-data-small">최근 본 상품이 없습니다.</div>';
                return;
            }
            recentViewList.innerHTML = '';
            data.data.forEach(recent => {
                const book = recent.book;
                const div = document.createElement('div');
                div.className = 'recent-book';
                div.onclick = () => location.href = `/product/${book.bookId}`;
                div.innerHTML = `
                    <div class="recent-book-image">
                        ${book.bookImage ? `<img src="${book.bookImage}" alt="${book.bookName}" style="width:100%;height:100%;object-fit:cover;">` : '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#718096;font-size:0.5rem;">이미지</div>'}
                    </div>
                    <div class="recent-book-info">
                        <div class="recent-book-title">${book.bookName}</div>
                        <div class="recent-book-author">${book.author}</div>
                        <div class="recent-book-price" style="font-size:0.625rem;color:var(--accent-color);font-weight:bold;">${Number(book.price).toLocaleString()}원</div>
                    </div>
                `;
                recentViewList.appendChild(div);
            });
        })
        .catch(() => {
            recentViewList.innerHTML = '<div class="no-data-small">최근 본 상품을 불러올 수 없습니다.</div>';
        });
}