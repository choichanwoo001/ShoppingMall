// 로그인 폼 처리
document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const userId = document.getElementById('userId').value;
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('rememberMe').checked;

    // 유효성 검사
    if (!userId || !password) {
        alert('아이디와 비밀번호를 모두 입력해주세요.');
        return;
    }

    // 아이디 기억하기 처리
    if (rememberMe) {
        localStorage.setItem('rememberedUserId', userId);
    } else {
        localStorage.removeItem('rememberedUserId');
    }

    // 실제로는 서버에 로그인 요청을 보내야 함
    alert('로그인이 완료되었습니다.');
    window.location.href = 'index.html';
});

// 페이지 로드 시 저장된 아이디 불러오기
window.addEventListener('load', function() {
    const rememberedUserId = localStorage.getItem('rememberedUserId');
    if (rememberedUserId) {
        document.getElementById('userId').value = rememberedUserId;
        document.getElementById('rememberMe').checked = true;
    }
});

// 비밀번호 찾기
function resetPassword() {
    const email = prompt('가입 시 등록한 이메일을 입력하세요:');
    if (email) {
        alert(`${email}로 비밀번호 재설정 메일을 발송했습니다.`);
    }
}