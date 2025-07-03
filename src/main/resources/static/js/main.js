// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    setupSearchFunctionality();
});

// 검색 기능 설정
function setupSearchFunctionality() {
    const searchBtn = document.querySelector('.search-btn');
    const searchInput = document.querySelector('.search-input');

    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const searchTerm = searchInput?.value.trim();
            if (searchTerm) {
                window.location.href = `/?search=${encodeURIComponent(searchTerm)}`;
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