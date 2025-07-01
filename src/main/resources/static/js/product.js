// API 기본 설정
const API_BASE_URL = '/api';

// 현재 상품 정보
let currentBook = null;
let currentBookId = null;

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
    // URL에서 bookId 파라미터 추출
    const urlParams = new URLSearchParams(window.location.search);
    currentBookId = urlParams.get('bookId');

    if (!currentBookId) {
        alert('상품 정보를 찾을 수 없습니다.');
        window.location.href = '/';
        return;
    }

    await loadBookDetail();
    await loadBookReviews();
    setupEventListeners();
});

// 상품 상세 정보 로드
async function loadBookDetail() {
    try {
        const bookDetail = await apiRequest(`/books/${currentBookId}`);
        currentBook = bookDetail;
        displayBookDetail(bookDetail);

        // 최근 본 상품에 추가 (로그인한 경우)
        const loginStatus = await checkLoginStatus();
        if (loginStatus.isLoggedIn) {
            await addToRecentViews(currentBookId);
        }
    } catch (error) {
        console.error('상품 정보 로드 실패:', error);
        showError('상품 정보를 불러올 수 없습니다.');
    }
}

// 상품 상세 정보 표시
function displayBookDetail(book) {
    // 상품 이미지
    const bookImage = document.querySelector('.book-image img');
    if (bookImage) {
        bookImage.src = book.bookImage || '/images/default-book.jpg';
        bookImage.alt = book.bookName;
    }

    // 상품 기본 정보
    const bookTitle = document.querySelector('.book-title');
    if (bookTitle) bookTitle.textContent = book.bookName;

    const bookAuthor = document.querySelector('.book-author');
    if (bookAuthor) bookAuthor.textContent = `저자: ${book.author}`;

    const bookPublisher = document.querySelector('.book-publisher');
    if (bookPublisher) bookPublisher.textContent = `출판사: ${book.publisher}`;

    const bookPrice = document.querySelector('.book-price');
    if (bookPrice) bookPrice.textContent = `${book.price?.toLocaleString()}원`;

    // 평점 표시
    const bookRating = document.querySelector('.book-rating');
    if (bookRating && book.rating) {
        const rating = parseFloat(book.rating);
        bookRating.innerHTML = `
            <div class="rating-stars">
                ${'★'.repeat(Math.floor(rating))}${'☆'.repeat(5 - Math.floor(rating))}
                <span class="rating-text">(${rating.toFixed(1)}/5)</span>
            </div>
        `;
    }

    // 판매 상태
    const salesStatus = document.querySelector('.sales-status');
    if (salesStatus) {
        salesStatus.textContent = book.salesStatus;
        salesStatus.className = `sales-status status-${book.salesStatus}`;
    }

    // 재고 정보
    const stockInfo = document.querySelector('.stock-info');
    if (stockInfo && book.stockQuantity !== undefined) {
        if (book.stockQuantity > 0) {
            stockInfo.textContent = `재고: ${book.stockQuantity}권`;
            stockInfo.className = 'stock-info in-stock';
        } else {
            stockInfo.textContent = '재고 없음';
            stockInfo.className = 'stock-info out-of-stock';
        }
    }

    // 상품 설명
    const bookDescription = document.querySelector('.book-description');
    if (bookDescription && book.description) {
        bookDescription.innerHTML = book.description.replace(/\n/g, '<br>');
    }

    // 상품 상세 정보
    const bookDetails = document.querySelector('.book-details');
    if (bookDetails) {
        bookDetails.innerHTML = `
            <div class="detail-item">
                <span class="label">ISBN:</span>
                <span class="value">${book.isbn || '정보 없음'}</span>
            </div>
            <div class="detail-item">
                <span class="label">크기:</span>
                <span class="value">${book.bookSize || '정보 없음'}</span>
            </div>
            <div class="detail-item">
                <span class="label">등록일:</span>
                <span class="value">${formatDate(book.registrationDate)}</span>
            </div>
            ${book.category ? `
                <div class="detail-item">
                    <span class="label">카테고리:</span>
                    <span class="value">${getCategoryPath(book.category)}</span>
                </div>
            ` : ''}
        `;
    }

    // 구매 버튼 상태 업데이트
    updatePurchaseButtons(book);
}

