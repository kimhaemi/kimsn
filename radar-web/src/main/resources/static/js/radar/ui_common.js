(function init($) {
    // navigator
    var menu1Deps = $('.navi > div > li'),
        $locate = $('.locate_list>li'),
        headerUtil = $('.header .util'),
        fullnav = $('.full_lnb');

    menu1Deps.on({
        mouseenter: function () {
            var sub = $(this).children('.sub');
            sub.stop();
            sub.slideDown();
        },
        mouseleave: function () {
            var sub = $(this).children('.sub');
            sub.stop();
            sub.slideUp();
        }
    })

    $locate.on('click', function (e) {
        e.stopPropagation();
        $(this).toggleClass('on');
        $(this).children('.sub_loca').slideToggle();
    });
    $(document).on('click', function (e) {
        e.stopPropagation();
        resetDoc();
    });

    function resetDoc() {
        $locate.removeClass('on').children('.sub_loca').slideUp();

    }


    headerUtil.on('click', function () {
        $(this).toggleClass('fullNav_on');
        $(this).hasClass('fullNav_on') ? fullnav.slideDown() : fullnav.slideUp();
    })

    /* layer_popup */
    // use jquery-ui
    var modal = $("[dataformat='modal']");
    modal.draggable({handle: ".pop_header"});
    modal.find("[role='btn_close']").on('click', function (e) {
        e.preventDefault();
        $(this).parents('.overlay').hide();
        selectCGI();
    });

    /* fileDeco */
    $('[role="fileAdd"]').change(function () {
        var fileAdd = $(this);
        fileAdd.parent('span').prev('[role="filePath"]').val(fileAdd.val());
    });

    /* calendar */
    // use jquery-ui
    // $.datepicker.setDefaults({
    //     regional: ["ko"],
    //     buttonImageOnly: true,
    //     showOn: "both",
    //     buttonImage: "/apt/img/btn_calendar.png",
    //     buttonText: "달력검색",
    //     changeMonth: true,
    //     changeYear: true,
    //     dateFormat: "yy-mm-dd"
    // });

    // $("[dataformat='datepic']").datepicker({
    //     buttonText: "날짜를 선택하세요."
    // });
    // $("[dataformat='from']").datepicker({
    //     buttonText: "시작날짜를 선택하세요.",
    //     onClose: function (selectedDate) {
    //         var getName = $(this).attr('name');
    //         $("input[name='" + getName + "'].to").datepicker("option", "minDate", selectedDate);
    //     }
    // });
    // $("[dataformat='to']").datepicker({
    //     buttonText: "종료날짜를 선택하세요.",
    //     onClose: function (selectedDate) {
    //         var getName = $(this).attr('name');
    //         $("input[name='" + getName + "'].from").datepicker("option", "maxDate", selectedDate);
    //     }
    // });

    function date_to_str(format) {
        var year = format.getFullYear(),
            month = format.getMonth() + 1,
            date = format.getDate(),
            hour = format.getHours(),
            min = format.getMinutes(),
            ampm = (hour >= 12) ? '오후' : '오전';
        if (month < 10) month = '0' + month;
        if (date < 10) date = '0' + date;
        hour = hour % 12;
        hour = hour ? hour : 12;
        if (hour < 10) hour = '0' + hour;
        min = min < 10 ? '0' + min : min;
        return year + "-" + month + "-" + date + " " + ampm + " " + hour + ":" + min;
    }

    // $('[dataformat="datetimepic"]').val( date_to_str(new Date()));
    // $('[dataformat="datetimepic"]').val( "2011-11-11 오후 11:11");


    // use jqueryui-timepicker-addon
    // https://trentrichardson.com/examples/timepicker/
    // $("[dataformat='datetimepic']").datetimepicker({
    //     timeFormat: 'tt hh:mm',
    //     controlType: 'select',
    //     oneLine: true
    // });

    $(".time_remort .btn").on('click', function () {
        var getVal = $(this)[0].value,
            iptDateTime = $("[dataformat='datetimepic']"),
            now = new Date(),
            timepic = iptDateTime.datetimepicker('getDate'),
            calctime = timepic.addMinutes(parseInt(getVal));

        var id = $("#cgiId").val();
        if (id == "PR") {
            $("#tab04 ul li").each(function () {
                console.log($(this).find("label").hasClass("on"));
                if ($(this).find("label").hasClass("on")) {
                    $(this).find("label").removeClass("on");
                }
            });
        }

        if (getVal === "now") {
            iptDateTime.datetimepicker('setDate', now);
            nowTime();
            return;
        }
        if (now >= calctime) {
            iptDateTime.datetimepicker('setDate', calctime);
            getTime($(this).val());
        } else {
            alert('선택한 시간이 현재 시간보다 앞섭니다.\n현재시간으로 조회합니다.');
            nowTime();
        }

        if (getVal === 'prTot') {
            $("#prId").val("gif");
            $(".timelab ul").width("145%");
            $(".btn_v_wrap").css("display", "none");
            selectCGI();
            return;
        }

        if ($(this).hasClass("btn_greenline") == true) {
            $("#prId").val("cgi");
            $("#cgiOpt").val(getVal);
            selectCGI();
            return;
        }
    });

    Date.prototype.addMinutes = function (minutes) {
        this.setMinutes(this.getMinutes() + minutes);
        return this;
    };

    // tab
    var tab_cont = $('.tab_cont'),
        tab_btn = $('.tab_list li');
    tab_cont.hide();
    tab_cont.first().show();
    // tab_btn.first().addClass('on');

    $('.tab_list').on('click', 'a', function (e) {
        e.preventDefault();
        var cgiId = $("#cgiId").val();
        tab_cont.hide();
        tab_btn.removeClass('on');
        $(this).parent('li').addClass('on');
        if (cgiId == "HSR" || cgiId == "HCI") {
            $("#set").css("display", "block");
            $("#tab01").css("display", "none");
            $("#tab02").css("display", "block");
            $("#tab03").css("display", "none");
            $("#tab04").css("display", "none");
        } else if (cgiId == "MK") {
            $("#tab01").css("display", "none");
            $("#tab02").css("display", "none");
            $("#tab03").css("display", "block");
            $("#tab04").css("display", "none");
        } else if (cgiId == "PR") {
            $("#tab01").css("display", "none");
            $("#tab02").css("display", "none");
            $("#tab03").css("display", "none");
            $("#tab04").css("display", "block");
        } else if (cgiId == "STN") {
            $("#tab01").css("display", "none");
            $("#tab02").css("display", "none");
            $("#tab03").css("display", "none");
            $("#tab04").css("display", "none");
            $("#tab05").css("display", "block");
        } else {
            $("#tab01").css("display", "block");
            $("#tab02").css("display", "none");
            $("#tab03").css("display", "none");
            $("#tab04").css("display", "none");
            $("#tab05").css("display", "none");
        }
    });
    // slectlist evt
    var selList = $('[role="checklist"]'),
        selBtn = selList.find('input'),
        allBtn = $('[role="all"]');
    selBtn.each(function () {
        if ($(this).prop('checked')) $(this).parent('label').addClass('on');
    });

    selBtn.on('click', function () {
        var sel = $(this),
            selP = sel.parents('ul');
        addOn(sel);
        allEvt(selP);
    });
    allBtn.on('change', function () {
        var all = $(this);
        addOn(all);
    })

    function addOn(sel) {
        if (sel[0].type === 'radio') {
            sel.parents('ul').find('label').removeClass('on');
        }
        (sel.prop('checked')) ?
            sel.parents('label').addClass('on') :
            sel.parents('label').removeClass('on');
    }

    function allEvt(selP) {
        var checkLeng = selP.find(':checkbox:checked').length,
            allLeng = selP.find(':checkbox').length,
            thisAll = selP.prev('label').find('[role="all"]');
        (checkLeng === allLeng) ?
            thisAll.prop('checked', true).parents('label').addClass('on') :
            thisAll.prop('checked', false).parents('label').removeClass('on');
    }

    allBtn.on('change', function () {
        var sel = $(this),
            selUl = $(this).parent('label').next('ul');
        if (sel.prop('checked')) {
            selUl.find(':checkbox').prop('checked', true);
            selUl.find('label').addClass('on');
        } else {
            selUl.find(':checkbox').prop('checked', false);
            selUl.find('label').removeClass('on');
        }
    });
    $('.btn_toggle').on('click', function (e) {
        e.preventDefault();
        var cur = $(this).attr('datavalue');
        if ($(this).attr('disabled') == 'disabled') return false;
        if (cur == 'on') {
            $(this).attr('datavalue', 'off');
        } else {
            $(this).attr('datavalue', 'on');
        }
    })

})(jQuery);

