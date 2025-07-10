// 관리자 페이지 JavaScript

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    initializeAdminPage();
});

// 관리자 페이지 초기화
function initializeAdminPage() {
    // 대시보드 통계 로드
    if (document.getElementById('totalBooks')) {
        loadDashboardStats();
    }
    
    // 검색 폼 이벤트 리스너
    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', handleSearch);
    }
    
    // 페이지 크기 변경 이벤트
    const sizeSelect = document.getElementById('size');
    if (sizeSelect) {
        sizeSelect.addEventListener('change', handlePageSizeChange);
    }
    
    // 페이지 크기 변경 이벤트 (모든 페이지에서 동작)
    const pageSizeSelect = document.getElementById('pageSize');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', handlePageSizeChange);
    }
    
    // 모달 이벤트 리스너
    setupModalEvents();
    
    // 폼 이벤트 리스너
    setupFormEvents();
}

// 대시보드 통계 로드
async function loadDashboardStats() {
    try {
        const response = await fetch('/admin/api/stats');
        if (response.ok) {
            const stats = await response.json();
            updateDashboardStats(stats);
        }
    } catch (error) {
        console.error('통계 로드 실패:', error);
    }
}

// 대시보드 통계 업데이트
function updateDashboardStats(stats) {
    const totalBooks = document.getElementById('totalBooks');
    const todayOrders = document.getElementById('todayOrders');
    const totalMembers = document.getElementById('totalMembers');
    const outOfStock = document.getElementById('outOfStock');
    
    if (totalBooks) totalBooks.textContent = stats.totalBooks || 0;
    if (todayOrders) todayOrders.textContent = stats.todayOrders || 0;
    if (totalMembers) totalMembers.textContent = stats.totalMembers || 0;
    if (outOfStock) outOfStock.textContent = stats.outOfStock || 0;
}

// 검색 처리
function handleSearch(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    // 페이지를 1페이지로 리셋
    formData.set('page', '0');
    
    // URL 파라미터로 변환
    const params = new URLSearchParams(formData);
    
    // 현재 URL의 기본 경로 가져오기
    const currentPath = window.location.pathname;
    
    // 검색 파라미터와 함께 페이지 이동
    window.location.href = currentPath + '?' + params.toString();
}

// 페이지 크기 변경 처리
function handlePageSizeChange(event) {
    const form = document.getElementById('searchForm');
    if (form) {
        const formData = new FormData(form);
        
        // 페이지를 1페이지로 리셋
        formData.set('page', '0');
        
        // URL 파라미터로 변환
        const params = new URLSearchParams(formData);
        
        // 현재 URL의 기본 경로 가져오기
        const currentPath = window.location.pathname;
        
        // 페이지 크기 변경과 함께 페이지 이동
        window.location.href = currentPath + '?' + params.toString();
    }
}

// 폼 초기화
function resetForm() {
    const form = document.getElementById('searchForm');
    if (form) {
        // 모든 입력값 비우기
        form.reset();
        
        // 현재 URL의 기본 경로로 이동 (검색 파라미터 제거)
        const currentPath = window.location.pathname;
        window.location.href = currentPath;
    }
}

// resetForm을 window에 등록 (HTML onclick에서 정상 호출되도록)
window.resetForm = resetForm;

// 모달 이벤트 설정
function setupModalEvents() {
    const modals = document.querySelectorAll('.modal');
    const closeButtons = document.querySelectorAll('.close');
    
    // 모달 닫기 버튼
    closeButtons.forEach(button => {
        button.addEventListener('click', closeModal);
    });
    
    // 모달 외부 클릭 시 닫기
    modals.forEach(modal => {
        modal.addEventListener('click', function(event) {
            if (event.target === modal) {
                closeModal();
            }
        });
    });
}

// 모달 닫기
function closeModal() {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.style.display = 'none';
    });
}

// 폼 이벤트 설정
function setupFormEvents() {
    // 상품 등록 폼
    const bookRegisterForm = document.getElementById('bookRegisterForm');
    if (bookRegisterForm) {
        bookRegisterForm.addEventListener('submit', handleBookRegister);
    }
    
    // 주문 상태 변경 폼
    const statusUpdateForm = document.getElementById('statusUpdateForm');
    if (statusUpdateForm) {
        statusUpdateForm.addEventListener('submit', handleStatusUpdate);
    }
    
    // 재고 수정 폼
    const inventoryForm = document.getElementById('inventoryForm');
    if (inventoryForm) {
        inventoryForm.addEventListener('submit', handleInventoryUpdate);
    }
    
    // 재고 수정 버튼 이벤트 리스너
    document.addEventListener('click', function(event) {
        if (event.target.closest('.update-inventory-btn')) {
            const button = event.target.closest('.update-inventory-btn');
            const bookId = button.getAttribute('data-book-id');
            const bookName = button.getAttribute('data-book-name');
            const stockQuantity = button.getAttribute('data-stock-quantity');
            updateInventory(bookId, bookName, stockQuantity);
        }
    });
}

