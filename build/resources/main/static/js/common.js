// 공통 모달 관련 함수들

// 주문서 모달 열기
function openOrderModal() {
    const modal = document.getElementById('orderModal');
    if (modal) {
        modal.style.display = 'block';
    }
}

// 주문서 모달 닫기
function closeOrderModal() {
    const modal = document.getElementById('orderModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// 알림 모달 열기
function openAlertModal(title, message) {
    const modal = document.getElementById('alertModal');
    const titleElement = document.getElementById('alertTitle');
    const messageElement = document.getElementById('alertMessage');
    
    if (modal && titleElement && messageElement) {
        titleElement.textContent = title || '알림';
        messageElement.textContent = message || '메시지';
        modal.style.display = 'block';
    }
}

// 알림 모달 닫기
function closeAlertModal() {
    const modal = document.getElementById('alertModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// 모달 외부 클릭 시 닫기
window.addEventListener('click', function(event) {
    const orderModal = document.getElementById('orderModal');
    const alertModal = document.getElementById('alertModal');
    
    if (event.target === orderModal) {
        closeOrderModal();
    }
    
    if (event.target === alertModal) {
        closeAlertModal();
    }
});

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeOrderModal();
        closeAlertModal();
    }
});

// 공통 유틸리티 함수들

// 숫자를 천 단위 콤마가 포함된 문자열로 변환
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

// 가격을 원화 형식으로 포맷팅
function formatPrice(price) {
    return formatNumber(price) + '원';
}

// 날짜를 한국 형식으로 포맷팅
function formatDate(date) {
    if (!date) return '';
    
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    
    return `${year}년 ${month}월 ${day}일`;
}

// 별점을 별 문자로 변환
function formatRating(rating) {
    if (!rating) return '☆☆☆☆☆';
    
    const fullStars = Math.floor(rating);
    const emptyStars = 5 - fullStars;
    
    return '★'.repeat(fullStars) + '☆'.repeat(emptyStars);
}

// API 호출 공통 함수
async function apiCall(url, options = {}) {
    const defaultOptions = {
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json',
        }
    };
    
    const finalOptions = { ...defaultOptions, ...options };
    
    try {
        const response = await fetch(url, finalOptions);
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'API 호출에 실패했습니다.');
        }
        
        return data;
    } catch (error) {
        console.error('API 호출 오류:', error);
        throw error;
    }
}

// 로그인 상태 확인
function isLoggedIn() {
    // 세션에서 로그인 상태 확인 (서버에서 전달된 데이터 기반)
    return document.body.dataset.loggedIn === 'true';
}

// 로그인 필요 시 리다이렉트
function requireLogin() {
    if (!isLoggedIn()) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

// 페이지 로드 시 공통 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 검색 기능 초기화
    initializeSearch();
    
    // 카테고리 드롭다운 초기화
    initializeCategoryDropdown();
});

// 검색 기능 초기화
function initializeSearch() {
    const searchBtn = document.querySelector('.search-btn');
    const searchInput = document.querySelector('.search-input');
    
    if (searchBtn && searchInput) {
        // 검색 버튼 클릭
        searchBtn.addEventListener('click', function() {
            const keyword = searchInput.value.trim();
            if (keyword) {
                window.location.href = `/search?keyword=${encodeURIComponent(keyword)}`;
            }
        });
        
        // 엔터 키로 검색
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchBtn.click();
            }
        });
    }
}

