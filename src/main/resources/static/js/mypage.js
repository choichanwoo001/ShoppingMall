// 마이페이지 JavaScript - DB 연동 버전

// API 기본 설정
const API_BASE_URL = '/api';
let currentRating = 0;
let currentPage = {
    orders: 0,
    reviews: 0,
    wishlist: 0
};
const PAGE_SIZE = 10;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    setupEventHandlers();
    checkLoginAndLoadData();
    loadRecentViewsForMypage(); // 최근 본 상품 배너 로드
});

// 페이지 초기화
function initializePage() {
    // 오늘 날짜를 종료일로 설정
    const today = new Date().toISOString().split('T')[0];
    const endDateInput = document.getElementById('endDate');
    if (endDateInput) {
        endDateInput.value = today;
    }

    // 3개월 전을 시작일로 설정
    const threeMonthsAgo = new Date();
    threeMonthsAgo.setMonth(threeMonthsAgo.getMonth() - 3);
    const startDateInput = document.getElementById('startDate');
    if (startDateInput) {
        startDateInput.value = threeMonthsAgo.toISOString().split('T')[0];
    }
}

// 로그인 상태 확인 및 데이터 로드
async function checkLoginAndLoadData() {
    try {
        const isLoggedIn = await checkLoginStatus();
        if (!isLoggedIn) {
            alert('로그인이 필요합니다.');
            window.location.href = '/login';
            return;
        }

        // 초기 탭(주문내역) 로드
        await loadOrderHistory();
    } catch (error) {
        console.error('초기화 실패:', error);
        showError('페이지 로드 중 오류가 발생했습니다.');
    }
}

