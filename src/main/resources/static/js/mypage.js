// 탭 전환
function showTab(tabName) {
    // 모든 탭 버튼 비활성화
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    // 모든 탭 콘텐츠 숨기기
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));

    // 선택된 탭 활성화
    event.target.classList.add('active');
    document.getElementById(tabName).classList.add('active');
}

// 프로필 수정
function editProfile() {
    alert('프로필 수정 페이지로 이동합니다.');
}

// 리뷰 작성
function writeReview() {
    alert('리뷰 작성 페이지로 이동합니다.');
}

// 배송 추적
function trackOrder() {
    alert('배송 추적 페이지로 이동합니다.');
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