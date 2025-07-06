// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    setupSearchFunctionality();
    setupSortAndFilter();
});

// 검색 기능 설정
function setupSearchFunctionality() {
    const searchBtn = document.querySelector('.search-btn');
    const searchInput = document.querySelector('.search-input');

    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            performSearch();
        });
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
    }
}

// 검색 실행
function performSearch() {
    const searchInput = document.querySelector('.search-input');
    const searchTerm = searchInput?.value.trim();

    // 현재 URL에서 파라미터 추출
    const urlParams = new URLSearchParams(window.location.search);
    const currentCategoryId = getCurrentCategoryId();
    const currentSort = urlParams.get('sort') || 'registration_date';
    const currentDirection = urlParams.get('direction') || 'desc';
    const currentSize = urlParams.get('size') || '30';

    // 새 URL 생성
    let newUrl = `/category/${currentCategoryId}?page=0&size=${currentSize}&sort=${currentSort}&direction=${currentDirection}`;

    if (searchTerm) {
        newUrl += `&keyword=${encodeURIComponent(searchTerm)}`;
    }

    window.location.href = newUrl;
}

// 현재 카테고리 ID 추출
function getCurrentCategoryId() {
    const pathParts = window.location.pathname.split('/');
    return pathParts[pathParts.length - 1];
}

// 정렬 및 필터 설정
function setupSortAndFilter() {
    const sortSelect = document.getElementById('sort-select');
    const sizeSelect = document.getElementById('size-select');

    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            applyFilters();
        });
    }

    if (sizeSelect) {
        sizeSelect.addEventListener('change', function() {
            applyFilters();
        });
    }
}

// 필터 적용
function applyFilters() {
    const sortSelect = document.getElementById('sort-select');
    const sizeSelect = document.getElementById('size-select');
    const searchInput = document.querySelector('.search-input');

    const sortValue = sortSelect?.value || 'registration_date-desc';
    const [sort, direction] = sortValue.split('-');
    const size = sizeSelect?.value || '30';
    const keyword = searchInput?.value.trim();

    const currentCategoryId = getCurrentCategoryId();

    // 새 URL 생성
    let newUrl = `/category/${currentCategoryId}?page=0&size=${size}&sort=${sort}&direction=${direction}`;

    if (keyword) {
        newUrl += `&keyword=${encodeURIComponent(keyword)}`;
    }

    window.location.href = newUrl;
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

// 페이지네이션 기능
function goToPage(page) {
    const urlParams = new URLSearchParams(window.location.search);
    const currentCategoryId = getCurrentCategoryId();
    const sort = urlParams.get('sort') || 'registration_date';
    const direction = urlParams.get('direction') || 'desc';
    const size = urlParams.get('size') || '30';
    const keyword = urlParams.get('keyword');

    let newUrl = `/category/${currentCategoryId}?page=${page}&size=${size}&sort=${sort}&direction=${direction}`;

    if (keyword) {
        newUrl += `&keyword=${encodeURIComponent(keyword)}`;
    }

    window.location.href = newUrl;
}

// 전역 함수로 노출 (HTML에서 사용)
window.showMiddleCategories = showMiddleCategories;
window.hideMiddleCategories = hideMiddleCategories;
window.showBottomCategories = showBottomCategories;
window.hideBottomCategories = hideBottomCategories;
window.goToPage = goToPage;