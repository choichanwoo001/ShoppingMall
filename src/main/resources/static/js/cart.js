// 장바구니 데이터 로드
function loadCart() {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    const cartItems = document.getElementById('cartItems');

    if (cart.length === 0) {
        cartItems.innerHTML = `
                    <div class="empty-cart">
                        <p>장바구니가 비어있습니다.</p>
                        <button class="btn btn-primary" onclick="continueShopping()">쇼핑하러 가기</button>
                    </div>
                `;
        updateSummary();
        return;
    }

    cartItems.innerHTML = cart.map((item, index) => `
                <div class="cart-item" data-index="${index}">
                    <input type="checkbox" class="item-checkbox" checked onchange="updateSummary()">
                    <div class="item-image">${item.image}</div>
                    <div class="item-info">
                        <div class="item-title">${item.title}</div>
                        <div class="item-author">${item.author} | ${item.publisher}</div>
                    </div>
                    <div class="quantity-control">
                        <button onclick="changeQuantity(${index}, -1)">-</button>
                        <input type="number" value="${item.quantity}" min="1" onchange="updateQuantity(${index}, this.value)">
                        <button onclick="changeQuantity(${index}, 1)">+</button>
                    </div>
                    <div class="item-price">${(item.price * item.quantity).toLocaleString()}원</div>
                    <button class="delete-btn" onclick="removeItem(${index})">삭제</button>
                </div>
            `).join('');

    updateSummary();
}

// 수량 변경
function changeQuantity(index, change) {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    cart[index].quantity = Math.max(1, cart[index].quantity + change);
    localStorage.setItem('cart', JSON.stringify(cart));
    loadCart();
}

function updateQuantity(index, quantity) {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    cart[index].quantity = Math.max(1, parseInt(quantity) || 1);
    localStorage.setItem('cart', JSON.stringify(cart));
    loadCart();
}

// 아이템 삭제
function removeItem(index) {
    if (confirm('상품을 장바구니에서 삭제하시겠습니까?')) {
        const cart = JSON.parse(localStorage.getItem('cart') || '[]');
        cart.splice(index, 1);
        localStorage.setItem('cart', JSON.stringify(cart));
        loadCart();
    }
}

// 선택된 아이템 삭제
function deleteSelected() {
    const checkboxes = document.querySelectorAll('.item-checkbox:checked');
    if (checkboxes.length === 0) {
        alert('삭제할 상품을 선택해주세요.');
        return;
    }

    if (confirm('선택된 상품을 장바구니에서 삭제하시겠습니까?')) {
        const cart = JSON.parse(localStorage.getItem('cart') || '[]');
        const indicesToRemove = Array.from(checkboxes).map(cb => parseInt(cb.closest('.cart-item').dataset.index));

        // 인덱스를 역순으로 정렬하여 삭제 (뒤에서부터 삭제해야 인덱스가 변경되지 않음)
        indicesToRemove.sort((a, b) => b - a).forEach(index => {
            cart.splice(index, 1);
        });

        localStorage.setItem('cart', JSON.stringify(cart));
        loadCart();
    }
}

// 전체 선택/해제
document.getElementById('selectAll').addEventListener('change', function() {
    const checkboxes = document.querySelectorAll('.item-checkbox');
    checkboxes.forEach(cb => cb.checked = this.checked);
    updateSummary();
});

// 개별 체크박스 변경 시 전체선택 상태 업데이트
document.addEventListener('change', function(e) {
    if (e.target.classList.contains('item-checkbox')) {
        const allCheckboxes = document.querySelectorAll('.item-checkbox');
        const checkedCheckboxes = document.querySelectorAll('.item-checkbox:checked');
        document.getElementById('selectAll').checked = allCheckboxes.length === checkedCheckboxes.length;
    }
});

// 금액 계산 및 업데이트
function updateSummary() {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    const checkedBoxes = document.querySelectorAll('.item-checkbox:checked');

    let subtotal = 0;
    checkedBoxes.forEach(cb => {
        const index = parseInt(cb.closest('.cart-item').dataset.index);
        const item = cart[index];
        if (item) {
            subtotal += item.price * item.quantity;
        }
    });

    const shippingCost = subtotal >= 30000 ? 0 : 2500; // 3만원 이상 무료배송
    const total = subtotal + shippingCost;

    document.getElementById('subtotal').textContent = subtotal.toLocaleString() + '원';
    document.getElementById('shippingCost').textContent = shippingCost.toLocaleString() + '원';
    document.getElementById('totalAmount').textContent = total.toLocaleString() + '원';
}

// 쇼핑 계속하기
function continueShopping() {
    window.location.href = 'index.html';
}

// 주문하기
function proceedToCheckout() {
    const checkedBoxes = document.querySelectorAll('.item-checkbox:checked');

    if (checkedBoxes.length === 0) {
        alert('주문할 상품을 선택해주세요.');
        return;
    }

    // 실제로는 결제 페이지로 이동
    alert('주문 페이지로 이동합니다.');
}

// 페이지 로드 시 장바구니 로드
window.addEventListener('load', loadCart);

// 검색 기능
document.querySelector('.search-btn').addEventListener('click', function() {
    const searchInput = document.querySelector('.search-input');
    const searchTerm = searchInput.value.trim();
    if (searchTerm) {
        alert(`"${searchTerm}"으로 검색합니다.`);
    }
});

// 엔터키로 검색
document.querySelector('.search-input').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        document.querySelector('.search-btn').click();
    }
});