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

function signup() {
  let checkBox = $('#email-check').val();
  let username = $('#username').val();
  let password = $('#password').val();
  let nickname = $('#nickname').val();
  let introduce = $('#introduce').val();
  let adminToken = $('#admin-token').val();
  const RegExp1 = /^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
  const num = password.search(/[0-9]/g);
  const eng = password.search(/[a-z]/ig);
  const spe = password.search(/[`~!@@#$%^&*|₩₩₩'₩";:₩/?]/gi);

  if (username === "") {
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

  if (checkBox === "false") {
    Swal.fire({
      icon: 'warning',
      title: '이메일 인증 요망',
      text: '이메일 인증을 완료해주세요',
    });
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

  if (nickname === "") {
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
      introduction: introduce, adminToken: adminToken
    }),
  })
  .done(function (data) {
    Toast.fire({
      icon: 'success',
      title: data,
    }).then(function () {
      window.location.reload();
    })
  })
  .fail(function (jqXHR, textStatus, error) {
    Toast.fire({
      icon: 'error',
      title: '회원가입에 실패하였습니다.'
    })
  });
}

var modal = document.getElementById('emailModal');
var timerValue = document.getElementById('timerValue');
var timerIntervalId;


// 인증메일 전송 버튼 클릭 시
function sendEmail() {
  // 서버로 인증번호 전송 요청하기.
  let userEmail = $('#username').val();
  var modal = $('.modal');
  modal.css('display', 'block');

  $.ajax({
    type: "GET",
    url: `/api/auth/email/${userEmail}/invited`,
    success: function (data) {
      startTimer(5 * 60); // 5분 타이머 시작 (5분 = 5 * 60초)
    },
    error: function (error, response, xhr) {
      console.error("이메일 전송 실패");
      modal.css('display', 'none');
      if (xhr.responseText == "메일 보내는 도중 오류 발생") {
        Toast.fire({
          icon: 'error',
          title: '이메일 전송 오류'
        })
      } else {
        Toast.fire({
          icon: 'error',
          title: '이미 가입된 이메일입니다'
        })
      }
    }
  });
}

function startTimer(duration) {
  var timer = duration;
  var minutes, seconds;

  timerIntervalId = setInterval(function () {
    minutes = parseInt(timer / 60, 10);
    seconds = parseInt(timer % 60, 10);

    minutes = minutes < 10 ? "0" + minutes : minutes;
    seconds = seconds < 10 ? "0" + seconds : seconds;

    timerValue.textContent = minutes + ":" + seconds;

    if (--timer < 0) {
      clearInterval(timerIntervalId);
      timerValue.textContent = "시간 초과!";
      // 타이머 종료 후 원하는 작업을 수행하세요.
    }
  }, 1000);
}


function checkEmail() {
  clearInterval(timerIntervalId); // 타이머 종료

  // 인증 여부 저장할 hidden
  var checkBox = document.getElementById("email-check");
  // 사용자가 입력한 인증 번호
  var inputPassword  = $("#email-password");
  var inputPasswordValue  = $("#email-password").val();

  // ajax 통신을 통해 확인함.
  checkEmailPassword(inputPasswordValue).then(function (result) {
    if (result) {
      // successful
      // 이메일 인증 성공했을 경우에 checked = true
      checkBox.value = "true";
      inputPassword.val("");
    } else {
      // fail
      checkBox.value = "false";
      inputPassword.val("");
    }
  });
}

function checkEmailPassword(inputPassword) {
  return new Promise(function (resolve, reject) {
    let userEmail = $('#username').val();

    $.ajax({
      type: "POST",
      url: `/api/auth/email/${userEmail}/checked`,
      contentType: "application/json",
      data: JSON.stringify({password: inputPassword}),
    })
    .done(function (data) {
      Toast.fire({
        icon: 'success',
        title: data,
      }).then(function () {
        resolve(true);
      })
    })
    .fail(function (jqXHR, textStatus) {
      Toast.fire({
        icon: 'warning',
        title: '인증 정보가 일치하지않습니다.'
      }).then(function () {
        resolve(false);
      })
    });
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
      window.location.href = "/"
    })
  })
  .fail(function (jqXHR, textStatus) {
    Toast.fire({
      icon: 'warning',
      title: '로그인 정보 재확인 부탁드립니다.'
    }).then(function () {
      window.location.reload();
    })
  });
}