// 로그인 상태 확인
async function checkLoginStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}/members/login-status`, {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const result = await response.json();
            return result.success && result.data.isLoggedIn;
        }
        return false;
    } catch (error) {
        console.error('로그인 상태 확인 실패:', error);
        return false;
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

        if (response.status === 401) {
            alert('로그인이 필요합니다.');
            window.location.href = '/login';
            return null;
        }

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

// 이벤트 핸들러 설정
function setupEventHandlers() {
    // 리뷰 폼 제출 이벤트
    const reviewForm = document.getElementById('reviewForm');
    if (reviewForm) {
        reviewForm.addEventListener('submit', handleReviewSubmit);
    }

    // 별점 클릭 이벤트
    setupStarRating();

    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', function(event) {
        const orderModal = document.getElementById('orderDetailModal');
        const reviewModal = document.getElementById('reviewModal');

        if (event.target === orderModal) {
            closeOrderDetailModal();
        }
        if (event.target === reviewModal) {
            closeReviewModal();
        }
    });

    // 검색 기능
    setupSearchHandlers();
}

// 별점 이벤트 설정
function setupStarRating() {
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('star')) {
            currentRating = parseInt(e.target.dataset.rating);
            updateStarRating(currentRating);
        }
    });

    document.addEventListener('mouseover', function(e) {
        if (e.target.classList.contains('star')) {
            const rating = parseInt(e.target.dataset.rating);
            highlightStars(rating);
        }
    });

    document.addEventListener('mouseleave', function(e) {
        if (e.target.closest('.star-rating')) {
            updateStarRating(currentRating);
        }
    });
}

// 검색 핸들러 설정
function setupSearchHandlers() {
    const searchBtn = document.querySelector('.search-btn');
    const searchInput = document.querySelector('.search-input');

    if (searchBtn) {
        searchBtn.addEventListener('click', function() {
            const searchTerm = searchInput.value.trim();
            if (searchTerm) {
                window.location.href = `/?search=${encodeURIComponent(searchTerm)}`;
            }
        });
    }

    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchBtn.click();
            }
        });
    }
}

// editProfile 함수 (member-edit.html로 연결)
function editProfile() {
    location.href = '/mypage/edit';
}

// 탭 전환
function showTab(tabName, buttonElement) {
    // 모든 탭 버튼에서 active 클래스 제거
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // 모든 탭 컨텐츠 숨기기
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });

    // 선택된 탭 버튼에 active 클래스 추가
    if (buttonElement) {
        buttonElement.classList.add('active');
    }

    // 선택된 탭 컨텐츠 보이기
    const tabContent = document.getElementById(tabName);
    if (tabContent) {
        tabContent.classList.add('active');
    }

    // 탭별 데이터 로드
    switch(tabName) {
        case 'orders':
            loadOrderHistory();
            break;
        case 'reviews':
            loadMyReviews();
            break;
        case 'wishlist':
            loadWishlist();
            break;
    }
}

// 주문 내역 로드
async function loadOrderHistory(page = 0) {
    try {
        showLoading('orders-list');
        currentPage.orders = page;

        // 필터 조건 수집
        const status = document.querySelector('.order-status-filter')?.value || '';
        const startDate = document.getElementById('startDate')?.value || '';
        const endDate = document.getElementById('endDate')?.value || '';

        let url = `/orders/my?page=${page}&size=${PAGE_SIZE}`;
        if (status) url += `&status=${encodeURIComponent(status)}`;
        if (startDate) url += `&startDate=${startDate}`;
        if (endDate) url += `&endDate=${endDate}`;

        const ordersData = await apiRequest(url);
        displayOrderHistory(ordersData);

    } catch (error) {
        console.error('주문 내역 로드 실패:', error);
        showError('주문 내역을 불러올 수 없습니다.');
        document.getElementById('orders-list').innerHTML = '<div class="empty-message"><p>주문 내역을 불러올 수 없습니다.</p></div>';
    }
}

// 주문 내역 표시
function displayOrderHistory(ordersData) {
    const ordersContainer = document.getElementById('orders-list');

    if (!ordersData || !ordersData.content || ordersData.content.length === 0) {
        ordersContainer.innerHTML = `
            <div class="empty-message">
                <p>주문 내역이 없습니다.</p>
                <button class="btn btn-primary" onclick="location.href='/'">쇼핑하러 가기</button>
            </div>
        `;
        document.getElementById('orders-pagination').innerHTML = '';
        return;
    }

    let ordersHtml = '';
    ordersData.content.forEach(order => {
        ordersHtml += `
            <div class="order-item">
                <div class="order-header">
                    <div class="order-info">
                        <span class="order-date">${formatDate(order.orderDate)}</span>
                        <span class="order-number">주문번호: ${order.orderNumber || order.orderId}</span>
                        <span class="order-total">총 결제금액: ${formatPrice(order.totalAmount)}원</span>
                    </div>
                    <div class="order-status-area">
                        <span class="order-status ${getOrderStatusClass(order.orderStatus)}">${order.orderStatus}</span>
                        <button class="btn btn-small btn-secondary" onclick="viewOrderDetail(${order.orderId})">주문상세</button>
                    </div>
                </div>
                <div class="order-products">
                    ${order.orderItems ? order.orderItems.map(item => `
                        <div class="product-row">
                            <div class="product-image">
                                ${item.book?.bookImage ?
            `<img src="${item.book.bookImage}" alt="${item.book.bookTitle}">` :
            '<span>이미지</span>'
        }
                            </div>
                            <div class="product-info">
                                <div class="product-title">${item.book?.bookTitle || '상품명'}</div>
                                <div class="product-author">${item.book?.author || ''} ${item.book?.publisher ? '| ' + item.book.publisher : ''}</div>
                                <div class="product-price">${formatPrice(item.price)}원</div>
                                <div class="product-quantity">수량: ${item.quantity}개</div>
                            </div>
                            <div class="product-actions">
                                ${getOrderActionButtons(order.orderStatus, order.orderId, item.book?.bookId)}
                            </div>
                        </div>
                    `).join('') : ''}
                </div>
            </div>
        `;
    });

    ordersContainer.innerHTML = ordersHtml;

    // 페이지네이션 생성
    generatePagination('orders-pagination', ordersData, 'loadOrderHistory');
}

// 주문 상태에 따른 CSS 클래스 반환
function getOrderStatusClass(status) {
    switch(status) {
        case '배송완료': return 'delivered';
        case '배송중': return 'shipping';
        case '배송준비중':
        case '준비중': return 'preparing';
        case '주문취소': return 'cancelled';
        case '결제완료': return 'paid';
        default: return 'paid';
    }
}

// 주문 상태에 따른 액션 버튼 생성
function getOrderActionButtons(orderStatus, orderId, bookId) {
    let buttons = '';

    if (orderStatus === '결제완료' || orderStatus === '배송준비중') {
        buttons += `<button class="btn btn-small btn-secondary" onclick="cancelOrder(${orderId})">주문취소</button>`;
    }

    if (orderStatus === '배송중') {
        buttons += `<button class="btn btn-small btn-primary" onclick="trackOrder(${orderId})">배송추적</button>`;
    }

    if (orderStatus === '배송완료' && bookId) {
        buttons += `<button class="btn btn-small btn-primary" onclick="writeReview(${bookId}, ${orderId})">리뷰작성</button>`;
    }

    return buttons;
}

// 내 리뷰 목록 로드
async function loadMyReviews(page = 0) {
    try {
        showLoading('reviews-list');
        currentPage.reviews = page;

        const reviewsData = await apiRequest(`/reviews/my?page=${page}&size=${PAGE_SIZE}`);
        displayMyReviews(reviewsData);

        // 리뷰 통계 로드
        try {
            const statsData = await apiRequest('/reviews/my/stats');
            displayReviewStats(statsData);
        } catch (error) {
            console.error('리뷰 통계 로드 실패:', error);
        }

    } catch (error) {
        console.error('리뷰 내역 로드 실패:', error);
        showError('리뷰 내역을 불러올 수 없습니다.');
        document.getElementById('reviews-list').innerHTML = '<div class="empty-message"><p>리뷰 내역을 불러올 수 없습니다.</p></div>';
    }
}

// 리뷰 통계 표시
function displayReviewStats(statsData) {
    const statsContainer = document.getElementById('review-stats');
    if (statsContainer && statsData) {
        statsContainer.innerHTML = `
            <span>총 <strong>${statsData.totalCount || 0}</strong>개의 리뷰</span>
            <span>평균 평점: <strong>${statsData.averageRating || 0}</strong>점</span>
        `;
    }
}

// 내 리뷰 목록 표시
function displayMyReviews(reviewsData) {
    const reviewsContainer = document.getElementById('reviews-list');

    if (!reviewsData || !reviewsData.content || reviewsData.content.length === 0) {
        reviewsContainer.innerHTML = `
            <div class="empty-message">
                <p>작성한 리뷰가 없습니다.</p>
                <button class="btn btn-primary" onclick="location.href='/'">책 구매하고 리뷰 남기기</button>
            </div>
        `;
        document.getElementById('reviews-pagination').innerHTML = '';
        return;
    }

    let reviewsHtml = '';
    reviewsData.content.forEach(review => {
        reviewsHtml += `
            <div class="review-item">
                <div class="review-book">
                    <div class="book-image">
                        ${review.book?.bookImage ?
            `<img src="${review.book.bookImage}" alt="${review.book.bookTitle}">` :
            '<span>이미지</span>'
        }
                    </div>
                    <div class="book-info">
                        <div class="book-title">${review.book?.bookTitle || '도서명'}</div>
                        <div class="book-author">${review.book?.author || ''}</div>
                    </div>
                </div>
                <div class="review-content">
                    <div class="review-rating">
                        ${generateStarRating(review.rating)}
                    </div>
                    <p class="review-text">${review.reviewContent || ''}</p>
                    <div class="review-date">${formatDate(review.reviewDate || review.createdAt)} 작성</div>
                </div>
                <div class="review-actions">
                    <button class="btn btn-small btn-secondary" onclick="editReview(${review.reviewId})">수정</button>
                    <button class="btn btn-small btn-danger" onclick="deleteReview(${review.reviewId})">삭제</button>
                </div>
            </div>
        `;
    });

    reviewsContainer.innerHTML = reviewsHtml;

    // 페이지네이션 생성
    generatePagination('reviews-pagination', reviewsData, 'loadMyReviews');
}

// 관심목록 로드
async function loadWishlist(page = 0) {
    try {
        showLoading('wishlist-grid');
        currentPage.wishlist = page;

        const wishlistData = await apiRequest(`/wishlist?page=${page}&size=${PAGE_SIZE}`);
        displayWishlist(wishlistData);

    } catch (error) {
        console.error('관심목록 로드 실패:', error);
        showError('관심목록을 불러올 수 없습니다.');
        document.getElementById('wishlist-grid').innerHTML = '<div class="empty-message"><p>관심목록을 불러올 수 없습니다.</p></div>';
    }
}

// 관심목록 표시
function displayWishlist(wishlistData) {
    const wishlistContainer = document.getElementById('wishlist-grid');
    const wishlistCountSpan = document.getElementById('wishlist-count');

    // 관심목록 개수 업데이트
    if (wishlistCountSpan) {
        const totalCount = wishlistData?.totalElements || 0;
        wishlistCountSpan.innerHTML = `총 <strong>${totalCount}</strong>개의 관심 도서`;
    }

    if (!wishlistData || !wishlistData.content || wishlistData.content.length === 0) {
        wishlistContainer.innerHTML = `
            <div class="empty-message">
                <p>관심 도서가 없습니다.</p>
                <button class="btn btn-primary" onclick="location.href='/'">도서 둘러보기</button>
            </div>
        `;
        document.getElementById('wishlist-pagination').innerHTML = '';
        return;
    }

    wishlistContainer.className = 'wishlist-grid';
    let wishlistHtml = '';

    wishlistData.content.forEach(item => {
        const book = item.book || item; // API 응답 구조에 따라 조정
        wishlistHtml += `
            <div class="book-card">
                <div class="book-image">
                    ${book.bookImage ?
            `<img src="${book.bookImage}" alt="${book.bookTitle}">` :
            '<span>이미지</span>'
        }
                </div>
                <div class="book-info">
                    <div class="book-title">${book.bookTitle || book.title}</div>
                    <div class="book-author">${book.author || ''}</div>
                    <div class="book-publisher">${book.publisher || ''}</div>
                    <div class="book-price">${formatPrice(book.price)}원</div>
                    <div class="book-rating">
                        <span class="rating-stars">
                            ${generateStarRating(book.averageRating || 0)}
                        </span>
                        <span class="rating-text">(${book.averageRating || 0}점)</span>
                    </div>
                </div>
                <div class="book-actions">
                    <button class="btn btn-small btn-primary" onclick="addToCart(${book.bookId || book.id})">장바구니</button>
                    <button class="btn btn-small btn-danger" onclick="removeFromWishlist(${book.bookId || book.id})">삭제</button>
                </div>
            </div>
        `;
    });

    wishlistContainer.innerHTML = wishlistHtml;

    // 페이지네이션 생성
    generatePagination('wishlist-pagination', wishlistData, 'loadWishlist');
}

// 주문 필터링
function filterOrders() {
    loadOrderHistory(0); // 첫 페이지부터 다시 로드
}

// 주문 상세 보기
async function viewOrderDetail(orderId) {
    try {
        const orderDetail = await apiRequest(`/orders/${orderId}`);
        displayOrderDetail(orderDetail);
        document.getElementById('orderDetailModal').style.display = 'block';
    } catch (error) {
        console.error('주문 상세 조회 실패:', error);
        alert('주문 정보를 불러오는 데 실패했습니다.');
    }
}

// 주문 상세 정보 표시
function displayOrderDetail(order) {
    const content = document.getElementById('orderDetailContent');

    const deliveryInfo = order.deliveryAddress ? `
        <div class="detail-section">
            <h4>배송 정보</h4>
            <div class="detail-grid">
                <div class="detail-row">
                    <span class="detail-label">받는분</span>
                    <span class="detail-value">${order.receiverName || ''}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">연락처</span>
                    <span class="detail-value">${order.receiverPhone || ''}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">배송주소</span>
                    <span class="detail-value">${order.deliveryAddress}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">배송메모</span>
                    <span class="detail-value">${order.deliveryMemo || '없음'}</span>
                </div>
            </div>
        </div>
    ` : '';

    content.innerHTML = `
        <div class="order-detail-info">
            <div class="detail-section">
                <h4>주문 정보</h4>
                <div class="detail-grid">
                    <div class="detail-row">
                        <span class="detail-label">주문번호</span>
                        <span class="detail-value">${order.orderNumber || order.orderId}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">주문일시</span>
                        <span class="detail-value">${formatDateTime(order.orderDate)}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">주문상태</span>
                        <span class="detail-value">${order.orderStatus}</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">결제방법</span>
                        <span class="detail-value">${order.paymentMethod || '카카오페이'}</span>
                    </div>
                </div>
            </div>

            ${deliveryInfo}

            <div class="detail-section">
                <h4>주문 상품</h4>
                ${order.orderItems ? order.orderItems.map(item => `
                    <div class="product-row">
                        <div class="product-image">
                            ${item.book?.bookImage ?
        `<img src="${item.book.bookImage}" alt="${item.book.bookTitle}">` :
        '<span>이미지</span>'
    }
                        </div>
                        <div class="product-info">
                            <div class="product-title">${item.book?.bookTitle || '상품명'}</div>
                            <div class="product-author">${item.book?.author || ''} ${item.book?.publisher ? '| ' + item.book.publisher : ''}</div>
                            <div class="product-price">${formatPrice(item.price)}원</div>
                            <div class="product-quantity">수량: ${item.quantity}개</div>
                        </div>
                        <div class="item-total">
                            <strong>${formatPrice(item.price * item.quantity)}원</strong>
                        </div>
                    </div>
                `).join('') : ''}
            </div>

            <div class="detail-section">
                <h4>결제 정보</h4>
                <div class="detail-grid">
                    <div class="detail-row">
                        <span class="detail-label">상품금액</span>
                        <span class="detail-value">${formatPrice(order.totalAmount)}원</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">배송비</span>
                        <span class="detail-value">${formatPrice(order.deliveryFee || 0)}원</span>
                    </div>
                    <div class="detail-row">
                        <span class="detail-label">할인금액</span>
                        <span class="detail-value">-${formatPrice(order.discountAmount || 0)}원</span>
                    </div>
                    <div class="detail-row total-row">
                        <span class="detail-label"><strong>총 결제금액</strong></span>
                        <span class="detail-value"><strong>${formatPrice(order.finalAmount || order.totalAmount)}원</strong></span>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// 주문 상세 모달 닫기
