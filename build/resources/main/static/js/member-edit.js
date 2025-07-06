// 회원 정보 수정 페이지 JavaScript

let isEmailChecked = false;
let originalEmail = '';

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    loadMemberInfo();
    setupFormHandlers();
});

// 회원 정보 로드
async function loadMemberInfo() {
    try {
        const response = await fetch('/api/members/login-status');
        const result = await response.json();

        if (!result.success || !result.data.isLoggedIn) {
            alert('로그인이 필요합니다.');
            location.href = '/login';
            return;
        }

        const member = result.data.member;
        populateForm(member);
        updateUserGreeting(member.memberName);

    } catch (error) {
        console.error('회원 정보 로드 실패:', error);
        alert('회원 정보를 불러오는 중 오류가 발생했습니다.');
    }
}

// 폼에 데이터 채우기
function populateForm(member) {
    document.getElementById('memberId').value = member.memberId;
    document.getElementById('memberName').value = member.memberName;
    document.getElementById('email').value = member.email;
    document.getElementById('phone').value = member.phone;
    document.getElementById('address').value = member.address;
    document.getElementById('memberGrade').value = member.memberGrade || '일반';

    // 날짜 포맷팅
    if (member.joinDate) {
        document.getElementById('joinDate').value = formatDate(member.joinDate);
    }
    if (member.lastLoginDate) {
        document.getElementById('lastLoginDate').value = formatDate(member.lastLoginDate);
    }

    // 원본 이메일 저장
    originalEmail = member.email;
    isEmailChecked = true; // 기존 이메일은 중복확인 완료로 간주
}

// 사용자 인사말 업데이트
function updateUserGreeting(name) {
    const greeting = document.getElementById('userGreeting');
    if (greeting) {
        greeting.textContent = `안녕하세요, ${name}님`;
    }
}

// 날짜 포맷팅
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// 폼 핸들러 설정
function setupFormHandlers() {
    const form = document.getElementById('memberEditForm');
    const emailInput = document.getElementById('email');

    // 폼 제출 처리
    form.addEventListener('submit', handleFormSubmit);

    // 이메일 변경 감지
    emailInput.addEventListener('input', function() {
        const currentEmail = this.value.trim();
        if (currentEmail !== originalEmail) {
            isEmailChecked = false;
            clearEmailMessage();
        } else {
            isEmailChecked = true;
        }
    });

    // 전화번호 자동 포맷팅
    const phoneInput = document.getElementById('phone');
    phoneInput.addEventListener('input', formatPhoneNumber);
}

// 전화번호 포맷팅
function formatPhoneNumber() {
    const phoneInput = document.getElementById('phone');
    let value = phoneInput.value.replace(/\D/g, '');

    if (value.length >= 3 && value.length <= 7) {
        value = value.replace(/(\d{3})(\d+)/, '$1-$2');
    } else if (value.length > 7) {
        value = value.replace(/(\d{3})(\d{4})(\d+)/, '$1-$2-$3');
    }

    phoneInput.value = value;
}

// 이메일 중복 확인
async function checkEmailDuplicate() {
    const emailInput = document.getElementById('email');
    const email = emailInput.value.trim();
    const messageDiv = document.getElementById('emailCheckMessage');

    if (!email) {
        showEmailMessage('이메일을 입력해주세요.', 'error');
        return;
    }

    if (!isValidEmail(email)) {
        showEmailMessage('올바른 이메일 형식이 아닙니다.', 'error');
        return;
    }

    // 원본 이메일과 같으면 중복확인 불필요
    if (email === originalEmail) {
        showEmailMessage('현재 사용 중인 이메일입니다.', 'success');
        isEmailChecked = true;
        return;
    }

    try {
        const response = await fetch('/api/members/check-email', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email: email })
        });

        const result = await response.json();

        if (result.success) {
            if (result.data.isDuplicate) {
                showEmailMessage('이미 사용 중인 이메일입니다.', 'error');
                isEmailChecked = false;
            } else {
                showEmailMessage('사용 가능한 이메일입니다.', 'success');
                isEmailChecked = true;
            }
        } else {
            showEmailMessage(result.message || '중복 확인 중 오류가 발생했습니다.', 'error');
            isEmailChecked = false;
        }
    } catch (error) {
        console.error('이메일 중복 확인 실패:', error);
        showEmailMessage('중복 확인 중 오류가 발생했습니다.', 'error');
        isEmailChecked = false;
    }
}

