function addToCart() {
    const book = {
        title: '채식주의자',
        author: '한강',
        publisher: '창비',
        price: 13500,
        image: '책 이미지'
    };

    // 실제로는 서버에 장바구니 추가 요청을 보내야 함
    let cart = JSON.parse(localStorage.getItem('cart') || '[]');

    // 이미 장바구니에 있는지 확인
    const existingItem = cart.find(item => item.title === book.title);
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({...book, quantity: 1});
    }

    localStorage.setItem('cart', JSON.stringify(cart));
    alert('장바구니에 추가되었습니다.');
}

function buyNow() {
    addToCart();
    window.location.href = 'cart.html';
}

function writeReview() {
    // 실제로는 로그인 체크 후 리뷰 작성 모달을 띄워야 함
    const isLoggedIn = false; // 실제로는 세션에서 확인

    if (!isLoggedIn) {
        alert('리뷰 작성을 위해 로그인이 필요합니다.');
        window.location.href = 'login.html';
        return;
    }

    // 리뷰 작성 모달 또는 페이지로 이동
    alert('리뷰 작성 페이지로 이동합니다.');
}

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