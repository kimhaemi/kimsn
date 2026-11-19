/*
 * [Claude 2026-11-19] CSRF 토큰 자동 전달 (Spring Security CSRF 보호 활성화 대응) — 이 주석 블록과 바로 아래 함수 전체가 추가분
 *  - layout/config.html 의 <meta name="_csrf*"> 값을 사용
 *  - jQuery Ajax($.ajax, $.post, fnCall): 모든 요청 헤더에 토큰 추가
 *  - method="post" 폼: 화면 로드 시 hidden 입력값으로 토큰 추가 (form.submit() 호출 포함)
 */
(function ($) {
    if (!$) return;
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    var param = $("meta[name='_csrf_parameter']").attr("content");
    if (!token || !header) return;

    $(document).ajaxSend(function (e, xhr) {
        xhr.setRequestHeader(header, token);
    });

    $(function () {
        $("form").each(function () {
            var method = ($(this).attr("method") || "get").toLowerCase();
            if (method === "post" && param && $(this).find("input[name='" + param + "']").length === 0) {
                $("<input>", { type: "hidden", name: param, value: token }).appendTo(this);
            }
        });
    });
})(window.jQuery);

function fnCall(method, url, params){
    let result;
    $.ajax({
        type : method,           // 타입 (get, post, put 등등)
        url : url,           // 요청할 서버url
        async : false,            // 비동기화 여부 (default : true)
        headers : {              // Http header
            "Content-Type" : "application/json",
            // "Content-Type" : "application/x-www-form-urlencoded; charset=UTF-8"
        // // "X-HTTP-Method-Override" : "POST"
        },
        dataType : 'json',       // 데이터 타입 (html, xml, json, text 등등)
        data : params ,
        // data : JSON.stringify({  // 보낼 데이터 (Object , String, Array)
        //     params
        // }),
        success : function(res) { // 결과 성공 콜백함수
            console.log("success: " + res.status);
            result = res;
            // return res;
        },
        error : function(request, status, error) { // 결과 에러 콜백함수
            console.log("request : " + request);
            console.log("status : " + status);
            console.log("error : " + error);
            result = error;
            // return error;
        }
    });

    return result;
}

function getByteB(str) {

    var byte = 0;
    
    for (var i=0; i<str.length; ++i) {
    
    // 기본 한글 2바이트 처리
    
    (str.charCodeAt(i) > 127) ? byte += 2 : byte++ ;
    
    }
    
    return byte;
    
}