// 이메일 메시지 표시
function showEmailMessage(message, type) {
    const messageDiv = document.getElementById('emailCheckMessage');
    messageDiv.textContent = message;
    messageDiv.className = `form-message ${type}`;
}

// 이메일 메시지 지우기
function clearEmailMessage() {
    const messageDiv = document.getElementById('emailCheckMessage');
    messageDiv.textContent = '';
    messageDiv.className = 'form-message';
}

// 이메일 유효성 검사
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// 폼 제출 처리
async function handleFormSubmit(event) {
    event.preventDefault();

    if (!validateForm()) {
        return;
    }

    const formData = {
        memberName: document.getElementById('memberName').value.trim(),
        email: document.getElementById('email').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        address: document.getElementById('address').value.trim()
    };

    const submitBtn = event.target.querySelector('button[type="submit"]');

    try {
        // 로딩 상태 표시
        submitBtn.disabled = true;
        submitBtn.classList.add('loading');
        submitBtn.textContent = '수정 중...';

        const response = await fetch('/api/members/me', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        });

        const result = await response.json();

        if (result.success) {
            alert('회원 정보가 성공적으로 수정되었습니다.');
            location.href = '/mypage';
        } else {
            alert(result.message || '회원 정보 수정 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('회원 정보 수정 실패:', error);
        alert('회원 정보 수정 중 오류가 발생했습니다.');
    } finally {
        // 로딩 상태 해제
        submitBtn.disabled = false;
        submitBtn.classList.remove('loading');
        submitBtn.textContent = '정보 수정';
    }
}

// 폼 유효성 검사
function validateForm() {
    const memberName = document.getElementById('memberName').value.trim();
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const address = document.getElementById('address').value.trim();

    if (!memberName) {
        alert('이름을 입력해주세요.');
        document.getElementById('memberName').focus();
        return false;
    }

    if (!email) {
        alert('이메일을 입력해주세요.');
        document.getElementById('email').focus();
        return false;
    }

    if (!isValidEmail(email)) {
        alert('올바른 이메일 형식이 아닙니다.');
        document.getElementById('email').focus();
        return false;
    }

    if (email !== originalEmail && !isEmailChecked) {
        alert('이메일 중복 확인을 해주세요.');
        document.getElementById('email').focus();
        return false;
    }

    if (!phone) {
        alert('연락처를 입력해주세요.');
        document.getElementById('phone').focus();
        return false;
    }

    if (!isValidPhone(phone)) {
        alert('올바른 연락처 형식이 아닙니다. (010-0000-0000)');
        document.getElementById('phone').focus();
        return false;
    }

    if (!address) {
        alert('주소를 입력해주세요.');
        document.getElementById('address').focus();
        return false;
    }

    return true;
}

// 전화번호 유효성 검사
function isValidPhone(phone) {
    const phoneRegex = /^010-\d{4}-\d{4}$/;
    return phoneRegex.test(phone);
}

// 회원 탈퇴 확인
function confirmWithdrawal() {
    if (confirm('정말로 회원 탈퇴를 하시겠습니까?\n탈퇴 후에는 모든 정보가 삭제되며 복구할 수 없습니다.')) {
        if (confirm('마지막 확인입니다. 정말로 탈퇴하시겠습니까?')) {
            withdrawMember();
        }
    }
}

// 회원 탈퇴 처리
async function withdrawMember() {
    try {
        const response = await fetch('/api/members/withdrawal', {
            method: 'DELETE'
        });

        const result = await response.json();

        if (result.success) {
            alert('회원 탈퇴가 완료되었습니다.');
            location.href = '/';
        } else {
            alert(result.message || '회원 탈퇴 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('회원 탈퇴 실패:', error);
        alert('회원 탈퇴 중 오류가 발생했습니다.');
    }
}

// 로그아웃
async function logout() {
    try {
        const response = await fetch('/api/members/logout', {
            method: 'POST'
        });

        const result = await response.json();

        if (result.success) {
            location.href = '/';
        } else {
            alert('로그아웃 중 오류가 발생했습니다.');
        }
    } catch (error) {
        console.error('로그아웃 실패:', error);
        alert('로그아웃 중 오류가 발생했습니다.');
    }
}