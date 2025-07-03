// 장바구니 데이터 로드
async function loadCart() {
    try {
        const response = await fetch('/api/cart', {
            credentials: 'include'
        });

        if (response.status === 401) {
            // 로그인이 필요한 경우
            window.location.href = '/login';
            return;
        }

        const data = await response.json();
        const cartItems = document.getElementById('cartItems');

        if (response.ok && data.success) {
            const cartData = data.data;

            if (cartData.items.length === 0) {
                cartItems.innerHTML = `
                    <div class="empty-cart">
                        <p>장바구니가 비어있습니다.</p>
                        <button class="btn btn-primary" onclick="continueShopping()">쇼핑하러 가기</button>
                    </div>
                `;
                updateSummaryDisplay(cartData);
                return;
            }

            cartItems.innerHTML = cartData.items.map(item => `
                <div class="cart-item" data-cart-id="${item.cartId}">
                    <input type="checkbox" class="item-checkbox" checked onchange="updateSummaryDisplay()">
                    <div class="item-image">${item.book.bookImage || '책 이미지'}</div>
                    <div class="item-info">
                        <div class="item-title">${item.book.bookName}</div>
                        <div class="item-author">${item.book.author} | ${item.book.publisher}</div>
                    </div>
                    <div class="quantity-control">
                        <button onclick="changeQuantity(${item.cartId}, ${item.quantity - 1})">-</button>
                        <input type="number" value="${item.quantity}" min="1" onchange="updateQuantity(${item.cartId}, this.value)">
                        <button onclick="changeQuantity(${item.cartId}, ${item.quantity + 1})">+</button>
                    </div>
                    <div class="item-price">${item.totalPrice ? item.totalPrice.toLocaleString() + '원' : '0원'}</div>
                    <button class="delete-btn" onclick="removeItem(${item.cartId})">삭제</button>
                </div>
            `).join('');

            updateSummaryDisplay(cartData);
        }
    } catch (error) {
        console.error('장바구니 로드 오류:', error);
        alert('장바구니 로드 중 오류가 발생했습니다.');
    }
}

// 수량 변경
async function changeQuantity(cartId, newQuantity) {
    if (newQuantity < 1) return;

    try {
        const response = await fetch(`/api/cart/${cartId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({ quantity: newQuantity })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            await loadCart(); // 장바구니 새로고침
        } else {
            alert(data.message || '수량 변경에 실패했습니다.');
        }
    } catch (error) {
        console.error('수량 변경 오류:', error);
        alert('수량 변경 중 오류가 발생했습니다.');
    }
}

async function updateQuantity(cartId, quantity) {
    const newQuantity = Math.max(1, parseInt(quantity) || 1);
    await changeQuantity(cartId, newQuantity);
}

// 아이템 삭제
async function removeItem(cartId) {
    if (!confirm('상품을 장바구니에서 삭제하시겠습니까?')) return;

    try {
        const response = await fetch(`/api/cart/${cartId}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        const data = await response.json();

        if (response.ok && data.success) {
            await loadCart(); // 장바구니 새로고침
        } else {
            alert(data.message || '삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('아이템 삭제 오류:', error);
        alert('삭제 중 오류가 발생했습니다.');
    }
}

// 선택된 아이템 삭제
async function deleteSelected() {
    const checkboxes = document.querySelectorAll('.item-checkbox:checked');
    if (checkboxes.length === 0) {
        alert('삭제할 상품을 선택해주세요.');
        return;
    }

    if (!confirm('선택된 상품을 장바구니에서 삭제하시겠습니까?')) return;

    try {
        const cartIds = Array.from(checkboxes).map(cb =>
            parseInt(cb.closest('.cart-item').dataset.cartId)
        );

        const response = await fetch('/api/cart', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(cartIds)
        });

        const data = await response.json();

        if (response.ok && data.success) {
            await loadCart(); // 장바구니 새로고침
        } else {
            alert(data.message || '삭제에 실패했습니다.');
        }
    } catch (error) {
        console.error('선택 삭제 오류:', error);
        alert('삭제 중 오류가 발생했습니다.');
    }
}

// 전체 선택/해제
document.addEventListener('DOMContentLoaded', function() {
    const selectAllCheckbox = document.getElementById('selectAll');
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            const checkboxes = document.querySelectorAll('.item-checkbox');
            checkboxes.forEach(cb => cb.checked = this.checked);
            updateSummaryDisplay();
        });
    }
});

// 개별 체크박스 변경 시 전체선택 상태 업데이트
document.addEventListener('change', function(e) {
    if (e.target.classList.contains('item-checkbox')) {
        const allCheckboxes = document.querySelectorAll('.item-checkbox');
        const checkedCheckboxes = document.querySelectorAll('.item-checkbox:checked');
        const selectAllCheckbox = document.getElementById('selectAll');
        if (selectAllCheckbox) {
            selectAllCheckbox.checked = allCheckboxes.length === checkedCheckboxes.length;
        }
        updateSummaryDisplay();
    }
});