// 상품 리뷰 로드
async function loadBookReviews(page = 0, size = 5) {
    try {
        const reviewsData = await apiRequest(`/books/${currentBookId}/reviews?page=${page}&size=${size}`);
        displayBookReviews(reviewsData);
    } catch (error) {
        console.error('리뷰 로드 실패:', error);
        // 리뷰 로드 실패는 상품 정보 표시에 영향을 주지 않도록 함
    }
}

// 리뷰 표시
function displayBookReviews(reviewsData) {
    const reviewsSection = document.querySelector('.reviews-section');
    if (!reviewsSection) return;

    let reviewsHtml = '<h3>상품 리뷰</h3>';

    if (reviewsData.content && reviewsData.content.length > 0) {
        // 리뷰 요약
        if (reviewsData.totalElements > 0) {
            reviewsHtml += `
                <div class="reviews-summary">
                    <p>총 ${reviewsData.totalElements}개의 리뷰</p>
                </div>
            `;
        }

        // 리뷰 목록
        reviewsHtml += '<div class="reviews-list">';
        reviewsData.content.forEach(review => {
            reviewsHtml += `
                <div class="review-item">
                    <div class="review-header">
                        <div class="reviewer-info">
                            <span class="reviewer-name">${review.reviewer?.memberName || '익명'}</span>
                            <span class="reviewer-grade">${review.reviewer?.memberGrade || ''}</span>
                        </div>
                        <div class="review-rating">
                            ${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}
                        </div>
                        <div class="review-date">${formatDate(review.createdAt)}</div>
                    </div>
                    <div class="review-content">
                        <h5 class="review-title">${review.reviewTitle}</h5>
                        <p class="review-text">${review.reviewContent}</p>
                        ${review.isRecommended ? '<span class="recommended-badge">추천</span>' : ''}
                    </div>
                </div>
            `;
        });
        reviewsHtml += '</div>';

        // 더 보기 버튼
        if (reviewsData.hasNext) {
            reviewsHtml += `
                <div class="reviews-actions">
                    <button onclick="loadMoreReviews()" class="btn btn-secondary">리뷰 더보기</button>
                </div>
            `;
        }
    } else {
        reviewsHtml += '<p class="no-reviews">아직 리뷰가 없습니다. 첫 번째 리뷰를 작성해보세요!</p>';
    }

    reviewsSection.innerHTML = reviewsHtml;
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

    // 판매 상태 확인
    if (currentBook.salesStatus !== '판매중') {
        alert('현재 판매하지 않는 상품입니다.');
        return;
    }

    // 재고 확인
    if (currentBook.stockQuantity !== undefined && currentBook.stockQuantity <= 0) {
        alert('재고가 부족합니다.');
        return;
    }

    // 수량 입력 (기본 1개)
    const quantity = parseInt(document.querySelector('.quantity-input')?.value || '1');

    if (quantity <= 0) {
        alert('수량을 올바르게 입력해주세요.');
        return;
    }

    if (currentBook.stockQuantity !== undefined && quantity > currentBook.stockQuantity) {
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

    // 판매 상태 확인
    if (currentBook.salesStatus !== '판매중') {
        alert('현재 판매하지 않는 상품입니다.');
        return;
    }

    // 재고 확인
    if (currentBook.stockQuantity !== undefined && currentBook.stockQuantity <= 0) {
        alert('재고가 부족합니다.');
        return;
    }

    // 수량 확인
    const quantity = parseInt(document.querySelector('.quantity-input')?.value || '1');

    if (quantity <= 0) {
        alert('수량을 올바르게 입력해주세요.');
        return;
    }

    if (currentBook.stockQuantity !== undefined && quantity > currentBook.stockQuantity) {
        alert(`재고가 부족합니다. (현재 재고: ${currentBook.stockQuantity}개)`);
        return;
    }

    // 주문 페이지로 이동 (상품 정보를 URL 파라미터로 전달)
    const orderData = {
        items: [{
            bookId: currentBookId,
            quantity: quantity,
            bookName: currentBook.bookName,
            price: currentBook.price,
            bookImage: currentBook.bookImage
        }]
    };

    // 세션 스토리지에 임시 저장 후 주문 페이지로 이동
    sessionStorage.setItem('orderData', JSON.stringify(orderData));
    window.location.href = '/order';
}

// 리뷰 작성
async function writeReview() {
    const loginStatus = await checkLoginStatus();

    if (!loginStatus.isLoggedIn) {
        alert('리뷰 작성을 위해 로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    // 구매 이력 확인 API 호출 (실제로는 구현되어야 함)
    try {
        // 이 부분은 실제로는 해당 사용자가 이 상품을 구매했는지 확인하는 API를 호출해야 함
        // const hasPurchased = await apiRequest(`/orders/check-purchase/${currentBookId}`);

        // 임시로 리뷰 작성 페이지로 이동
        window.location.href = `/review/write?bookId=${currentBookId}`;
    } catch (error) {
        console.error('구매 이력 확인 실패:', error);
        alert('리뷰 작성을 위해서는 해당 상품을 구매하셔야 합니다.');
    }
}

// 최근 본 상품에 추가
async function addToRecentViews(bookId) {
    try {
        await apiRequest('/recent-views', {
            method: 'POST',
            body: JSON.stringify({ bookId: bookId })
        });
    } catch (error) {
        // 최근 본 상품 추가 실패는 무시 (핵심 기능이 아니므로)
        console.error('최근 본 상품 추가 실패:', error);
    }
}

// 더 많은 리뷰 로드
let currentReviewPage = 0;
async function loadMoreReviews() {
    currentReviewPage++;
    try {
        const reviewsData = await apiRequest(`/books/${currentBookId}/reviews?page=${currentReviewPage}&size=5`);
        appendMoreReviews(reviewsData);
    } catch (error) {
        console.error('추가 리뷰 로드 실패:', error);
        showError('리뷰를 더 불러올 수 없습니다.');
    }
}

// 추가 리뷰 표시
function appendMoreReviews(reviewsData) {
    const reviewsList = document.querySelector('.reviews-list');
    if (!reviewsList || !reviewsData.content) return;

    reviewsData.content.forEach(review => {
        const reviewHtml = `
            <div class="review-item">
                <div class="review-header">
                    <div class="reviewer-info">
                        <span class="reviewer-name">${review.reviewer?.memberName || '익명'}</span>
                        <span class="reviewer-grade">${review.reviewer?.memberGrade || ''}</span>
                    </div>
                    <div class="review-rating">
                        ${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}
                    </div>
                    <div class="review-date">${formatDate(review.createdAt)}</div>
                </div>
                <div class="review-content">
                    <h5 class="review-title">${review.reviewTitle}</h5>
                    <p class="review-text">${review.reviewContent}</p>
                    ${review.isRecommended ? '<span class="recommended-badge">추천</span>' : ''}
                </div>
            </div>
        `;
        reviewsList.insertAdjacentHTML('beforeend', reviewHtml);
    });

    // 더보기 버튼 업데이트
    const moreButton = document.querySelector('.reviews-actions button');
    if (!reviewsData.hasNext && moreButton) {
        moreButton.style.display = 'none';
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
        addToCartBtn.textContent = isAvailable ? '장바구니 담기' : '구매 불가';
    }

    if (buyNowBtn) {
        buyNowBtn.disabled = !isAvailable;
        buyNowBtn.textContent = isAvailable ? '바로 구매' : '구매 불가';
    }
}

// 카테고리 경로 생성
function getCategoryPath(category) {
    let path = category.categoryName;

    if (category.middleCategoryName) {
        path = category.middleCategoryName + ' > ' + path;
    }

    if (category.topCategoryName) {
        path = category.topCategoryName + ' > ' + path;
    }

    return path;
}

// 날짜 포맷팅
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
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
    const quantityInput = document.querySelector('.quantity-input');
    const increaseBtn = document.querySelector('.quantity-increase');
    const decreaseBtn = document.querySelector('.quantity-decrease');

    if (quantityInput && increaseBtn && decreaseBtn) {
        increaseBtn.addEventListener('click', function() {
            const currentValue = parseInt(quantityInput.value || '1');
            const maxQuantity = currentBook?.stockQuantity || 999;
            if (currentValue < maxQuantity) {
                quantityInput.value = currentValue + 1;
            }
        });

        decreaseBtn.addEventListener('click', function() {
            const currentValue = parseInt(quantityInput.value || '1');
            if (currentValue > 1) {
                quantityInput.value = currentValue - 1;
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
        });
    }

    // 검색 기능
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