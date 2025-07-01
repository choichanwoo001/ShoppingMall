let isIdChecked = false;

// 아이디 중복 확인
async function checkDuplicateId() {
    const userId = document.getElementById('userId').value;
    const messageEl = document.getElementById('idCheckMessage');

    if (!userId) {
        messageEl.style.color = 'red';
        messageEl.textContent = '아이디를 입력해주세요.';
        return;
    }

    // 아이디 유효성 검사
    const idPattern = /^[a-zA-Z0-9]{4,12}$/;
    if (!idPattern.test(userId)) {
        messageEl.style.color = 'red';
        messageEl.textContent = '아이디는 4-12자의 영문, 숫자만 사용 가능합니다.';
        isIdChecked = false;
        return;
    }

    try {
        // API 호출
        const response = await fetch('/api/members/check-id', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ memberId: userId })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            if (data.data.isDuplicate) {
                messageEl.style.color = 'red';
                messageEl.textContent = '이미 사용 중인 아이디입니다.';
                isIdChecked = false;
            } else {
                messageEl.style.color = 'green';
                messageEl.textContent = '사용 가능한 아이디입니다.';
                isIdChecked = true;
            }
        } else {
            messageEl.style.color = 'red';
            messageEl.textContent = data.message || '아이디 확인 중 오류가 발생했습니다.';
            isIdChecked = false;
        }
    } catch (error) {
        console.error('아이디 중복 확인 오류:', error);
        messageEl.style.color = 'red';
        messageEl.textContent = '아이디 확인 중 오류가 발생했습니다.';
        isIdChecked = false;
    }
}

// 비밀번호 확인 체크
document.getElementById('passwordConfirm').addEventListener('input', function() {
    const password = document.getElementById('password').value;
    const passwordConfirm = this.value;
    const messageEl = document.getElementById('passwordCheckMessage');

    if (passwordConfirm && password !== passwordConfirm) {
        messageEl.style.color = 'red';
        messageEl.textContent = '비밀번호가 일치하지 않습니다.';
    } else if (passwordConfirm && password === passwordConfirm) {
        messageEl.style.color = 'green';
        messageEl.textContent = '비밀번호가 일치합니다.';
    } else {
        messageEl.textContent = '';
    }
});

// 아이디 입력 시 중복확인 상태 초기화
document.getElementById('userId').addEventListener('input', function() {
    isIdChecked = false;
    document.getElementById('idCheckMessage').textContent = '';
});

// 연락처 자동 포맷팅
document.getElementById('phone').addEventListener('input', function() {
    let value = this.value.replace(/[^0-9]/g, '');
    if (value.length >= 3 && value.length <= 7) {
        value = value.replace(/(\d{3})(\d{1,4})/, '$1-$2');
    } else if (value.length > 7) {
        value = value.replace(/(\d{3})(\d{4})(\d{1,4})/, '$1-$2-$3');
    }
    this.value = value;
});

// 회원가입 폼 처리
document.getElementById('signupForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const userId = document.getElementById('userId').value;
    const password = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('passwordConfirm').value;
    const userName = document.getElementById('userName').value;
    const email = document.getElementById('email').value;
    const phone = document.getElementById('phone').value;
    const address = document.getElementById('address').value;

    // 유효성 검사
    if (!isIdChecked) {
        alert('아이디 중복확인을 해주세요.');
        return;
    }

    if (password !== passwordConfirm) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
    }

    // 비밀번호 강도 검사
    const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    if (!passwordPattern.test(password)) {
        alert('비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 모두 포함해야 합니다.');
        return;
    }

    // 이메일 형식 검사
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(email)) {
        alert('올바른 이메일 형식을 입력해주세요.');
        return;
    }

    // 연락처 형식 검사
    const phonePattern = /^010-\d{4}-\d{4}$/;
    if (!phonePattern.test(phone)) {
        alert('연락처는 010-0000-0000 형식으로 입력해주세요.');
        return;
    }

    try {
        // API 호출
        const response = await fetch('/api/members/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                memberId: userId,
                password: password,
                passwordConfirm: passwordConfirm,
                memberName: userName,
                email: email,
                phone: phone,
                address: address
            })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            alert(data.message || '회원가입이 완료되었습니다.');
            window.location.href = '/login';
        } else {
            alert(data.message || '회원가입에 실패했습니다.');
        }
    } catch (error) {
        console.error('회원가입 오류:', error);
        alert('회원가입 중 오류가 발생했습니다.');
    }
});