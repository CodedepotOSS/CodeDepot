var errorPage = "/error/500.html";
function getPages(curPage, allPages, fn)
{
	var pageRange = 10;
	var before = "";
	var after = "";
	for (i = 1; i < pageRange && 0 < (curPage - i); i++)
	{
		var num = curPage - i;
		before = "<a href='#?page=" + num +"' onclick='" + fn + "(" + num + ");'>" + num + "</a> " + before;
	}
	for (j = 1; j <= pageRange && (curPage + j) <= allPages; j++)
	{
		var num = curPage + j;
		after += "<a href='#?page=" + num + "' onclick='" + fn + "(" + num + ");'>" + num + "</a> ";
	}
	var cur = "<span class='current'>" + curPage + "</span> ";
	var all = before + cur + after;
	if (1 != curPage)
	{
		var head = "<a href='#?page=1' onclick='" + fn + "(1);'>先頭</a> <a href='#?page=" + (curPage - 1) + "' onclick='" + fn + "(" + (curPage - 1) + ");'>前へ</a>";
		all = head + all;
	}
	if (curPage != allPages)
	{
		var tail = "<a href='#?page=" + (curPage + 1) + "' onclick='" + fn + "(" + (curPage + 1) + ");'>次へ</a> <a href='#?page=" + allPages + "' onclick='" + fn + "(" + allPages + ");'>最後</a>";
		all = all + tail;
	}
	return all;
}

function common_getMsg(msgid, msgparams, fn)
{
	var msg = "";
	var params = {"method": "getMsg", "msgid": msgid, "msgparams": msgparams};
	var paramStr = $.param(params);
	$.ajax({
		type: "GET",
		dataType: "json",
		url: "../msg",
		data: paramStr,
		success: fn
		});
}

function common_showMessageText(msg)
{
	if ("" == msg) {
		$("#messagetext").empty();
	} else {
		$("#messagetext").append(msg);
	}
}

function formatNumber(s) {
	if(/[^0-9\.]/.test(s)) return "invalid value";
	s=(s+".").replace(/^(\d*)$/,"$1.");
	s=s.replace(/(\d*\.\d\d)\d*/,"$1");

	s=s.replace(".",",");
	var re=/(\d)(\d{3},)/;

	while(re.test(s))
	        s=s.replace(re,"$1,$2");
	s=s.replace(/,(\d\d)$/,".$1");

	s = s.substring(0, s.length - 1);
	return s.replace(/^\./, "0.");
}

function checkIsIE6() {
	var Sys = {};
    var ua = navigator.userAgent.toLowerCase();
    var s;
    (s = ua.match(/msie ([\d.]+)/)) ? Sys.ie = s[1] :
    (s = ua.match(/firefox\/([\d.]+)/)) ? Sys.firefox = s[1] :
    (s = ua.match(/chrome\/([\d.]+)/)) ? Sys.chrome = s[1] :
    (s = ua.match(/opera.([\d.]+)/)) ? Sys.opera = s[1] :
    (s = ua.match(/version\/([\d.]+).*safari/)) ? Sys.safari = s[1] : 0;
	if(Sys.ie == '6.0')
		return true;
	return false;
}

function loadCSS() {
	if (loadCSS.arguments.length <= 1)
		return;
	var isIE6 = checkIsIE6();
	var prefix = loadCSS.arguments[0];
	if (isIE6 == true) {
		prefix = loadCSS.arguments[0] + "ie6/";
	}
	for (var i=1;i<loadCSS.arguments.length;i++){
		var head = document.getElementsByTagName('HEAD').item(0);
		var style = document.createElement('link');
		style.href = prefix + loadCSS.arguments[i];
		style.rel = 'stylesheet';
		style.type = 'text/css';
		head.appendChild(style);
   	}
}

/* only Run at IE */
function resizeSearchResult() {
    if (checkIsIE6() == true) {
	var mh = getMainHeight();
	var mw = getMainWidth() - 20;

	$("#infoContainer").width(mw* 0.3);
	$("#infoContainer").height(mh);
    	$("#infoTabs").width($("#infoContainer").width() - 2);
    	$("#infoTabs").height($("#infoContainer").height() - 2);

	$("#contentContainer").height(mh);
    	$("#codeTabs").height($("#contentContainer").height() - 2);
	if ($("#infoContainer").is(':visible')) {
		$("#contentContainer").width(mw * 0.7);
    		$("#codeTabs").width(mw * 0.7 - 2);
	} else {
		$("#contentContainer").width(mw);
    		$("#codeTabs").width(mw  - 2);
	}

    	$("#prjC").width($("#infoTabs").width() - 2);
    	$("#prjC").height($("#infoTabs").height() - 24);
    	$("#noteC").width($("#infoTabs").width() - 2);
    	$("#noteC").height($("#infoTabs").height() - 24);
    	$("#helpC").width($("#infoTabs").width() - 2);
    	$("#helpC").height($("#infoTabs").height() - 24);

	$("#resultsC").width($("#codeTabs").width() - 2);
	$("#resultsC").height($("#codeTabs").height() - 24);
	$(".srcPane").width($("#codeTabs").width() - 2);
        $(".srcPane").height($("#codeTabs").height() - 24);

        $(".srcPane .tabody").width($("#codeTabs").width() - 2);
        $(".srcPane .tabody").height($("#codeTabs").height() - 44);
    }
}
