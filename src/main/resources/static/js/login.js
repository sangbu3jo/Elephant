// 비밀번호 내용 표시 및 숨기기
const passwordInput = document.getElementById('password');
const loginPasswordInput = document.getElementById('loginPassword');
const showSignupPwBtn = document.getElementById('showSignupPwBtn');
const showLoginPwBtn = document.getElementById('showLoginPwBtn');

showSignupPwBtn.addEventListener('click', (event) => {
    event.preventDefault();
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
    } else {
        passwordInput.type = 'password';
    }
});

showLoginPwBtn.addEventListener('click', (event) => {
    event.preventDefault();
    if (loginPasswordInput.type === 'password') {
        loginPasswordInput.type = 'text';
    } else {
        loginPasswordInput.type = 'password';
    }
});


const logInBtn = document.getElementById("logIn");
const signUpBtn = document.getElementById("signUp");
const fistForm = document.getElementById("form1");
const secondForm = document.getElementById("form2");
const container = document.querySelector(".container");

logInBtn.addEventListener("click", () => {
    container.classList.remove("right-panel-active");
});

signUpBtn.addEventListener("click", () => {
    container.classList.add("right-panel-active");
});

fistForm.addEventListener("submit", (e) => e.preventDefault());
secondForm.addEventListener("submit", (e) => e.preventDefault());


const Toast = Swal.mixin({
    toast: true,
    position: 'center-center',
    showConfirmButton: false,
    timer: 1000,
    timerProgressBar: true,
    didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer)
        toast.addEventListener('mouseleave', Swal.resumeTimer)
    }
})


// 쿠키값 가져오기
let token = Cookies.get('Authorization');

function signup() {
    let username = $('#username').val();
    let password = $('#password').val();
    let nickname = $('#nickname').val();
    let introduce = $('#introduce').val();
    let adminToken = $('#admin-token').val();
    const RegExp1 = /^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
    const num = password.search(/[0-9]/g);
    const eng = password.search(/[a-z]/ig);
    const spe = password.search(/[`~!@@#$%^&*|₩₩₩'₩";:₩/?]/gi);

    if (username == "") {
        Swal.fire({
            icon: 'warning',
            title: '아이디 입력 오류',
            text: '아이디가 공백입니다. 문자를 입력해주세요.',
        });
        $('#username').focus();
        return false;
    }

    if (!RegExp1.test(username)) {
        Swal.fire({
            icon: 'warning',
            title: '아이디 입력 오류',
            text: '이메일 형식에 맞게 입력해주세요.',
        });
        $('#username').focus();
        return false;
    }

    if (password.length < 8 || password.length > 20) {
        Swal.fire({
            icon: 'warning',
            title: '비밀번호 입력 오류',
            text: '8자리 ~ 20자리 이내로 입력해주세요.',
        });
        $('#password').focus();
        return false;
    }

    if (password.search(/\s/) != -1) {
        Swal.fire({
            icon: 'warning',
            title: '비밀번호 입력 오류',
            text: '비밀번호는 공백 없이 입력해주세요.',
        });
        $('#password').focus();
        return false;
    }

    if (num < 0 || eng < 0 || spe < 0) {
        Swal.fire({
            icon: 'warning',
            title: '비밀번호 입력 오류',
            text: '영문,숫자, 특수문자를 혼합하여 입력해주세요.',
        });
        $('#password').focus();
        return false;
    }

    if (nickname == "") {
        Swal.fire({
            icon: 'warning',
            title: '닉네임 입력 오류',
            text: '닉네임이 공백입니다. 문자를 입력해주세요.',
        });
        $('#nickname').focus();
        return false;
    }

    if (introduce.length <= 10 || introduce.length > 1000) {
        Swal.fire({
            icon: 'warning',
            title: '자기소개 입력오류',
            text: '자기소개를 10글자 이상 1000글자 미만으로 입력해주세요.',
        });
        $('#introduce').focus();
        return false;
    }


    $.ajax({
        type: "POST",
        url: `/api/auth/signup`,
        contentType: "application/json",
        data: JSON.stringify({
            username: username, password: password, nickname: nickname,
            introduction : introduce, adminToken: adminToken
        }),
    })
    .done(function (res, status, xhr) {
        Toast.fire({
            icon: 'success',
            title: '회원가입에 성공하셨습니다.'
        }).then(function () {
            window.location.reload();
        })
    })
    .fail(function (jqXHR, textStatus, error) {
        console.log(jqXHR);
        console.log(textStatus);
        console.log(error);
        Toast.fire({
            icon: 'error',
            title: '회원가입에 실패하였습니다.'
        })
    });
}

function onclickAdmin() {
    // Get the checkbox
    var checkBox = document.getElementById("admin-check");
    // Get the output text
    var box = document.getElementById("admin-token");

    // If the checkbox is checked, display the output text
    if (checkBox.checked == true) {
        box.style.display = "block";
    } else {
        box.style.display = "none";
    }
}

function onLogin() {
    let loginUsername = $('#loginUsername').val();
    let loginPassword = $('#loginPassword').val();

    $.ajax({
        type: "POST",
        url: `/api/auth/login`,
        contentType: "application/json",
        data: JSON.stringify({username: loginUsername, password: loginPassword}),
    })
    .done(function () {
        Toast.fire({
            icon: 'success',
            title: loginUsername + '님 환영합니다!'
        }).then(function () {
            //window.location.href = "/view/main"
        })
    })
    .fail(function (jqXHR, textStatus) {
        Toast.fire({
            icon: 'warning',
            title: '가입한 내역 여부 혹은 \n 로그인 정보를 확인부탁드립니다.'
        }).then(function () {
            window.location.reload();
        })
    });
}