function closeOrderDetailModal() {
    document.getElementById('orderDetailModal').style.display = 'none';
}

// 주문 취소
async function cancelOrder(orderId) {
    if (!confirm('정말로 이 주문을 취소하시겠습니까?')) {
        return;
    }

    try {
        await apiRequest(`/orders/${orderId}/cancel`, {
            method: 'POST'
        });

        alert('주문이 취소되었습니다.');
        loadOrderHistory(currentPage.orders); // 현재 페이지 새로고침
    } catch (error) {
        console.error('주문 취소 실패:', error);
        alert('주문 취소에 실패했습니다.');
    }
}

// 배송 추적
function trackOrder(orderId) {
    // 배송 추적 페이지로 이동 또는 모달 표시
    window.open(`/orders/${orderId}/tracking`, '_blank');
}

// 리뷰 작성 모달 열기
async function writeReview(bookId, orderId) {
    try {
        // 도서 정보 조회
        const bookInfo = await apiRequest(`/books/${bookId}`);

        document.getElementById('reviewId').value = '';
        document.getElementById('reviewBookId').value = bookId;
        document.getElementById('reviewOrderId').value = orderId;
        document.getElementById('reviewContent').value = '';
        document.getElementById('reviewModalTitle').textContent = '리뷰 작성';

        // 도서 정보 표시
        const bookInfoDiv = document.getElementById('reviewBookInfo');
        bookInfoDiv.innerHTML = `
            ${bookInfo.bookImage ? `<img src="${bookInfo.bookImage}" alt="${bookInfo.bookTitle}">` : ''}
            <div class="book-info">
                <h4>${bookInfo.bookTitle}</h4>
                <p>${bookInfo.author} | ${bookInfo.publisher}</p>
            </div>
        `;

        currentRating = 0;
        updateStarRating(0);
        document.getElementById('reviewModal').style.display = 'block';
    } catch (error) {
        console.error('리뷰 작성 모달 열기 실패:', error);
        alert('리뷰 작성을 준비하는 중 오류가 발생했습니다.');
    }
}

