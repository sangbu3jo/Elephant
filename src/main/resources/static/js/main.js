$(document).ready(function () {
  pageSetting();
});


/* 페이지 첫 로드 메서드 */
function pageSetting() {  // main page 세팅
  const token = Cookies.get('Authorization');

  if(!token) {
    redirectToLoginPage();
  }

  // 만료되지 않은 경우, 로그인한 유저에게 보여줘야 할 내용을 처리하실 수 있습니다.

}


function removeTokenAll() { // 토큰 모두 삭제
  Cookies.remove('Authorization', {path: '/'});
  Cookies.remove('RefreshToken', {path: '/'});
}



/* 서버 통신 메서드 */
function refreshAccessToken() { // 엑세스 토큰 갱신
  $.ajax({
    type: "GET",
    url: `/api/auth/refresh/access-token`,
    contentType: "application/json",
    success: function (data) {
      //console.log('Success:', data);
      alert("로그인 연장 성공");
    },
    error: function (jqXHR, textStatus) {
      console.log('Error:', textStatus);
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
      console.log('Success:', data);
      removeTokenAll();
      alert("로그아웃 성공'");
    },
    error: function (jqXHR, textStatus) {
      console.log('Error:', textStatus);
      alert("로그아웃 실패");
      window.location.reload();
    }
  });
}

function redirectToLoginPage() {
  window.location.href = "/api/auth/login-page";
}
