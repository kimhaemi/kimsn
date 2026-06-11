
//TODO - document load후 init_body 실행
(function($){ //create closure so we can safely use $ as alias for jQuery
	$(document).ready(init_body);
})(jQuery);

function init_body() {
	playMax=$("#slide-images img").length;
	playNow=playMax-1;
	$("#slide-images").click(zoom);
	$("#slide-images").mousemove(mouse_move);
	$("#slide-wholemap").click(function(){unzoom_area(0);});
	$("#slide-unzoom").click(function(){unzoom_area(1);});
}

function zoom(event) {
	if(isPlay) return;
	var offset = $(this).offset();
	var x=event.pageX-offset.left;
	var y=event.pageY-offset.top;

	var nmap = 5;
	var dmap = 0.5 * size / ( nmap - 1 );
	var margin = (size * 0.5 - dmap) * 0.5;
	var zx = 0;
	var zy = 0;
	zx =  Math.ceil((x - margin) / dmap) + 1;
	zy =  Math.ceil((y - margin) / dmap) + 1;
	if(zx > 1) zx = zx - 1;
	if(zx > 5) zx = zx - 1;
	if(zy > 1) zy = zy - 1;
	if(zy > 5) zy = zy - 1;
	if(zx > 5) zx = 0;
	if(zy > 5) zy = 0;
	if(zx > 0 && zy > 0){
		zoom_area(zx,zy);
	} else {
		if(zoom_level > 0) {
			/* 확대 되었을 경우 좌/우/위/아래/좌상단/우상단/죄하단/우하단 으로 이동하는 기능 */

			var smap = Math.round(dmap*0.5);
			var dx = 0, dy = 0;
			
			if(x < smap ) {
				dx = -1;
				if(y < smap ) dy = -1;
				else if(y > (size - smap)) dy = +1;
				
				move_area(zoom_level, dx, dy);							
			} else if(x > (size - smap)) {
				dx = +1;
				if(y < smap ) dy = -1;
				if(y > (size - smap)) dy = +1;
				move_area(zoom_level, dx, dy);
			}

			if(y < smap ) {
				dy = -1;
				if(x < smap ) dx = -1;
				else if(x > (size - smap)) dx = +1;
				
				move_area(zoom_level, dx, dy);							
			} else if(y > (size - smap)) {
				dy = +1;
				if(x < smap ) dx = -1;
				if(x > (size - smap)) dx = +1;
				move_area(zoom_level, dx, dy);
			}
		}
		return;
	}
}
function mouse_move(event) {
	if(isPlay) return;
	var offset = $(this).offset();
	var x=event.pageX-offset.left;
	var y=event.pageY-offset.top;
	var src=$("#slide-images").children().children();

	var nmap = 5;
	var dmap = 0.5 * size / ( nmap - 1 );
	var margin = (size * 0.5 - dmap) * 0.5;
	var zx = 0;
	var zy = 0;
	zx =  Math.ceil((x - margin) / dmap) + 1;
	zy =  Math.ceil((y - margin) / dmap) + 1;
	if(zx > 1) zx = zx - 1;
	if(zx > 5) zx = zx - 1;
	if(zy > 1) zy = zy - 1;
	if(zy > 5) zy = zy - 1;
	if(zx > 5) zx = 0;
	if(zy > 5) zy = 0;
	if(zx > 0 && zy > 0){
		src.css("cursor","pointer");
		src.attr("title","[" + zx + "," + zy + "] 영역으로 이동");
//		src.style.cursor = "pointer";
//		src.title = "[" + zx + "," + zy + "] 영역으로 이동";
	} else {
		if(zoom_level > 0) {
			/* 확대 되었을 경우 좌/우/위/아래/좌상단/우상단/죄하단/우하단 으로 이동하는 기능 */

			var smap = Math.round(dmap*0.5);
			var dx = 0, dy = 0;
			
			if(x < smap ) {
				dx = -1;
				if(y < smap ) src.attr("title","좌상단으로 이동"); //src.title = "좌상단으로 이동";
				else if(y > (size - smap)) src.attr("title","좌하단으로 이동"); //src.title = "좌하단으로 이동";
				else src.attr("title","왼쪽으로 이동"); //src.title = "왼쪽으로 이동";
			} else if(x > (size - smap)) {
				dx = +1;
				if(y < smap ) src.attr("title","우상단으로 이동"); //src.title = "우상단으로 이동";
				else if(y > (size - smap)) src.attr("title","우하단으로 이동"); //src.title = "우하단으로 이동";
				else  src.attr("title","오른쪽으로 이동"); //src.title = "오른쪽으로 이동";
			}

			if(y < smap ) {
				dy = -1;
				if(x < smap ) src.attr("title","좌상단으로 이동"); //src.title = "좌상단으로 이동";
				else if(x > (size - smap))  src.attr("title","우상단으로 이동"); //src.title = "우상단으로 이동";
				else  src.attr("title","상단으로 이동"); //src.title = "상단으로 이동";
			
			} else if(y > (size - smap)) {
				dy = +1;
				if(x < smap ) src.attr("title","좌하단으로 이동"); //src.title = "좌하단으로 이동";
				else if(x > (size - smap)) src.attr("title","우하단으로 이동"); //src.title = "우하단으로 이동";
				else  src.attr("title","하단으로 이동"); //src.title = "하단으로 이동"; 
			}
		}
	}
}
function zoom_area(x,y)
{
	v = parseInt($("#zoom_level").val());
	if (v == 7)	{
		alert("더이상의 확대는 안됩니다.");
		return;
	}
	if (v == 0)
		v1 = x + $("#zoom_x").val().substr(v+1,6);
	else
		v1 = $("#zoom_x").val().substr(0,v) + x + $("#zoom_x").val().substr(v+1,6);
	$("#zoom_x").val(v1);

	if (v == 0)
		v1 = y + $("#zoom_y").val().substr(v+1,6);
	else
		v1 = $("#zoom_y").val().substr(0,v) + y + $("#zoom_y").val().substr(v+1,6);
	$("#zoom_y").val(v1);

	$("#zoom_level").val(v + 1);
	$("#lgtForm").submit();
}