// 리뷰 수정
async function editReview(reviewId) {
    try {
        const review = await apiRequest(`/reviews/${reviewId}`);

        document.getElementById('reviewId').value = reviewId;
        document.getElementById('reviewBookId').value = review.bookId;
        document.getElementById('reviewOrderId').value = review.orderId || '';
        document.getElementById('reviewContent').value = review.reviewContent;
        document.getElementById('reviewModalTitle').textContent = '리뷰 수정';

        // 도서 정보 표시
        const bookInfoDiv = document.getElementById('reviewBookInfo');
        if (review.book) {
            bookInfoDiv.innerHTML = `
                ${review.book.bookImage ? `<img src="${review.book.bookImage}" alt="${review.book.bookTitle}">` : ''}
                <div class="book-info">
                    <h4>${review.book.bookTitle}</h4>
                    <p>${review.book.author} | ${review.book.publisher}</p>
                </div>
            `;
        }

        currentRating = review.rating;
        updateStarRating(review.rating);
        document.getElementById('reviewModal').style.display = 'block';
    } catch (error) {
        console.error('리뷰 수정 실패:', error);
        alert('리뷰 정보를 불러오는 데 실패했습니다.');
    }
}

// 리뷰 작성 모달 닫기
function closeReviewModal() {
    document.getElementById('reviewModal').style.display = 'none';
}

