// 전역 변수로 타이머 설정
let categoryHoverTimer = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    setupSearchFunctionality();
    setupSortAndFilter();
    setupDropdownEvents();
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

// 카테고리 호버 처리 함수들
function showMiddleCategories(topCategoryId) {
    // 타이머가 있으면 취소
    if (categoryHoverTimer) {
        clearTimeout(categoryHoverTimer);
        categoryHoverTimer = null;
    }
    
    // 해당 중분류 드롭다운을 복사해서 공통 영역에 표시
    const middleDropdown = document.getElementById(`middle-${topCategoryId}`);
    const dropdownArea = document.querySelector('.category-dropdown-area');
    
    if (middleDropdown && dropdownArea) {
        // 원본 HTML을 복사하되, display 스타일 제거
        let html = middleDropdown.outerHTML;
        html = html.replace(/style="[^"]*"/g, '');
        dropdownArea.innerHTML = html;
        
        // 복사된 요소의 이벤트 재설정
        setupDropdownEvents();
    }
}

function hideMiddleCategories(topCategoryId) {
    // 300ms 후에 숨김 (다른 카테고리로 이동할 시간 확보)
    categoryHoverTimer = setTimeout(() => {
        const dropdownArea = document.querySelector('.category-dropdown-area');
        if (dropdownArea) {
            dropdownArea.innerHTML = '';
        }
    }, 300);
}

function showBottomCategories(middleCategoryId) {
    // 소분류는 이미 CSS로 표시되므로 별도 처리 불필요
}

function hideBottomCategories(middleCategoryId) {
    // 소분류는 이미 CSS로 표시되므로 별도 처리 불필요
}

// 드롭다운 영역의 이벤트 설정
function setupDropdownEvents() {
    const dropdownArea = document.querySelector('.category-dropdown-area');
    
    if (dropdownArea) {
        // 드롭다운 영역에 마우스가 있을 때는 숨기지 않음
        dropdownArea.addEventListener('mouseenter', () => {
            if (categoryHoverTimer) {
                clearTimeout(categoryHoverTimer);
                categoryHoverTimer = null;
            }
        });
        
        // 드롭다운 영역을 벗어나면 숨김
        dropdownArea.addEventListener('mouseleave', () => {
            categoryHoverTimer = setTimeout(() => {
                dropdownArea.innerHTML = '';
            }, 300);
        });
    }
}

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
window.goToPage = goToPage;
window.showMiddleCategories = showMiddleCategories;
window.hideMiddleCategories = hideMiddleCategories;
window.showBottomCategories = showBottomCategories;
window.hideBottomCategories = hideBottomCategories;