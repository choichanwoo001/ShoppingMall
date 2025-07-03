let isIdChecked = false;
let validationState = {
    userId: false,
    password: false,
    passwordConfirm: false,
    userName: false,
    email: false,
    phone: false,
    address: false
};

// 유효성 검사 메시지 표시 함수
function showValidationMessage(elementId, message, isValid) {
    const messageEl = document.getElementById(elementId);
    if (messageEl) {
        messageEl.style.color = isValid ? 'green' : 'red';
        messageEl.textContent = message;
    }
}

// 전체 폼 유효성 상태 확인
function checkFormValidity() {
    const submitButton = document.querySelector('button[type="submit"]');
    const allValid = Object.values(validationState).every(state => state) && isIdChecked;

    if (submitButton) {
        submitButton.disabled = !allValid;
        submitButton.style.opacity = allValid ? '1' : '0.6';
    }
}

// 아이디 실시간 유효성 검사
document.getElementById('userId').addEventListener('input', function() {
    const userId = this.value;
    const messageEl = document.getElementById('idCheckMessage');

    // 중복확인 상태 초기화
    isIdChecked = false;

    if (!userId) {
        validationState.userId = false;
        messageEl.textContent = '';
        checkFormValidity();
        return;
    }

    // 아이디 형식 검사
    const idPattern = /^[a-zA-Z0-9]{4,50}$/;
    if (!idPattern.test(userId)) {
        validationState.userId = false;
        showValidationMessage('idCheckMessage', '아이디는 4-50자의 영문, 숫자만 사용 가능합니다.', false);
        checkFormValidity();
        return;
    }

    validationState.userId = true;
    showValidationMessage('idCheckMessage', '형식이 올바릅니다. 중복확인을 해주세요.', false);
    checkFormValidity();
});