// 별점 업데이트
function updateStarRating(rating) {
    const stars = document.querySelectorAll('.star-rating .star');
    stars.forEach((star, index) => {
        if (index < rating) {
            star.classList.add('active');
        } else {
            star.classList.remove('active');
        }
    });
}

// 별점 하이라이트
function highlightStars(rating) {
    const stars = document.querySelectorAll('.star-rating .star');
    stars.forEach((star, index) => {
        if (index < rating) {
            star.style.color = 'var(--warning-color)';
        } else {
            star.style.color = 'var(--border-color)';
        }
    });
}

// 리뷰 폼 제출
async function handleReviewSubmit(event) {
    event.preventDefault();

    const reviewId = document.getElementById('reviewId').value;
    const bookId = document.getElementById('reviewBookId').value;
    const orderId = document.getElementById('reviewOrderId').value;
    const content = document.getElementById('reviewContent').value.trim();

    if (currentRating === 0) {
        alert('별점을 선택해주세요.');
        return;
    }

    if (!content) {
        alert('리뷰 내용을 입력해주세요.');
        return;
    }

    const reviewData = {
        bookId: parseInt(bookId),
        orderId: orderId ? parseInt(orderId) : null,
        rating: currentRating,
        reviewContent: content
    };

    try {
        if (reviewId) {
            // 리뷰 수정
            await apiRequest(`/reviews/${reviewId}`, {
                method: 'PUT',
                body: JSON.stringify(reviewData)
            });
            alert('리뷰가 수정되었습니다.');
        } else {
            // 리뷰 등록
            await apiRequest('/reviews', {
                method: 'POST',
                body: JSON.stringify(reviewData)
            });
            alert('리뷰가 등록되었습니다.');
        }

        closeReviewModal();
        loadMyReviews(currentPage.reviews); // 현재 페이지 새로고침
    } catch (error) {
        console.error('리뷰 저장 실패:', error);
        alert('리뷰 저장에 실패했습니다.');
    }
}

