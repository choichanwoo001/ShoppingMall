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
});

// 페이지 초기화
function initializePage() {
    // 오늘 날짜를 종료일로 설정
    const today = new Date();
    const todayStr = today.toISOString().split('T')[0];
    const endDateInput = document.getElementById('endDate');
    if (endDateInput) {
        endDateInput.value = todayStr;
        endDateInput.max = todayStr;
    }

    // 3개월 전을 시작일로 설정
    const threeMonthsAgo = new Date();
    threeMonthsAgo.setMonth(threeMonthsAgo.getMonth() - 3);
    // 만약 3개월 전이 오늘보다 미래면 오늘로 고정
    if (threeMonthsAgo > today) threeMonthsAgo.setTime(today.getTime());
    const startDateInput = document.getElementById('startDate');
    if (startDateInput) {
        const startStr = threeMonthsAgo.toISOString().split('T')[0];
        startDateInput.value = startStr;
        startDateInput.max = todayStr;
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
        // closest 오류 방지: e.target이 Element인지 체크
        if (e.target && e.target.nodeType === 1 && e.target.closest && e.target.closest('.star-rating')) {
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

    // 클릭된 버튼에 active 클래스 추가
    if (buttonElement) {
        buttonElement.classList.add('active');
    }

    // 해당 탭 컨텐츠 보이기
    const targetContent = document.getElementById(tabName);
    if (targetContent) {
        targetContent.classList.add('active');
    }

    // 탭에 따른 데이터 로드
    switch (tabName) {
        case 'orders':
            loadOrderHistory(currentPage.orders);
            break;
        case 'reviews':
            loadMyReviews(currentPage.reviews);
            break;
        case 'wishlist':
            loadWishlist(currentPage.wishlist);
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

        let url = `/orders?page=${page}&size=${PAGE_SIZE}`;
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
        // 총 결제금액을 orderItems의 합으로 직접 계산
        let calcTotal = 0;
        if (order.orderItems && Array.isArray(order.orderItems)) {
            calcTotal = order.orderItems.reduce((sum, item) => {
                return sum + (item.price * item.quantity);
            }, 0);
        } else {
            calcTotal = order.totalAmount; // fallback
        }
        ordersHtml += `
            <div class="order-item">
                <div class="order-header">
                    <div class="order-info">
                        <span class="order-date">${formatDate(order.orderDate)}</span>
                        <span class="order-number">주문번호: ${order.orderNumber || order.orderId}</span>
                        <span class="order-total">총 결제금액: ${formatPrice(calcTotal)}원</span>
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
            `<img src="${item.book.bookImage}" alt="${item.book.bookName}">` :
            '<span>이미지</span>'
        }
                            </div>
                            <div class="product-info">
                                <div class="product-title">${item.book?.bookName || '상품명'}</div>
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
    const reviewsList = document.getElementById('reviews-list');
    if (!reviewsList) return;
    reviewsList.innerHTML = '';
    if (!reviewsData || !reviewsData.content || reviewsData.content.length === 0) {
        reviewsList.innerHTML = '<div class="no-reviews">작성한 리뷰가 없습니다.</div>';
        return;
    }
    reviewsData.content.forEach(review => {
        const div = document.createElement('div');
        div.className = 'review-item';
        div.innerHTML = `
            <div class="review-book">
                ${review.book && review.book.bookImage ? `<img src="${review.book.bookImage}" alt="${review.book.bookName}" class="review-book-img" style="width:60px;height:60px;object-fit:cover;margin-right:8px;vertical-align:middle;">` : ''}
                <span class="book-bookName">${review.book ? review.book.bookName : ''}</span>
                <span class="review-date">${formatDate(review.createdAt)}</span>
            </div>
            <div class="review-content">${review.reviewContent}</div>
            <div class="review-rating">평점: ${review.rating}점</div>
            <div class="review-actions">
                <button class="btn btn-secondary btn-edit-review" data-review-id="${review.reviewId}" data-review-title="${review.reviewTitle || ''}" data-review-content="${review.reviewContent || ''}" data-review-rating="${review.rating}">수정</button>
                <button class="btn btn-danger btn-delete-review" data-review-id="${review.reviewId}">삭제</button>
            </div>
        `;
        reviewsList.appendChild(div);
    });
    // 삭제 버튼 이벤트 연결
    document.querySelectorAll('.btn-delete-review').forEach(btn => {
        btn.addEventListener('click', async function() {
            const reviewId = this.getAttribute('data-review-id');
            try {
                await apiRequest(`/reviews/${reviewId}`, { method: 'DELETE' });
                await loadMyReviews(currentPage.reviews || 0);
            } catch (e) {
                alert('리뷰 삭제 중 오류가 발생했습니다.');
            }
        });
    });
    // 수정 버튼 이벤트 연결
    document.querySelectorAll('.btn-edit-review').forEach(btn => {
        btn.addEventListener('click', function() {
            const reviewId = this.getAttribute('data-review-id');
            const reviewTitle = this.getAttribute('data-review-title');
            const reviewContent = this.getAttribute('data-review-content');
            const reviewRating = this.getAttribute('data-review-rating');
            openEditReviewModal(reviewId, reviewTitle, reviewContent, reviewRating);
        });
    });

    // 페이지네이션 생성
    generatePagination('reviews-pagination', reviewsData, 'loadMyReviews');
}

function openEditReviewModal(reviewId, reviewTitle, reviewContent, reviewRating) {
    const modal = document.getElementById('reviewModal');
    if (!modal) return;
    document.getElementById('reviewModalTitle').textContent = '리뷰 수정';
    document.getElementById('reviewId').value = reviewId;
    document.getElementById('reviewContent').value = reviewContent;
    // 별점 세팅
    currentRating = parseInt(reviewRating);
    updateStarRating(currentRating);
    // 제목 입력란이 있다면 세팅 (폼에 제목 필드가 없으면 생략)
    const titleInput = document.getElementById('reviewTitle');
    if (titleInput) titleInput.value = reviewTitle;
    modal.classList.add('show');
}

// 리뷰 폼 제출 이벤트 핸들러 수정 (수정/작성 모두 지원)
async function handleReviewSubmit(event) {
    event.preventDefault();
    const reviewId = document.getElementById('reviewId').value;
    const content = document.getElementById('reviewContent').value;
    const rating = currentRating;
    // 제목 입력란이 있다면 가져오기 (폼에 제목 필드가 없으면 생략)
    const titleInput = document.getElementById('reviewTitle');
    const title = titleInput ? titleInput.value : '';
    if (!content || !rating) {
        alert('평점과 내용을 모두 입력해주세요.');
        return;
    }
    try {
        if (reviewId) {
            // 수정
            await apiRequest(`/reviews/${reviewId}`, {
                method: 'PUT',
                body: JSON.stringify({
                    reviewId: parseInt(reviewId),
                    reviewTitle: title,
                    reviewContent: content,
                    rating: rating
                })
            });
            closeReviewModal();
            await loadMyReviews(currentPage.reviews || 0);
        } else {
            // 신규 작성 (기존 로직)
            // ...
        }
    } catch (e) {
        alert('리뷰 저장 중 오류가 발생했습니다.');
    }
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
        `<img src="${item.book.bookImage}" alt="${item.book.bookName}">` :
        '<span>이미지</span>'
    }
                        </div>
                        <div class="product-info">
                            <div class="product-title">${item.book?.bookName || '상품명'}</div>
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
            ${bookInfo.bookImage ? `<img src="${bookInfo.bookImage}" alt="${bookInfo.bookName}">` : ''}
            <div class="book-info">
                <h4>${bookInfo.bookName}</h4>
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
                ${review.book.bookImage ? `<img src="${review.book.bookImage}" alt="${review.book.bookName}">` : ''}
                <div class="book-info">
                    <h4>${review.book.bookName}</h4>
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