// 카테고리 드롭다운 초기화
function initializeCategoryDropdown() {
    let currentDropdown = null;
    let hideTimer = null;
    let isMouseInDropdown = false;
    
    // 모든 카테고리 드롭다운 요소들
    const categoryDropdowns = document.querySelectorAll('.category-dropdown');
    const middleDropdowns = document.querySelectorAll('.middle-dropdown');
    const bottomDropdowns = document.querySelectorAll('.bottom-dropdown');
    const dropdownArea = document.querySelector('.category-dropdown-area');
    
    // 드롭다운 영역 크기 통일
    function normalizeDropdownSizes() {
        middleDropdowns.forEach(dropdown => {
            const items = dropdown.querySelectorAll('.middle-item');
            const itemCount = items.length;
            
            // 하위 카테고리 개수에 따라 간격 조정
            if (itemCount <= 3) {
                dropdown.style.gap = '3rem';
            } else if (itemCount <= 6) {
                dropdown.style.gap = '2rem';
            } else {
                dropdown.style.gap = '1.5rem';
            }
            
            // 최소 높이 보장
            dropdown.style.minHeight = '200px';
        });
    }
    
    // 중분류 표시
    window.showMiddleCategories = function(categoryId) {
        console.log('showMiddleCategories called with:', categoryId);
        
        // 이전 드롭다운 숨기기
        if (currentDropdown && currentDropdown !== categoryId) {
            hideAllDropdowns();
        }
        
        const middleDropdown = document.getElementById('middle-' + categoryId);
        if (middleDropdown) {
            // 모든 중분류 드롭다운 숨기기
            middleDropdowns.forEach(dropdown => {
                dropdown.classList.remove('show');
            });
            
            // 모든 소분류 드롭다운 숨기기
            bottomDropdowns.forEach(dropdown => {
                dropdown.classList.remove('show');
            });
            
            // 현재 드롭다운 표시
            middleDropdown.classList.add('show');
            currentDropdown = categoryId;
            
            // 드롭다운 영역 확장
            if (dropdownArea) {
                dropdownArea.classList.add('expanded');
            }
            
            // 타이머 초기화
            clearTimeout(hideTimer);
        }
    };
    
    // 중분류 숨기기 (지연)
    window.hideMiddleCategories = function(categoryId) {
        console.log('hideMiddleCategories called with:', categoryId);
        
        // 마우스가 드롭다운 영역에 있는지 확인
        if (isMouseInDropdown) {
            return;
        }
        
        hideTimer = setTimeout(() => {
            if (currentDropdown === categoryId) {
                hideAllDropdowns();
            }
        }, 200); // 200ms 지연
    };
    
    // 소분류 표시
    window.showBottomCategories = function(middleCategoryId) {
        console.log('showBottomCategories called with:', middleCategoryId);
        
        const bottomDropdown = document.getElementById('bottom-' + middleCategoryId);
        if (bottomDropdown) {
            // 모든 소분류 드롭다운 숨기기
            bottomDropdowns.forEach(dropdown => {
                dropdown.classList.remove('show');
            });
            
            // 현재 소분류 드롭다운 표시
            bottomDropdown.classList.add('show');
            
            // 타이머 초기화
            clearTimeout(hideTimer);
        }
    };
    
    // 소분류 숨기기 (지연)
    window.hideBottomCategories = function(middleCategoryId) {
        console.log('hideBottomCategories called with:', middleCategoryId);
        
        // 마우스가 드롭다운 영역에 있는지 확인
        if (isMouseInDropdown) {
            return;
        }
        
        hideTimer = setTimeout(() => {
            const bottomDropdown = document.getElementById('bottom-' + middleCategoryId);
            if (bottomDropdown) {
                bottomDropdown.classList.remove('show');
            }
        }, 150); // 150ms 지연
    };
    
    // 모든 드롭다운 숨기기
    function hideAllDropdowns() {
        middleDropdowns.forEach(dropdown => {
            dropdown.classList.remove('show');
        });
        
        bottomDropdowns.forEach(dropdown => {
            dropdown.classList.remove('show');
        });
        
        if (dropdownArea) {
            dropdownArea.classList.remove('expanded');
        }
        
        currentDropdown = null;
    }
    
    // 드롭다운 영역 마우스 이벤트
    if (dropdownArea) {
        dropdownArea.addEventListener('mouseenter', () => {
            isMouseInDropdown = true;
            clearTimeout(hideTimer);
        });
        
        dropdownArea.addEventListener('mouseleave', () => {
            isMouseInDropdown = false;
            hideTimer = setTimeout(() => {
                hideAllDropdowns();
            }, 200);
        });
    }
    
    // 카테고리 네비게이션 영역 마우스 이벤트
    const categoryNav = document.querySelector('.category-nav');
    if (categoryNav) {
        categoryNav.addEventListener('mouseleave', () => {
            isMouseInDropdown = false;
            hideTimer = setTimeout(() => {
                hideAllDropdowns();
            }, 100);
        });
    }
    
    // ESC 키로 드롭다운 닫기
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            hideAllDropdowns();
        }
    });
    
    // 페이지 클릭 시 드롭다운 닫기
    document.addEventListener('click', (event) => {
        if (!event.target.closest('.category-nav')) {
            hideAllDropdowns();
        }
    });
    
    // 현재 활성 카테고리 표시
    function highlightCurrentCategory() {
        const currentPath = window.location.pathname;
        const categoryMatch = currentPath.match(/\/category\/(\d+)/);
        
        if (categoryMatch) {
            const categoryId = categoryMatch[1];
            
            // 대분류 아이템에서 활성 상태 제거
            document.querySelectorAll('.category-item').forEach(item => {
                item.classList.remove('active');
            });
            
            // 중분류 링크에서 활성 상태 제거
            document.querySelectorAll('.middle-link').forEach(link => {
                link.classList.remove('active');
            });
            
            // 소분류 링크에서 활성 상태 제거
            document.querySelectorAll('.bottom-link').forEach(link => {
                link.classList.remove('active');
            });
            
            // 현재 카테고리 활성화
            const currentCategoryLink = document.querySelector(`[href="/category/${categoryId}"]`);
            if (currentCategoryLink) {
                currentCategoryLink.classList.add('active');
                
                // 부모 카테고리들도 활성화
                const parentDropdown = currentCategoryLink.closest('.category-dropdown');
                if (parentDropdown) {
                    const parentCategoryItem = parentDropdown.querySelector('.category-item');
                    if (parentCategoryItem) {
                        parentCategoryItem.classList.add('active');
                    }
                }
            }
        }
    }
    
    // 초기화 실행
    normalizeDropdownSizes();
    highlightCurrentCategory();
    
    console.log('Category dropdown initialized');
} 