// 리뷰 삭제
async function deleteReview(reviewId) {
    if (!confirm('정말로 이 리뷰를 삭제하시겠습니까?')) {
        return;
    }

    try {
        await apiRequest(`/reviews/${reviewId}`, {
            method: 'DELETE'
        });

        alert('리뷰가 삭제되었습니다.');
        loadMyReviews(currentPage.reviews); // 현재 페이지 새로고침
    } catch (error) {
        console.error('리뷰 삭제 실패:', error);
        alert('리뷰 삭제에 실패했습니다.');
    }
}

// 장바구니에 추가
async function addToCart(bookId) {
    try {
        await apiRequest('/cart/add', {
            method: 'POST',
            body: JSON.stringify({
                bookId: bookId,
                quantity: 1
            })
        });

        if (confirm('장바구니에 추가되었습니다. 장바구니로 이동하시겠습니까?')) {
            location.href = '/cart';
        }
    } catch (error) {
        console.error('장바구니 추가 실패:', error);
        alert('장바구니 추가에 실패했습니다.');
    }
}

// 관심목록에서 제거
async function removeFromWishlist(bookId) {
    if (!confirm('관심목록에서 제거하시겠습니까?')) {
        return;
    }

    try {
        await apiRequest(`/wishlist/${bookId}`, {
            method: 'DELETE'
        });

        alert('관심목록에서 제거되었습니다.');
        loadWishlist(currentPage.wishlist); // 현재 페이지 새로고침
    } catch (error) {
        console.error('관심목록 제거 실패:', error);
        alert('관심목록 제거에 실패했습니다.');
    }
}

