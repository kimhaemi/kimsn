/* Korean translation for the jQuery Timepicker Addon */
/* Written by Genie */
/* Modified by Junyeop Rhee */
(function($) {
	$.timepicker.regional['ko'] = {
		/* Add option by Junyeop Rhee */
//		showOn: "button",
//		buttonImage: "../images/calendar.gif",
//		buttonImageOnly: false,
//		buttonText: "시간 선택",
//		changeMonth: true,
//		changeYear: true,
		minDate: "-1M"/*(new Date(2009, 1 - 1, 1))*/, 
		maxDate: "+1M"/*(new Date(2014, 11, 31)) /*"+1M +10D"*/ ,
		dateFormat: 'yy-mm-dd',
		monthNames: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
		monthNamesShort: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
		dayNames: ['일','월','화','수','목','금','토'],
		dayNamesShort: ['일','월','화','수','목','금','토'],
		dayNamesMin: ['일','월','화','수','목','금','토'],
		showMonthAfterYear: true,
		yearSuffix: '년',

		/* Default Options */
		timeOnlyTitle: '시간 선택',
		timeText: '시간',
		hourText: '시',
		minuteText: '분',
		secondText: '초',
		millisecText: '밀리초',
		microsecText: '마이크로초',
		timezoneText: '표준 시간대',
		currentText: '현재 시각',
		closeText: '닫기',
//		timeFormat: 'tt h:mm',
		timeFormat: 'HH:mm',	// change 
		// amNames: ['오전', 'AM', 'A'],
		// pmNames: ['오후', 'PM', 'P'],
		isRTL: false
	};
	$.timepicker.setDefaults($.timepicker.regional['ko']);
})(jQuery);
