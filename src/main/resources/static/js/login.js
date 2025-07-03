// 로그인 폼 처리
document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const userId = document.getElementById('userId').value;
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('rememberMe').checked;

    // 유효성 검사
    if (!userId || !password) {
        alert('아이디와 비밀번호를 모두 입력해주세요.');
        return;
    }

    try {
        // API 호출
        const response = await fetch('/api/members/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include', // 세션 쿠키 포함
            body: JSON.stringify({
                memberId: userId,
                password: password,
                rememberMe: rememberMe
            })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            // 아이디 기억하기 처리
            if (rememberMe) {
                localStorage.setItem('rememberedUserId', userId);
            } else {
                localStorage.removeItem('rememberedUserId');
            }

            alert(data.message || '로그인이 완료되었습니다.');
            window.location.href = '/';
            showLogoutUI(data.memberName);
        } else {
            alert(data.message || '로그인에 실패했습니다.');
        }
    } catch (error) {
        console.error('로그인 오류:', error);
        alert('로그인 중 오류가 발생했습니다.');
    }
});

// 페이지 로드 시 저장된 아이디 불러오기
window.addEventListener('load', function() {
    const rememberedUserId = localStorage.getItem('rememberedUserId');
    if (rememberedUserId) {
        document.getElementById('userId').value = rememberedUserId;
        document.getElementById('rememberMe').checked = true;
    }
});

// 비밀번호 찾기 (모달용)
async function submitResetPassword() {
    const email = document.getElementById('resetEmail').value;
    if (!email) {
        alert('이메일을 입력하세요.');
        return;
    }
    try {
        const response = await fetch('/api/members/reset-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email: email })
        });
        const data = await response.json();
        alert(data.message || '이메일로 임시 비밀번호를 발송했습니다.');
        document.getElementById('resetEmail').value = '';
        closeResetModal();
    } catch (error) {
        console.error('비밀번호 재설정 오류:', error);
        alert('비밀번호 재설정 중 오류가 발생했습니다.');
    }
}

// 로그아웃 기능
async function logout() {
    try {
        const response = await fetch('/api/members/logout', {
            method: 'POST',
            credentials: 'include'
        });
        const data = await response.json();
        if (data.success) {
            alert('로그아웃되었습니다.');
            window.location.reload();
        } else {
            alert(data.message || '로그아웃에 실패했습니다.');
        }
    } catch (error) {
        alert('로그아웃 중 오류가 발생했습니다.');
    }
}

// 로그인 성공 시 로그아웃 UI 표시
function showLogoutUI(memberName) {
    document.getElementById('loginForm').style.display = 'none';
    const section = document.getElementById('logoutSection');
    section.style.display = 'block';
    document.getElementById('logoutGreeting').textContent = `${memberName ? memberName + '님, ' : ''}로그인 상태입니다.`;
}