// 관심목록 전체 삭제
async function clearWishlist() {
    if (!confirm('관심목록을 모두 삭제하시겠습니까?')) {
        return;
    }

    try {
        await apiRequest('/wishlist/clear', {
            method: 'DELETE'
        });

        alert('관심목록이 모두 삭제되었습니다.');
        loadWishlist(0); // 첫 페이지로 이동
    } catch (error) {
        console.error('관심목록 삭제 실패:', error);
        alert('관심목록 삭제에 실패했습니다.');
    }
}

// 페이지네이션 생성
function generatePagination(containerId, pageData, functionName) {
    const container = document.getElementById(containerId);
    if (!container || !pageData) return;

    const {currentPage = 0, totalPages = 0} = pageData;

    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let paginationHtml = '<div class="pagination">';

    // 이전 페이지
    if (currentPage > 0) {
        paginationHtml += `<button onclick="${functionName}(${currentPage - 1})" class="page-btn">이전</button>`;
    }

    // 페이지 번호들
    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
        const activeClass = i === currentPage ? 'active' : '';
        paginationHtml += `<button onclick="${functionName}(${i})" class="page-btn ${activeClass}">${i + 1}</button>`;
    }

    // 다음 페이지
    if (currentPage < totalPages - 1) {
        paginationHtml += `<button onclick="${functionName}(${currentPage + 1})" class="page-btn">다음</button>`;
    }

    paginationHtml += '</div>';
    container.innerHTML = paginationHtml;
}

// 로딩 표시
function showLoading(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = '<div class="loading">데이터를 불러오는 중...</div>';
    }
}

// 에러 표시
function showError(message) {
    alert(message);
}