// 상품 등록 처리
async function handleBookRegister(event) {
    event.preventDefault();
    
    const formData = new FormData(event.target);
    const bookData = Object.fromEntries(formData.entries());
    
    try {
        const response = await fetch('/admin/books', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(bookData)
        });
        
        if (response.ok) {
            const result = await response.json();
            alert(result.message || '상품이 성공적으로 등록되었습니다.');
            window.location.href = '/admin/books';
        } else {
            const error = await response.json();
            alert(error.message || '상품 등록에 실패했습니다.');
        }
    } catch (error) {
        console.error('상품 등록 실패:', error);
        alert('상품 등록 중 오류가 발생했습니다.');
    }
}

// 상품 삭제 기능 제거됨

// 주문 상태 변경 모달 열기
function updateOrderStatus(orderId) {
    const modal = document.getElementById('statusModal');
    const orderIdInput = document.getElementById('orderId');
    
    if (modal && orderIdInput) {
        orderIdInput.value = orderId;
        modal.style.display = 'block';
    }
}

// 주문 상태 변경 확인
async function confirmStatusUpdate() {
    const orderId = document.getElementById('orderId').value;
    const newStatus = document.getElementById('newStatus').value;
    
    if (!newStatus) {
        alert('새로운 상태를 선택해주세요.');
        return;
    }
    
    try {
        const response = await fetch(`/admin/orders/${orderId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ status: newStatus })
        });
        
        if (response.ok) {
            const result = await response.json();
            alert(result.message || '주문 상태가 성공적으로 변경되었습니다.');
            closeModal();
            location.reload();
        } else {
            const error = await response.json();
            alert(error.message || '주문 상태 변경에 실패했습니다.');
        }
    } catch (error) {
        console.error('주문 상태 변경 실패:', error);
        alert('주문 상태 변경 중 오류가 발생했습니다.');
    }
}

// 주문 상태 변경 처리
async function handleStatusUpdate(event) {
    event.preventDefault();
    await confirmStatusUpdate();
}

// 회원 상태 변경 모달 열기
function updateMemberStatus(memberId) {
    const modal = document.getElementById('memberStatusModal');
    const memberIdInput = document.getElementById('memberId');
    
    if (modal && memberIdInput) {
        memberIdInput.value = memberId;
        modal.style.display = 'block';
    }
}

// 회원 상태 변경 확인
async function confirmMemberStatusUpdate() {
    const memberId = document.getElementById('memberId').value;
    const newStatus = document.getElementById('newMemberStatus').value;
    const statusReason = document.getElementById('statusReason').value;
    
    if (!newStatus) {
        alert('새로운 상태를 선택해주세요.');
        return;
    }
    
    try {
        const response = await fetch(`/admin/members/${memberId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ 
                memberStatus: newStatus,
                statusReason: statusReason 
            })
        });
        
        if (response.ok) {
            const result = await response.json();
            alert(result.message || '회원 상태가 성공적으로 변경되었습니다.');
            closeModal();
            location.reload();
        } else {
            const error = await response.json();
            alert(error.message || '회원 상태 변경에 실패했습니다.');
        }
    } catch (error) {
        console.error('회원 상태 변경 실패:', error);
        alert('회원 상태 변경 중 오류가 발생했습니다.');
    }
}

// 재고 수정 모달 열기
function updateInventory(bookId, bookName, currentQuantity) {
    const modal = document.getElementById('inventoryModal');
    const bookIdInput = document.getElementById('bookId');
    const bookNameInput = document.getElementById('bookName');
    const currentQuantityInput = document.getElementById('currentQuantity');
    
    // bookName이 undefined/null이면 테이블에서 찾아서 할당 (예외 방지)
    if ((!bookName || bookName === 'undefined') && bookId) {
        // 테이블에서 해당 bookId의 책 이름을 찾아서 할당
        const row = document.querySelector(`button[data-book-id='${bookId}']`)?.closest('tr');
        if (row) {
            const nameCell = row.querySelector('td:nth-child(3)'); // 3번째 컬럼: 책 이름
            if (nameCell) {
                bookName = nameCell.textContent.trim();
            }
        }
    }
    
    if (modal && bookIdInput && bookNameInput && currentQuantityInput) {
        bookIdInput.value = bookId;
        bookNameInput.value = bookName || '';
        currentQuantityInput.value = currentQuantity;
        modal.style.display = 'block';
    }
}

