// API 기본 설정
const API_BASE_URL = '/api';

// 세션 기반 인증 확인
async function checkLoginStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}/members/login-status`, {
            method: 'GET',
            credentials: 'include' // 세션 쿠키 포함
        });

        if (response.ok) {
            const result = await response.json();
            return result.data.isLoggedIn;
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
        credentials: 'include', // 세션 쿠키 포함
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
            return;
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

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', async function() {
    const isLoggedIn = await checkLoginStatus();

    if (!isLoggedIn) {
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    await loadUserInfo();
    await loadOrderHistory();
    await loadMyReviews();
});

// 사용자 정보 로드
async function loadUserInfo() {
    try {
        const loginStatus = await apiRequest('/members/login-status');
        if (loginStatus.isLoggedIn && loginStatus.member) {
            displayUserInfo(loginStatus.member);
        }
    } catch (error) {
        console.error('사용자 정보 로드 실패:', error);
        showError('사용자 정보를 불러올 수 없습니다.');
    }
}

// 사용자 정보 표시
function displayUserInfo(member) {
    const profileSection = document.querySelector('#profile .tab-content');
    if (profileSection) {
        profileSection.innerHTML = `
            <div class="profile-info">
                <h3>회원 정보</h3>
                <div class="info-item">
                    <label>아이디:</label>
                    <span>${member.memberId}</span>
                </div>
                <div class="info-item">
                    <label>이름:</label>
                    <span>${member.memberName}</span>
                </div>
                <div class="info-item">
                    <label>이메일:</label>
                    <span>${member.email}</span>
                </div>
                <div class="info-item">
                    <label>연락처:</label>
                    <span>${member.phone}</span>
                </div>
                <div class="info-item">
                    <label>주소:</label>
                    <span>${member.address}</span>
                </div>
                <div class="info-item">
                    <label>회원등급:</label>
                    <span>${member.memberGrade}</span>
                </div>
                <div class="info-item">
                    <label>가입일:</label>
                    <span>${formatDate(member.joinDate)}</span>
                </div>
                <div class="info-item">
                    <label>최종 로그인:</label>
                    <span>${member.lastLoginDate ? formatDate(member.lastLoginDate) : '정보 없음'}</span>
                </div>
                <button onclick="editProfile()" class="btn btn-primary">프로필 수정</button>
            </div>
        `;
    }
}

// 주문 내역 로드
async function loadOrderHistory(page = 0, size = 10) {
    try {
        const ordersData = await apiRequest(`/orders?page=${page}&size=${size}`);
        displayOrderHistory(ordersData);
    } catch (error) {
        console.error('주문 내역 로드 실패:', error);
        showError('주문 내역을 불러올 수 없습니다.');
    }
}

// 주문 내역 표시
function displayOrderHistory(ordersData) {
    const ordersSection = document.querySelector('#orders');
    if (!ordersSection) return;

    let ordersHtml = '<h3>주문 내역</h3>';

    if (ordersData.content && ordersData.content.length > 0) {
        ordersHtml += '<div class="orders-list">';
        ordersData.content.forEach(order => {
            ordersHtml += `
                <div class="order-item">
                    <div class="order-header">
                        <span class="order-id">주문번호: ${order.orderId}</span>
                        <span class="order-date">${formatDate(order.orderDate)}</span>
                        <span class="order-status status-${order.orderStatus?.toLowerCase()}">${order.orderStatus}</span>
                    </div>
                    <div class="order-summary">
                        <div class="representative-book">
                            <strong>${order.representativeBookName}</strong>
                            ${order.totalItemCount > 1 ? ` 외 ${order.totalItemCount - 1}권` : ''}
                        </div>
                        <div class="order-amount">
                            총 결제금액: <strong>${order.totalAmount?.toLocaleString()}원</strong>
                        </div>
                    </div>
                    <div class="order-actions">
                        <button onclick="viewOrderDetail(${order.orderId})" class="btn btn-secondary">상세보기</button>
                        ${order.orderStatus === '주문완료' || order.orderStatus === '결제완료' ?
                `<button onclick="cancelOrder(${order.orderId})" class="btn btn-danger">주문취소</button>` : ''}
                        ${order.orderStatus === '배송완료' ?
                `<button onclick="showReviewModal(${order.orderId})" class="btn btn-primary">리뷰작성</button>` : ''}
                    </div>
                </div>
            `;
        });
        ordersHtml += '</div>';

        // 페이지네이션
        if (ordersData.totalPages > 1) {
            ordersHtml += createPagination(ordersData.currentPage, ordersData.totalPages, 'loadOrderHistory');
        }
    } else {
        ordersHtml += '<p class="no-data">주문 내역이 없습니다.</p>';
    }

    ordersSection.innerHTML = ordersHtml;
}

// 내 리뷰 목록 로드
async function loadMyReviews(page = 0, size = 10) {
    try {
        const reviewsData = await apiRequest(`/reviews/my?page=${page}&size=${size}`);
        displayMyReviews(reviewsData);
    } catch (error) {
        console.error('리뷰 내역 로드 실패:', error);
        showError('리뷰 내역을 불러올 수 없습니다.');
    }
}

// 내 리뷰 목록 표시
function displayMyReviews(reviewsData) {
    const reviewsSection = document.querySelector('#reviews');
    if (!reviewsSection) return;

    let reviewsHtml = '<h3>내 리뷰</h3>';

    if (reviewsData.content && reviewsData.content.length > 0) {
        reviewsHtml += '<div class="reviews-list">';
        reviewsData.content.forEach(review => {
            reviewsHtml += `
                <div class="review-item">
                    <div class="review-book-info">
                        <img src="${review.book?.bookImage || '/images/default-book.jpg'}" alt="${review.book?.bookName}" class="book-image">
                        <div class="book-details">
                            <h4>${review.book?.bookName}</h4>
                            <p>저자: ${review.book?.author} | 출판사: ${review.book?.publisher}</p>
                        </div>
                    </div>
                    <div class="review-content">
                        <div class="review-rating">
                            ${'★'.repeat(review.rating)}${'☆'.repeat(5 - review.rating)}
                            <span class="rating-text">(${review.rating}/5)</span>
                            ${review.isRecommended ? '<span class="recommended">추천</span>' : ''}
                        </div>
                        <h5 class="review-title">${review.reviewTitle}</h5>
                        <p class="review-text">${review.reviewContent}</p>
                        <div class="review-date">
                            작성일: ${formatDate(review.createdAt)}
                            ${review.updatedAt !== review.createdAt ? `| 수정일: ${formatDate(review.updatedAt)}` : ''}
                        </div>
                    </div>
                    <div class="review-actions">
                        <button onclick="editReview(${review.reviewId})" class="btn btn-secondary">수정</button>
                        <button onclick="deleteReview(${review.reviewId})" class="btn btn-danger">삭제</button>
                    </div>
                </div>
            `;
        });
        reviewsHtml += '</div>';

        // 페이지네이션
        if (reviewsData.totalPages > 1) {
            reviewsHtml += createPagination(reviewsData.currentPage, reviewsData.totalPages, 'loadMyReviews');
        }
    } else {
        reviewsHtml += '<p class="no-data">작성한 리뷰가 없습니다.</p>';
    }

    reviewsSection.innerHTML = reviewsHtml;
}

// 탭 전환
function showTab(tabName) {
    // 모든 탭 버튼 비활성화
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    // 모든 탭 콘텐츠 숨기기
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

    // 선택된 탭 활성화
    event.target.classList.add('active');
    document.getElementById(tabName).classList.add('active');

    // 탭별 데이터 로드
    switch(tabName) {
        case 'orders':
            loadOrderHistory();
            break;
        case 'reviews':
            loadMyReviews();
            break;
    }
}

// 프로필 수정
async function editProfile() {
    // 프로필 수정 모달 또는 페이지로 이동
    const isConfirmed = confirm('프로필 수정 페이지로 이동하시겠습니까?');
    if (isConfirmed) {
        // 실제로는 프로필 수정 페이지로 이동하거나 모달을 열어야 함
        alert('프로필 수정 기능은 준비 중입니다.');
    }
}

// 주문 상세보기
async function viewOrderDetail(orderId) {
    try {
        const orderDetail = await apiRequest(`/orders/${orderId}`);
        showOrderDetailModal(orderDetail);
    } catch (error) {
        console.error('주문 상세 조회 실패:', error);
        showError('주문 상세 정보를 불러올 수 없습니다.');
    }
}

// 주문 취소
async function cancelOrder(orderId) {
    const reason = prompt('주문 취소 사유를 입력해주세요:');
    if (!reason || reason.trim() === '') {
        alert('취소 사유를 입력해주세요.');
        return;
    }

    if (!confirm('정말로 주문을 취소하시겠습니까?')) {
        return;
    }

    try {
        await apiRequest(`/orders/${orderId}/cancel`, {
            method: 'PUT',
            body: JSON.stringify(reason)
        });

        alert('주문이 취소되었습니다.');
        loadOrderHistory(); // 주문 목록 새로고침
    } catch (error) {
        console.error('주문 취소 실패:', error);
        showError('주문 취소 중 오류가 발생했습니다.');
    }
}

// 리뷰 작성 모달 표시
function showReviewModal(orderId) {
    alert(`주문 ${orderId}에 대한 리뷰 작성 페이지로 이동합니다.`);
    // 실제로는 리뷰 작성 모달이나 페이지를 열어야 함
}

// 리뷰 수정
function editReview(reviewId) {
    alert(`리뷰 ${reviewId} 수정 페이지로 이동합니다.`);
    // 실제로는 리뷰 수정 모달이나 페이지를 열어야 함
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
        loadMyReviews(); // 리뷰 목록 새로고침
    } catch (error) {
        console.error('리뷰 삭제 실패:', error);
        showError('리뷰 삭제 중 오류가 발생했습니다.');
    }
}

// 주문 상세 모달 표시
function showOrderDetailModal(orderDetail) {
    let modalHtml = `
        <div class="modal-overlay" onclick="closeModal()">
            <div class="modal-content" onclick="event.stopPropagation()">
                <div class="modal-header">
                    <h3>주문 상세 정보</h3>
                    <button onclick="closeModal()" class="close-btn">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="order-info">
                        <p><strong>주문번호:</strong> ${orderDetail.orderId}</p>
                        <p><strong>주문일:</strong> ${formatDate(orderDetail.orderDate)}</p>
                        <p><strong>주문상태:</strong> ${orderDetail.orderStatus}</p>
                        <p><strong>결제방법:</strong> ${orderDetail.paymentMethod}</p>
                        <p><strong>결제상태:</strong> ${orderDetail.paymentStatus}</p>
                    </div>
                    <div class="shipping-info">
                        <h4>배송 정보</h4>
                        <p><strong>배송주소:</strong> ${orderDetail.shippingAddress}</p>
                        <p><strong>배송연락처:</strong> ${orderDetail.shippingPhone}</p>
                        ${orderDetail.orderMemo ? `<p><strong>주문메모:</strong> ${orderDetail.orderMemo}</p>` : ''}
                    </div>
                    <div class="order-items">
                        <h4>주문 상품</h4>
                        ${orderDetail.orderDetails.map(item => `
                            <div class="order-item-detail">
                                <img src="${item.book.bookImage || '/images/default-book.jpg'}" alt="${item.book.bookName}" class="item-image">
                                <div class="item-info">
                                    <h5>${item.book.bookName}</h5>
                                    <p>저자: ${item.book.author} | 출판사: ${item.book.publisher}</p>
                                    <p>수량: ${item.quantity}개 | 단가: ${item.unitPrice.toLocaleString()}원</p>
                                    <p><strong>소계: ${item.totalPrice.toLocaleString()}원</strong></p>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                    <div class="order-total">
                        <h4>총 결제금액: ${orderDetail.totalAmount.toLocaleString()}원</h4>
                    </div>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHtml);
}

// 모달 닫기
function closeModal() {
    const modal = document.querySelector('.modal-overlay');
    if (modal) {
        modal.remove();
    }
}

// 페이지네이션 생성
function createPagination(currentPage, totalPages, functionName) {
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
    return paginationHtml;
}

// 날짜 포맷팅
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// 에러 메시지 표시
function showError(message) {
    alert(message);
}

// 검색 기능
document.addEventListener('DOMContentLoaded', function() {
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
});