// 별점 생성 함수
function generateStarRating(rating) {
    const fullStars = Math.floor(rating);
    const emptyStars = 5 - fullStars;

    return '★'.repeat(fullStars) + '☆'.repeat(emptyStars);
}

// 유틸리티 함수들
function formatPrice(price) {
    if (price == null) return '0';
    return new Intl.NumberFormat('ko-KR').format(price);
}

function formatDateTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}

// 마이페이지 최근 본 상품 배너 동적 로드
function loadRecentViewsForMypage() {
    const section = document.getElementById('recent-view-mypage-section');
    const list = document.getElementById('recent-view-mypage-list');
    const clearBtn = document.getElementById('recent-view-mypage-clear-btn');
    if (!section || !list || !clearBtn) return;

    fetch('/recent-views?limit=10', { credentials: 'include' })
        .then(res => {
            if (res.status === 401) {
                list.innerHTML = '<div class="no-data-small">로그인하시면 최근 본 상품을 확인할 수 있습니다.</div>';
                clearBtn.style.display = 'none';
                return null;
            }
            return res.json();
        })
        .then(data => {
            if (!data) return;
            if (!data.success || !Array.isArray(data.data) || data.data.length === 0) {
                list.innerHTML = '<div class="no-data-small">최근 본 상품이 없습니다.</div>';
                clearBtn.style.display = 'none';
                return;
            }
            list.innerHTML = '';
            clearBtn.style.display = '';
            data.data.forEach(recent => {
                const book = recent.book;
                const div = document.createElement('div');
                div.className = 'recent-book';
                div.style.display = 'flex';
                div.style.alignItems = 'center';
                div.style.marginBottom = '0.5rem';
                div.innerHTML = `
                    <div class="recent-book-image" style="width:2.5rem;height:3.125rem;margin-right:0.75rem;">
                        ${book.bookImage ? `<img src="${book.bookImage}" alt="${book.bookName}" style="width:100%;height:100%;object-fit:cover;">` : '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#718096;font-size:0.5rem;">이미지</div>'}
                    </div>
                    <div class="recent-book-info" style="flex:1;min-width:0;">
                        <div class="recent-book-title" style="font-size:0.95rem;font-weight:500;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${book.bookName}</div>
                        <div class="recent-book-author" style="font-size:0.8rem;color:#888;margin-bottom:0.15rem;">${book.author}</div>
                        <div class="recent-book-price" style="font-size:0.8rem;color:var(--accent-color);font-weight:bold;">${Number(book.price).toLocaleString()}원</div>
                    </div>
                    <button class="btn btn-xs btn-danger recent-view-delete-btn" data-book-id="${book.bookId}" style="margin-left:0.5rem;">삭제</button>
                `;
                div.onclick = (e) => {
                    if (e.target.classList.contains('recent-view-delete-btn')) return;
                    location.href = `/product/${book.bookId}`;
                };
                list.appendChild(div);
            });
            // 삭제 버튼 이벤트
            list.querySelectorAll('.recent-view-delete-btn').forEach(btn => {
                btn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    const bookId = this.getAttribute('data-book-id');
                    if (confirm('이 상품을 최근 본 목록에서 삭제하시겠습니까?')) {
                        fetch(`/recent-views/${bookId}`, { method: 'DELETE', credentials: 'include' })
                            .then(() => loadRecentViewsForMypage());
                    }
                });
            });
        })
        .catch(() => {
            list.innerHTML = '<div class="no-data-small">최근 본 상품을 불러올 수 없습니다.</div>';
            clearBtn.style.display = 'none';
        });

    // 전체삭제 버튼 이벤트
    clearBtn.onclick = function() {
        if (confirm('최근 본 상품을 모두 삭제하시겠습니까?')) {
            fetch('/recent-views/clear', { method: 'DELETE', credentials: 'include' })
                .then(() => loadRecentViewsForMypage());
        }
    };
}