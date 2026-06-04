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