// 비밀번호 실시간 유효성 검사
document.getElementById('password').addEventListener('input', function() {
    const password = this.value;
    const passwordConfirm = document.getElementById('passwordConfirm').value;

    if (!password) {
        validationState.password = false;
        showValidationMessage('passwordMessage', '', false);
        checkFormValidity();
        return;
    }

    // 비밀번호 강도 검사
    const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;

    if (password.length < 8) {
        validationState.password = false;
        showValidationMessage('passwordMessage', '비밀번호는 최소 8자 이상이어야 합니다.', false);
    } else if (!/(?=.*[A-Za-z])/.test(password)) {
        validationState.password = false;
        showValidationMessage('passwordMessage', '영문자를 포함해야 합니다.', false);
    } else if (!/(?=.*\d)/.test(password)) {
        validationState.password = false;
        showValidationMessage('passwordMessage', '숫자를 포함해야 합니다.', false);
    } else if (!/(?=.*[@$!%*#?&])/.test(password)) {
        validationState.password = false;
        showValidationMessage('passwordMessage', '특수문자(@$!%*#?&)를 포함해야 합니다.', false);
    } else if (passwordPattern.test(password)) {
        validationState.password = true;
        showValidationMessage('passwordMessage', '사용 가능한 비밀번호입니다.', true);
    } else {
        validationState.password = false;
        showValidationMessage('passwordMessage', '비밀번호 형식이 올바르지 않습니다.', false);
    }

    // 비밀번호 확인 재검사
    if (passwordConfirm) {
        checkPasswordConfirmation();
    }

    checkFormValidity();
});

// 비밀번호 확인 검사 함수
function checkPasswordConfirmation() {
    const password = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('passwordConfirm').value;

    if (!passwordConfirm) {
        validationState.passwordConfirm = false;
        showValidationMessage('passwordCheckMessage', '', false);
        return;
    }

    if (password !== passwordConfirm) {
        validationState.passwordConfirm = false;
        showValidationMessage('passwordCheckMessage', '비밀번호가 일치하지 않습니다.', false);
    } else {
        validationState.passwordConfirm = true;
        showValidationMessage('passwordCheckMessage', '비밀번호가 일치합니다.', true);
    }
}

// 비밀번호 확인 실시간 유효성 검사
document.getElementById('passwordConfirm').addEventListener('input', function() {
    checkPasswordConfirmation();
    checkFormValidity();
});

// 이름 실시간 유효성 검사
document.getElementById('userName').addEventListener('input', function() {
    const userName = this.value.trim();

    if (!userName) {
        validationState.userName = false;
        showValidationMessage('userNameMessage', '', false);
        checkFormValidity();
        return;
    }

    if (userName.length > 100) {
        validationState.userName = false;
        showValidationMessage('userNameMessage', '이름은 100자 이하여야 합니다.', false);
    } else if (userName.length < 2) {
        validationState.userName = false;
        showValidationMessage('userNameMessage', '이름은 2자 이상이어야 합니다.', false);
    } else {
        validationState.userName = true;
        showValidationMessage('userNameMessage', '올바른 이름입니다.', true);
    }

    checkFormValidity();
});

// 이메일 실시간 유효성 검사
document.getElementById('email').addEventListener('input', function() {
    const email = this.value.trim();

    if (!email) {
        validationState.email = false;
        showValidationMessage('emailMessage', '', false);
        checkFormValidity();
        return;
    }

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (email.length > 100) {
        validationState.email = false;
        showValidationMessage('emailMessage', '이메일은 100자 이하여야 합니다.', false);
    } else if (!emailPattern.test(email)) {
        validationState.email = false;
        showValidationMessage('emailMessage', '올바른 이메일 형식을 입력해주세요.', false);
    } else {
        validationState.email = true;
        showValidationMessage('emailMessage', '올바른 이메일 형식입니다.', true);
    }

    checkFormValidity();
});

// 연락처 실시간 유효성 검사 및 자동 포맷팅
document.getElementById('phone').addEventListener('input', function() {
    let value = this.value.replace(/[^0-9]/g, '');

    // 자동 포맷팅
    if (value.length >= 3 && value.length <= 7) {
        value = value.replace(/(\d{3})(\d{1,4})/, '$1-$2');
    } else if (value.length > 7) {
        value = value.replace(/(\d{3})(\d{4})(\d{1,4})/, '$1-$2-$3');
    }
    this.value = value;

    // 유효성 검사
    if (!value) {
        validationState.phone = false;
        showValidationMessage('phoneMessage', '', false);
        checkFormValidity();
        return;
    }

    const phonePattern = /^010-\d{4}-\d{4}$/;

    if (!value.startsWith('010')) {
        validationState.phone = false;
        showValidationMessage('phoneMessage', '010으로 시작하는 번호를 입력해주세요.', false);
    } else if (value.length < 13) {
        validationState.phone = false;
        showValidationMessage('phoneMessage', '연락처를 모두 입력해주세요.', false);
    } else if (!phonePattern.test(value)) {
        validationState.phone = false;
        showValidationMessage('phoneMessage', '올바른 연락처 형식이 아닙니다.', false);
    } else {
        validationState.phone = true;
        showValidationMessage('phoneMessage', '올바른 연락처 형식입니다.', true);
    }

    checkFormValidity();
});

// 주소 실시간 유효성 검사
document.getElementById('address').addEventListener('input', function() {
    const address = this.value.trim();

    if (!address) {
        validationState.address = false;
        showValidationMessage('addressMessage', '', false);
        checkFormValidity();
        return;
    }

    if (address.length < 5) {
        validationState.address = false;
        showValidationMessage('addressMessage', '주소를 상세히 입력해주세요.', false);
    } else if (address.length > 200) {
        validationState.address = false;
        showValidationMessage('addressMessage', '주소는 200자 이하여야 합니다.', false);
    } else {
        validationState.address = true;
        showValidationMessage('addressMessage', '주소가 입력되었습니다.', true);
    }

    checkFormValidity();
});

// 아이디 중복 확인
async function checkDuplicateId() {
    const userId = document.getElementById('userId').value;
    const messageEl = document.getElementById('idCheckMessage');

    if (!userId) {
        showValidationMessage('idCheckMessage', '아이디를 입력해주세요.', false);
        return;
    }

    if (!validationState.userId) {
        showValidationMessage('idCheckMessage', '올바른 아이디 형식을 먼저 입력해주세요.', false);
        return;
    }

    // 중복 확인 버튼 비활성화
    const checkButton = document.querySelector('.id-check-btn');
    if (checkButton) {
        checkButton.disabled = true;
        checkButton.textContent = '확인 중...';
    }

    try {
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
                showValidationMessage('idCheckMessage', '이미 사용 중인 아이디입니다.', false);
                isIdChecked = false;
                validationState.userId = false;
            } else {
                showValidationMessage('idCheckMessage', '사용 가능한 아이디입니다.', true);
                isIdChecked = true;
                validationState.userId = true;
            }
        } else {
            showValidationMessage('idCheckMessage', data.message || '아이디 확인 중 오류가 발생했습니다.', false);
            isIdChecked = false;
            validationState.userId = false;
        }
    } catch (error) {
        console.error('아이디 중복 확인 오류:', error);
        showValidationMessage('idCheckMessage', '아이디 확인 중 오류가 발생했습니다.', false);
        isIdChecked = false;
        validationState.userId = false;
    } finally {
        // 중복 확인 버튼 복원
        if (checkButton) {
            checkButton.disabled = false;
            checkButton.textContent = '중복확인';
        }
        checkFormValidity();
    }
}

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

    // 최종 유효성 검사
    const allValid = Object.values(validationState).every(state => state) && isIdChecked;

    if (!allValid) {
        alert('모든 항목을 올바르게 입력해주세요.');
        return;
    }

    if (!isIdChecked) {
        alert('아이디 중복확인을 해주세요.');
        return;
    }

    if (password !== passwordConfirm) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
    }

    // 제출 버튼 비활성화
    const submitButton = this.querySelector('button[type="submit"]');
    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = '가입 중...';
    }

    try {
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
    } finally {
        // 제출 버튼 복원
        if (submitButton) {
            submitButton.disabled = false;
            submitButton.textContent = '가입하기';
        }
    }
});

// 페이지 로드 시 초기 상태 설정
document.addEventListener('DOMContentLoaded', function() {
    checkFormValidity();

    // 필수 메시지 요소가 없으면 생성
    const messageIds = [
        'idCheckMessage', 'passwordMessage', 'passwordCheckMessage',
        'userNameMessage', 'emailMessage', 'phoneMessage', 'addressMessage'
    ];

    messageIds.forEach(id => {
        if (!document.getElementById(id)) {
            console.warn(`메시지 요소 ${id}가 없습니다. HTML에 추가해주세요.`);
        }
    });
});

// 입력 필드에서 엔터키 방지 (폼 자동 제출 방지)
document.querySelectorAll('input[type="text"], input[type="password"], input[type="email"]').forEach(input => {
    input.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && e.target.type !== 'submit') {
            e.preventDefault();
        }
    });
});