function unzoom_area(w) {
	if (w == 0) {
		$("#zoom_level").val(0);
		$("#zoom_x").val('0000000');
		$("#zoom_y").val('0000000');
	} else if (w == 1) {
		v = parseInt($("#zoom_level").val()) - 1;
		if (v == 0) {
			$("#zoom_x").val('0000000');
			$("#zoom_y").val('0000000');
			$("#zoom_level").val(0);
		} else {
			v1 = $("#zoom_x").val().substr(0, v) + '0'
					+ $("#zoom_x").val().substr(v + 1, 6);
			$("#zoom_x").val(v1);
			v1 = $("#zoom_y").val().substr(0, v) + '0';
					+ $("#zoom_y").val().substr(v + 1, 6);
			$("#zoom_y").val(v1);
			$("#zoom_level").val(v);
		}
	}
	$("#lgtForm").submit();
}
function move_area(zoom_level, dx, dy) {
	var v, x, y, v1;
	if (zoom_level <= 0)
		return;

	v = parseInt(zoom_level) - 1;
	x = parseInt($("#zoom_x").val().charAt(v));
	y = parseInt($("#zoom_y").val().charAt(v));
	x += dx;
	y += dy;

	if (v >= 1) {
		if (x < 1) {
			move_area(v, -1, 0);
			x = 2;
		}
		if (x > 5) {
			move_area(v, 1, 0);
			x = 4;
		}
		if (y < 1) {
			move_area(v, 0, -1);
			y = 2;
		}
		if (y > 5) {
			move_area(v, 0, 1);
			y = 4;
		}
	} else {
		if (x < 1)
			x = 1;
		if (x > 5)
			x = 5;
		if (y < 1)
			y = 1;
		if (y > 5)
			y = 5;
	}

	if (v == 0)
		v1 = x + $("#zoom_x").val().substr(v + 1, 6);
	else
		v1 = $("#zoom_x").val().substr(0, v) + x
				+ $("#zoom_x").val().substr(v + 1, 6);
	$("#zoom_x").val(v1);

	if (v == 0)
		v1 = y + $("#zoom_y").val().substr(v + 1, 6);
	else
		v1 = $("#zoom_y").val().substr(0, v) + y
				+ $("#zoom_y").val().substr(v + 1, 6);
	$("#zoom_y").val(v1);

	if (parseInt(zoom_level) == parseInt($("#zoom_level").val()))
		$("#lgtForm").submit();
}
function changeAccTm(tm){
	$("#accTm").val(tm);
	$("#lgtForm").submit();
}
function changeTimeTerm(tm){
	$("#timeTerm").val(tm);
	$("#lgtForm").submit();
}




function viewLgtByKey(key){
	if(key==37){ // <-
		playNow--;
		if(playNow<0){
			playNow = playMax-1;
		}
		viewLgt(playNow);
	}
	else if(key==39){ // ->
		playNow++;
		if(playNow>=playMax){
			playNow = 0;
		}
		viewLgt(playNow);
	}
}

function viewLgt(idx){
	$(".time_bar .time_on").attr("class","time");
	//$("#content_info li.last").text(list[idx][1]);
	$(".time_bar .time:eq("+idx+")").attr("class","time_on");
	$("#slide-images li:visible").hide();
	$("#slide-images li:eq("+idx+")").show();
	playNow=idx;
}
function orbitLgt(){
	//viewImg(playNow);
	playNow++;
	if(playNow>=playMax){
		playNow=0;
	}
	viewLgt(playNow);
} 
function playLgt(){
	$(".time_control .ctrl3").attr("class","ctrl3");//#snb 
	$(".time_control .ctrl4").attr("class","ctrl4 on");
	orbitLgt();
	playObj=window.setInterval(orbitLgt,playTerm);
	isPlay=true;
}
function stopLgt(){
	$(".time_control .ctrl3").attr("class","ctrl3 on");//#snb 
	$(".time_control .ctrl4").attr("class","ctrl4");
	clearInterval(playObj);
	isPlay=false;
}
function setPlayTermLgt(s){
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
		stopLgt();
		playLgt();
	}
}