$(document).ready(function () {
  pageSetting();
});


/* 페이지 첫 로드 메서드 */
function pageSetting() {  // main page 세팅
  const token = Cookies.get('Authorization');
  const rtoken = Cookies.get('RefreshToken');
  console.log(token);
  console.log(rtoken);

  if(!token) {
    // 토큰 없을 경우, 로그인하지 않은 유저에게 보여줘야 할 내용을 처리하실 수 있습니다.
  }

  // 만료되지 않은 경우, 로그인한 유저에게 보여줘야 할 내용을 처리하실 수 있습니다.

}


function removeToken() { // 토큰 삭제
  Cookies.remove('Authorization', {path: '/'});
}



/* 서버 통신 메서드 */
function refreshAccessToken() { // 엑세스 토큰 갱신
  $.ajax({
    type: "GET",
    url: `/api/auth/refresh/access-token`,
    contentType: "application/json",
    success: function (data) {
      console.log(data);
      alert("로그인 연장 성공");
    },
    error: function (data) {
      console.log(data);
      alert("로그인 연장 실패 \n 재로그인 부탁드립니다.");
      redirectToLoginPage();
    }
  });
}

function logout() { // 로그아웃
  $.ajax({
    type: "DELETE",
    url: `/api/auth/logout`,
    contentType: "application/json",
    success: function (data) {
      removeToken();
      alert("로그아웃 성공");
      window.location.reload();
    },
    error: function () {
      alert("로그아웃 실패");
      window.location.reload();
    }
  });
}

function redirectToLoginPage() {
  window.location.href = "/api/auth/login-page";
}
