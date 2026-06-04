(function($){ //create closure so we can safely use $ as alias for jQuery
	$(document).ready(function(){
		
	});
})(jQuery);

function alarmStart(){
	if($(".alarmAudio").prop('paused')){
		$(".alarmAudio").prop('volume',0.1);
		$(".alarmAudio").trigger('play');
	}
}
function alarmStop(){
	$(".alarmAudio").trigger('pause');
	$(".alarmAudio").prop('currentTime',0);
}
function notyCheckAndAlarmStop(){
	if($("ul[id^='noty']").length==0){
		alarmStop();
	}
}

function str2date(s){
	if(s.length==12){
		var y=s.substring(0,4);
		var m=s.substring(4,6);
		var d=s.substring(6,8);
		var h=s.substring(8,10);
		var i=s.substring(10,12);
		try{
			y=Number(y);
			m=Number(m)-1;
			d=Number(d);
			h=Number(h);
			i=Number(i);
		}
		catch(e){
			return Date();
		}
		var d=new Date(y,m,d,h,i,0,0); 
		return d;
	}
	return Date();
}

function nowTime(){
	var now=new Date();
	var y=now.getFullYear();
	var m=now.getMonth()+1;
	var d=now.getDate();
	var h=now.getHours();
	var i=now.getMinutes();
	var s=now.getSeconds();
	if(m<10) m="0"+m;
	if(d<10) d="0"+d;
	if(h<10) h="0"+h;
	if(i<10) i="0"+i;
	if(s<10) s="0"+s;
	return ""+y+"."+m+"."+d+" "+h+":"+i+":"+s;
}

function f5Main(){
	var txt=loadTEXTDoc("main.do?get=CONTENT");
	if(txt!=null){
		$("#f5content").html(txt);
	}
}
function f5Station(){
	var txt=loadTEXTDoc($("#alert_checksite").val()+".do?get=CONTENT");
	if(txt!=null){
		$("#f5content").html(txt);
	}
}
function alertCheck(){
	if($("#alert_activity").val()==1){
		var now=nowTime().replace(/:|\.| /g,"");
		$.post("/checkAlert.do", {time:$("#alert_lasttime").val(),site:$("#alert_checksite").val()}, function(data,status){
			if(data.length>0){
				$("#alert_lasttime").val(data[0].lastTime);
				for(i in data){
					var type="";
					switch(data[i].recvCondition){
						case 'ORDI':
							type="information";
							break;
						case 'NOTI':
							type="warning";
							break;
						case 'WARN':
						case 'TOTA':
							type="error";
							break;
					}
					notyGen(data[i].msg,type);
				}
			}
			else{
				$("#alert_lasttime").val(now);
			}
		});
	}
}
function notyGen(msg,type){
	var n=noty({
		text : msg,
		type : type,
		timeout : 600000,
		closeWith : ['click'],
		layout : 'top',
		theme : 'defaultTheme',
		maxVisible : 10,
		callback : {
			afterShow: function(){
				alarmStart();
			},
			afterClose: function(){
				window.setTimeout(notyCheckAndAlarmStop,1000);
			}
		}
	});
}
function alertCheck2(){
	if($("#alert_activity").val()==1){
		var now=nowTime().replace(/:|\.| /g,"");
		$.post("/checkAlert2.do", {time:$("#alert_lasttime").val(),site:$("#alert_checksite").val()}, function(data,status){
			if(data.length>0){
				$("#alert_lasttime").val(data[0].lastTime);
				for(i in data){
					var type="";
					switch(data[i].recvCondition){
						case 'ORDI':
							type="information";
							data[i].msg=notification_html[0]+data[i].msg+"</div></div>";
							break;
						case 'NOTI':
							type="warning";
							data[i].msg=notification_html[1]+data[i].msg+"</div></div>";
							break;
						case 'WARN':
							type="error";
							data[i].msg=notification_html[2]+data[i].msg+"</div></div>";
							break;
						case 'TOTA':
							type="error";
							data[i].msg=notification_html[3]+data[i].msg+"</div></div>";
							break;
					}
					notyGen2(data[i].msg,type);
				}
			}
			else{
				$("#alert_lasttime").val(now);
			}
		});
	}
}
function notyGen2(msg,type){
	var n=noty({
		text : msg,
		type : type,
		timeout : 600000,
		closeWith : ['click'],
		layout : 'topLeft',
		theme : 'relax',
		maxVisible : 7,
		animation : {
            open  : 'animated bounceInLeft',
            close : 'animated bounceOutLeft',
            easing: 'swing',
            speed : 500
		},
		callback : {
			afterShow: function(){
				alarmStart();
			},
			afterClose: function(){
				window.setTimeout(notyCheckAndAlarmStop,1000);
			}
		}
	});
}

function reloadControlPage(stncd){
	var text=loadTEXTDoc("control_"+stncd+".do?vType=content");
	if(text!=null){
		$("#content_div").html(text);
	}
	viewMetaf();
}

function playImg(){
	$(".time_control .ctrl3").attr("class","ctrl3");//#snb 
	$(".time_control .ctrl4").attr("class","ctrl4 on");
	orbit();
	playObj=window.setInterval(orbit,playTerm);
	isPlay=true;
}
function stopImg(){
	$(".time_control .ctrl3").attr("class","ctrl3 on");//#snb 
	$(".time_control .ctrl4").attr("class","ctrl4");
	clearInterval(playObj);
	isPlay=false;
}
function setPlayTerm(s){
	switch(s){
		case 1:
			playTerm=1500;
			$(".time_control .ctrl1").attr("class","ctrl1 on"); //#snb 
			$(".time_control .ctrl2").attr("class","ctrl2");
		break;
		case 2:
			playTerm=3000;
			$(".time_control .ctrl1").attr("class","ctrl1");
			$(".time_control .ctrl2").attr("class","ctrl2 on");
		break;
	}
	if(isPlay){
		stopImg();
		playImg();
	}
}
function loadTEXTDoc(b){try{var a=null;if(window.XMLHttpRequest){a=new XMLHttpRequest()}else{if(window.ActiveXObject){a=new ActiveXObject("MSXML2.XMLHTTP");if(!a){a=new ActiveXObject("Microsoft.XMLHTTP")}}}a.open("GET",b,false);a.send();if(a.readyState==4&&a.status==200){return a.responseText}else{return null}}finally{a=null}}

(function($){
	var cache=[];
	$.preLoadImages=function(){
		var args_len=arguments[0].length;
		for (var i = args_len; i--;) { // i++ 보다 i-- 가 더 빠른 거 아시죠? 수십개 정도 까지야 비슷하지만, 습관이 중요
			if(arguments[0][i][0]!=""){
				var cacheImage = document.createElement('img'); // 이건 pure javascript DOM 구조
				cacheImage.src = arguments[0][i][0];
				cache.push(cacheImage);
			}
		}
	};
})(jQuery);