// API 기본 설정
const API_BASE_URL = '/api';

// 현재 상품 정보
let currentBook = null;
let currentBookId = window.currentBookId; // HTML에서 전달받은 책 ID

// 세션 기반 인증 확인
async function checkLoginStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}/members/login-status`, {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const result = await response.json();
            return result.data;
        }
        return { isLoggedIn: false };
    } catch (error) {
        console.error('로그인 상태 확인 실패:', error);
        return { isLoggedIn: false };
    }
}

// API 요청 헬퍼 함수
async function apiRequest(url, options = {}) {
    const defaultOptions = {
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json'
        }
    };

    try {
        const response = await fetch(`${API_BASE_URL}${url}`, {
            ...defaultOptions,
            ...options,
            headers: { ...defaultOptions.headers, ...options.headers }
        });

        const result = await response.json();

        if (!result.success) {
            throw new Error(result.message || '요청 처리 중 오류가 발생했습니다.');
        }

        return result.data;
    } catch (error) {
        console.error('API 요청 실패:', error);
        throw error;
    }
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', async function() {
    if (!currentBookId) {
        console.error('상품 ID가 없습니다.');
        return;
    }

    await loadBookDetailFromAPI();
    await addToRecentViews();
    setupEventListeners();
});

// API에서 상품 상세 정보 로드
async function loadBookDetailFromAPI() {
    try {
        const bookDetail = await apiRequest(`/books/${currentBookId}`);
        currentBook = bookDetail;
        updatePageWithBookData(bookDetail);

        console.log('상품 정보 로드 완료:', bookDetail.bookName);
    } catch (error) {
        console.error('상품 정보 로드 실패:', error);
        showError('상품 정보를 불러올 수 없습니다.');
    }
}

// 페이지를 API에서 받은 데이터로 업데이트
function updatePageWithBookData(book) {
    // 페이지 제목 업데이트
    document.title = `${book.bookName} - BookStore`;

    // 상품 이미지 업데이트
    const productImage = document.querySelector('.product-image');
    if (productImage && book.bookImage) {
        productImage.innerHTML = `<img src="${book.bookImage}" alt="${book.bookName}" style="width: 100%; height: 100%; object-fit: cover;">`;
    }

    // 재고 정보에 따른 버튼 상태 업데이트
    updatePurchaseButtons(book);

    // 수량 선택기 최대값 설정
    const quantityInput = document.querySelector('.quantity-input');
    if (quantityInput && book.stockQuantity) {
        quantityInput.max = book.stockQuantity;
    }
}

// 장바구니에 추가
async function addToCart() {
    const loginStatus = await checkLoginStatus();

    if (!loginStatus.isLoggedIn) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    if (!currentBook) {
        showError('상품 정보를 불러올 수 없습니다.');
        return;
    }

    // 수량 확인
    const quantity = parseInt(document.querySelector('.quantity-input')?.value || '1');

    if (quantity <= 0) {
        alert('수량을 올바르게 입력해주세요.');
        return;
    }

    if (currentBook.stockQuantity && quantity > currentBook.stockQuantity) {
        alert(`재고가 부족합니다. (현재 재고: ${currentBook.stockQuantity}개)`);
        return;
    }

    try {
        await apiRequest('/cart', {
            method: 'POST',
            body: JSON.stringify({
                bookId: currentBookId,
                quantity: quantity
            })
        });

        if (confirm('장바구니에 추가되었습니다. 장바구니로 이동하시겠습니까?')) {
            window.location.href = '/cart';
        }
    } catch (error) {
        console.error('장바구니 추가 실패:', error);
        showError(error.message || '장바구니 추가 중 오류가 발생했습니다.');
    }
}

// 바로 구매
async function buyNow() {
    const loginStatus = await checkLoginStatus();

    if (!loginStatus.isLoggedIn) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    if (!currentBook) {
        showError('상품 정보를 불러올 수 없습니다.');
        return;
    }

    // 수량 확인
    const quantity = parseInt(document.querySelector('.quantity-input')?.value || '1');

    if (quantity <= 0) {
        alert('수량을 올바르게 입력해주세요.');
        return;
    }

    if (currentBook.stockQuantity && quantity > currentBook.stockQuantity) {
        alert(`재고가 부족합니다. (현재 재고: ${currentBook.stockQuantity}개)`);
        return;
    }

    // 주문서 모달에 상품 정보 채우기 (장바구니와 동일 UX)
    const orderModal = document.getElementById('orderModal');
    if (!orderModal) {
        alert('주문서 모달을 찾을 수 없습니다.');
        return;
    }

    updateOrderModalSummary();
    orderModal.style.display = 'flex';
}

// 리뷰 작성
async function writeReview() {
    const loginStatus = await checkLoginStatus();

    if (!loginStatus.isLoggedIn) {
        alert('리뷰 작성을 위해 로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    // 리뷰 작성 페이지로 이동
    window.location.href = `/review/write?bookId=${currentBookId}`;
}

// 최근 본 상품에 추가
async function addToRecentViews() {
    const loginStatus = await checkLoginStatus();

    if (!loginStatus.isLoggedIn) {
        return; // 로그인하지 않은 경우 무시
    }

    try {
        await apiRequest('/api/recent-views', {
            method: 'POST',
            body: JSON.stringify({ bookId: currentBookId })
        });
    } catch (error) {
        // 최근 본 상품 추가 실패는 무시 (핵심 기능이 아니므로)
        console.error('최근 본 상품 추가 실패:', error);
    }
}

// 리뷰 페이지네이션
async function loadBookReviews(page = 0) {
    try {
        // 현재 페이지를 새로고침하여 리뷰 페이지를 변경
        const url = new URL(window.location);
        url.searchParams.set('reviewPage', page);
        window.location.href = url.toString();
    } catch (error) {
        console.error('리뷰 페이지 로드 실패:', error);
        showError('리뷰를 불러올 수 없습니다.');
    }
}

// 구매 버튼 상태 업데이트
function updatePurchaseButtons(book) {
    const addToCartBtn = document.querySelector('.add-to-cart-btn');
    const buyNowBtn = document.querySelector('.buy-now-btn');

    const isAvailable = book.salesStatus === '판매중' &&
        (book.stockQuantity === undefined || book.stockQuantity > 0);

    if (addToCartBtn) {
        addToCartBtn.disabled = !isAvailable;
        addToCartBtn.textContent = isAvailable ? '장바구니' : '구매 불가';
        if (!isAvailable) {
            addToCartBtn.style.opacity = '0.5';
            addToCartBtn.style.cursor = 'not-allowed';
        }
    }

    if (buyNowBtn) {
        buyNowBtn.disabled = !isAvailable;
        buyNowBtn.textContent = isAvailable ? '바로구매' : '구매 불가';
        if (!isAvailable) {
            buyNowBtn.style.opacity = '0.5';
            buyNowBtn.style.cursor = 'not-allowed';
        }
    }
}

// 에러 메시지 표시
function showError(message) {
    alert(message);
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 장바구니 담기 버튼
    const addToCartBtn = document.querySelector('.add-to-cart-btn');
    if (addToCartBtn) {
        addToCartBtn.addEventListener('click', addToCart);
    }

    // 바로 구매 버튼
    const buyNowBtn = document.querySelector('.buy-now-btn');
    if (buyNowBtn) {
        buyNowBtn.addEventListener('click', buyNow);
    }

    // 리뷰 작성 버튼
    const writeReviewBtn = document.querySelector('.write-review-btn');
    if (writeReviewBtn) {
        writeReviewBtn.addEventListener('click', writeReview);
    }

    // 수량 조절 버튼
    setupQuantityControls();

    // 검색 기능
    setupSearchFunctionality();
}

// 수량 조절 기능 설정
function setupQuantityControls() {
    const quantityInput = document.querySelector('.quantity-input');
    const increaseBtn = document.querySelector('.quantity-increase');
    const decreaseBtn = document.querySelector('.quantity-decrease');

    if (quantityInput && increaseBtn && decreaseBtn) {
        increaseBtn.addEventListener('click', function() {
            const currentValue = parseInt(quantityInput.value || '1');
            const maxQuantity = currentBook?.stockQuantity || 999;
            if (currentValue < maxQuantity) {
                quantityInput.value = currentValue + 1;
                updateOrderModalSummary();
            } else {
                alert(`최대 ${maxQuantity}개까지 구매 가능합니다.`);
            }
        });

        decreaseBtn.addEventListener('click', function() {
            const currentValue = parseInt(quantityInput.value || '1');
            if (currentValue > 1) {
                quantityInput.value = currentValue - 1;
                updateOrderModalSummary();
            }
        });

        quantityInput.addEventListener('change', function() {
            const value = parseInt(this.value);
            const maxQuantity = currentBook?.stockQuantity || 999;

            if (isNaN(value) || value < 1) {
                this.value = 1;
            } else if (value > maxQuantity) {
                this.value = maxQuantity;
                alert(`최대 ${maxQuantity}개까지 구매 가능합니다.`);
            }
            updateOrderModalSummary();
        });
    }
}

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

// 전역 함수로 노출 (HTML에서 호출할 수 있도록)
window.addToCart = addToCart;
window.buyNow = buyNow;
window.writeReview = writeReview;
window.loadBookReviews = loadBookReviews;

function updateOrderModalSummary() {
    const quantity = parseInt(document.querySelector('.quantity-input')?.value || '1');
    if (!currentBook) return;
    // 상품 정보
    const orderItemsDiv = document.getElementById('orderItems');
    if (orderItemsDiv) {
        orderItemsDiv.innerHTML = `
            <div class="order-item-row">
                <img src="${currentBook.bookImage || ''}" alt="${currentBook.bookName}" class="order-item-img">
                <div class="order-item-info">
                    <div class="order-item-title">${currentBook.bookName}</div>
                    <div class="order-item-qty">수량: ${quantity}개</div>
                    <div class="order-item-price">${formatPrice(currentBook.price)}원</div>
                </div>
            </div>
        `;
    }
    // 총 결제금액
    const orderTotalDiv = document.getElementById('orderTotal');
    if (orderTotalDiv) {
        orderTotalDiv.textContent = formatPrice(currentBook.price * quantity) + '원';
    }
}