// 재고 수정 확인
async function confirmInventoryUpdate() {
    const bookId = document.getElementById('bookId').value;
    const newQuantity = document.getElementById('newQuantity').value;
    
    if (!newQuantity || newQuantity < 0) {
        alert('올바른 재고 수량을 입력해주세요.');
        return;
    }
    
    try {
        const response = await fetch(`/admin/inventory/${bookId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ quantity: parseInt(newQuantity) })
        });
        
        if (response.ok) {
            const result = await response.json();
            alert(result.message || '재고가 성공적으로 수정되었습니다.');
            closeModal();
            location.reload();
        } else {
            const error = await response.json();
            alert(error.message || '재고 수정에 실패했습니다.');
        }
    } catch (error) {
        console.error('재고 수정 실패:', error);
        alert('재고 수정 중 오류가 발생했습니다.');
    }
}

// 재고 수정 처리
async function handleInventoryUpdate(event) {
    event.preventDefault();
    await confirmInventoryUpdate();
}

// 카테고리 연동 (상품 등록 페이지용)
function setupCategoryCascading() {
    const categoryTop = document.getElementById('categoryTop');
    const categoryMiddle = document.getElementById('categoryMiddle');
    const categoryBottom = document.getElementById('categoryBottom');
    
    if (categoryTop) {
        categoryTop.addEventListener('change', function() {
            loadCategoryMiddle(this.value);
            categoryBottom.innerHTML = '<option value="">선택하세요</option>';
        });
    }
    
    if (categoryMiddle) {
        categoryMiddle.addEventListener('change', function() {
            loadCategoryBottom(categoryTop.value, this.value);
        });
    }
}

// 중위 카테고리 로드
async function loadCategoryMiddle(categoryTop) {
    const categoryMiddle = document.getElementById('categoryMiddle');
    if (!categoryMiddle) return;
    
    try {
        const response = await fetch(`/admin/api/categories/middle?top=${encodeURIComponent(categoryTop)}`);
        if (response.ok) {
            const categories = await response.json();
            categoryMiddle.innerHTML = '<option value="">선택하세요</option>';
            categories.forEach(category => {
                const option = document.createElement('option');
                option.value = category.categoryName;
                option.textContent = category.categoryName;
                categoryMiddle.appendChild(option);
            });
        }
    } catch (error) {
        console.error('중위 카테고리 로드 실패:', error);
    }
}

// 하위 카테고리 로드
async function loadCategoryBottom(categoryTop, categoryMiddle) {
    const categoryBottom = document.getElementById('categoryBottom');
    if (!categoryBottom) return;
    
    try {
        const response = await fetch(`/admin/api/categories/bottom?top=${encodeURIComponent(categoryTop)}&middle=${encodeURIComponent(categoryMiddle)}`);
        if (response.ok) {
            const categories = await response.json();
            categoryBottom.innerHTML = '<option value="">선택하세요</option>';
            categories.forEach(category => {
                const option = document.createElement('option');
                option.value = category.categoryName;
                option.textContent = category.categoryName;
                categoryBottom.appendChild(option);
            });
        }
    } catch (error) {
        console.error('하위 카테고리 로드 실패:', error);
    }
}

// 유틸리티 함수들
function formatNumber(num) {
    return new Intl.NumberFormat('ko-KR').format(num);
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
}

function formatDateTime(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR');
}

// 성공 메시지 표시
function showSuccess(message) {
    // 간단한 토스트 메시지 구현
    const toast = document.createElement('div');
    toast.className = 'toast success';
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #28a745;
        color: white;
        padding: 15px 20px;
        border-radius: 5px;
        z-index: 10000;
        animation: slideIn 0.3s ease;
    `;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

// 에러 메시지 표시
function showError(message) {
    const toast = document.createElement('div');
    toast.className = 'toast error';
    toast.textContent = message;
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #dc3545;
        color: white;
        padding: 15px 20px;
        border-radius: 5px;
        z-index: 10000;
        animation: slideIn 0.3s ease;
    `;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

// CSS 애니메이션 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
`;
document.head.appendChild(style); 