// 금액 요약 표시 업데이트
function updateSummaryDisplay(cartData = null) {
    if (cartData) {
        // API에서 받은 데이터 사용
        document.getElementById('subtotal').textContent = cartData.subtotalAmount.toLocaleString() + '원';
        document.getElementById('shippingCost').textContent = cartData.shippingCost.toLocaleString() + '원';
        document.getElementById('totalAmount').textContent = cartData.totalAmount.toLocaleString() + '원';
    } else {
        // 체크박스 상태에 따른 계산 (간단 구현)
        // 실제로는 서버에 다시 요청해서 계산하는 것이 정확함
        const checkedBoxes = document.querySelectorAll('.item-checkbox:checked');
        // 여기서는 간단히 처리하고 실제로는 API 재호출 권장
    }
}

// 쇼핑 계속하기
function continueShopping() {
    window.location.href = '/';
}

// 주문하기
async function proceedToCheckout() {
    const checkedBoxes = document.querySelectorAll('.item-checkbox:checked');

    if (checkedBoxes.length === 0) {
        alert('주문할 상품을 선택해주세요.');
        return;
    }

    // 선택된 아이템들로 주문 생성
    const selectedItems = Array.from(checkedBoxes).map(cb => {
        const cartItem = cb.closest('.cart-item');
        const cartId = parseInt(cartItem.dataset.cartId);
        const quantity = parseInt(cartItem.querySelector('input[type="number"]').value);
        return { cartId, quantity };
    });

    // 주문 페이지로 이동하거나 주문 API 호출
    // 여기서는 간단히 alert 처리
    alert('주문 기능은 추후 구현 예정입니다.');
}

// 주문서 모달 열기
function openOrderModal() {
    // 배송지/연락처 자동 입력
    fetch('/api/members/login-status', { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (data.success && data.data && data.data.isLoggedIn && data.data.member) {
                document.getElementById('orderAddress').value = data.data.member.address || '';
                document.getElementById('orderPhone').value = data.data.member.phone || '';
            }
        });
    document.getElementById('orderModal').style.display = 'block';
}

function closeOrderModal() {
    document.getElementById('orderModal').style.display = 'none';
}

// 주문서 제출(카카오페이 결제)
document.getElementById('orderForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const address = document.getElementById('orderAddress').value.trim();
    const phone = document.getElementById('orderPhone').value.trim();
    const memo = document.getElementById('orderMemo').value.trim();
    const paymentMethod = document.getElementById('paymentMethod').value;

    // 주문 상품: 선택된 상품이 있으면 그것만, 없으면 전체
    const checkedBoxes = document.querySelectorAll('.item-checkbox:checked');
    let items = Array.from(checkedBoxes).map(cb => {
        const cartItem = cb.closest('.cart-item');
        const cartId = parseInt(cartItem.dataset.cartId);
        const quantity = parseInt(cartItem.querySelector('input[type="number"]').value);
        const bookId = cartItem.querySelector('.item-title').dataset.bookId ? parseInt(cartItem.querySelector('.item-title').dataset.bookId) : null;
        return { cartId, quantity, bookId };
    });
    if (items.length === 0) {
        // 전체 상품 주문
        const allItems = document.querySelectorAll('.cart-item');
        items = Array.from(allItems).map(cartItem => {
            const cartId = parseInt(cartItem.dataset.cartId);
            const quantity = parseInt(cartItem.querySelector('input[type="number"]').value);
            const bookId = cartItem.querySelector('.item-title').dataset.bookId ? parseInt(cartItem.querySelector('.item-title').dataset.bookId) : null;
            return { cartId, quantity, bookId };
        });
    }
    if (items.length === 0) {
        alert('주문할 상품이 없습니다.');
        return;
    }

    // 주문 정보
    const orderItems = items.map(item => ({
        bookId: item.bookId,
        quantity: item.quantity,
        cartId: item.cartId
    }));
    const orderData = {
        orderItems,
        shippingAddress: address,
        shippingPhone: phone,
        orderMemo: memo,
        paymentMethod
    };

    // TODO: 카카오페이 결제 준비 API 호출(다음 단계)
    try {
        const res = await fetch('/api/pay/kakao/ready', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(orderData)
        });
        const result = await res.json();
        if (res.ok && result.success && result.data && result.data.next_redirect_pc_url) {
            window.location.href = result.data.next_redirect_pc_url; // 카카오페이 결제창 이동
        } else {
            alert(result.message || '카카오페이 결제 준비 중 오류가 발생했습니다.');
        }
    } catch (e) {
        alert('카카오페이 결제 준비 중 오류가 발생했습니다.');
    }
    closeOrderModal();
});

// 페이지 로드 시 장바구니 로드
window.addEventListener('load', loadCart);