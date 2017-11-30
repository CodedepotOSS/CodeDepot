/**
* Copyright (c) 2009 SRA (Software Research Associates, Inc.)
*
* This file is part of CodeDepot.
* CodeDepot is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3.0
* as published by the Free Software Foundation and appearing in
* the file GPL.txt included in the packaging of this file.
*
* CodeDepot is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with CodeDepot. If not, see <http://www.gnu.org/licenses/>.
*
**/
// $Id: codedepot.js 2354 2017-11-10 04:33:38Z fang $

var searchURL = contextPath + '/solr/select',
    integrate = true,
    qtext = "",
    queried = "",
    xhr = null,
    items = 0,
    settings = {
		rows : 10
    },
    prersp = {},
    langInfo = { curLang: "java", unit : { java  : 'file' } };

function simpleSearch() {
	var qtext, params, node, unit;
	qtext = document.getElementById("qtext").value;
	qtext = $.trim(qtext);
	if (qtext.length === 0) {
		return false;
	}
    node = document.getElementById("simpleSearchLang").langs.options;
	langInfo.curLang = node[node.selectedIndex].value;

	params = {'q': qtext};
	if (!(params.dq && params.dq !== "")) {
        unit = langInfo.unit[langInfo.curLang];
	    if (typeof (unit) === "undefined") {
            unit = "file";
        }
		params.dq = "unit:" + unit;
	}

	if (!(params.fq && params.fq !== "")) {
		params.fq = "lang:" + langInfo.curLang;
	}

	queried = params.q;

	goSearch(params);
}

function noteSearch() {
	var qtext, qprj, qfile, quser, params, node, kind, servletPath;
	qtext = document.getElementById("qnote").value;
	qtext = $.trim(qtext);

    node = document.getElementById("noteSearchKind").kind.options;
	kind = node[node.selectedIndex].value;

    qprj = document.getElementById('noteprj').value;
	qprj = $.trim(qprj);

    qfile = document.getElementById('notefile').value;
	qfile = $.trim(qfile);

	params = {
		'mode': 'list',
		'qtext': qtext,
		'kind': kind,
		'project': qprj,
		'file': qfile
	};

	queried = qtext;
	fillResults('<p>Searching ... <span class="loading"></span></p>');
	setLoadingMark();

	servletPath = contextPath + "/admin/note/noteServlet";
	$.get(servletPath, params, function (data) {
		data = eval("(" + data + ")");
		var list = data.list;

		var html = '<p>Found ' + addDelimiter(list.length) + ' items.</p>';
		html += '<ol>';
		for (var i=0; i< list.length; i++) {
			html += '<li>'
			var path = list[i].path;
	      		var file = path.substring(path.lastIndexOf("/")+1);
		  	var link = "addACodePane(this,'" + list[i].pid + "','"
				+ list[i].path + "','" + file + "',0);";
			html += '<span class="srcName slink" onclick="' + link + '">';
			html += list[i].path;
			html += '</span>';
			html += ' in ';
		  	var plink = "showPrjInfo('" + list[i].pid + "');";
			html += '<a class="prjName plink" href="#" onclick="' + plink + '">';
			html += list[i].project;
			html += '</a>';
			html += '</li>';
			html += '<div width="80%" style="margin: 0.2em 0em;" id="notes_' + list[i].id + '"></div>';
			html += '<div style="clear: both;">&nbsp;</div>';
		}
		html += '</ol>';

		fillResults(html);

		for (var i=0; i< list.length; i++) {
			var id = "#notes_" + list[i].id;
			var table = $(id);
			var array = new Array(1);
			array[0] = list[i];
			fillNoteList(table, array, -1);
		}
	});
}

function goSearch(params, mtype) {
	var plist = $.extend({}, settings, params);
	var methodType = mtype ? mtype : "GET";

	fillResults('<p>Searching for [<span id="queried"></span>]<span class="loading"></span></p>');
	setLoadingMark();

	$.ajax({
		type: methodType,
		url: searchURL,
		data: plist,
		success: showResults,
		error: showErrorResults
	});
}


function goSearchQStr(qstrs) {
	goSearchStr(toURLParamString(settings) + "&" + qstrs);
}

function goSearchStr(str) {
	var methodType = "GET";
	fillResults('<p>Searching for [<span id="queried"></span>]<span class="loading"></span></p>');
	setLoadingMark();

	if (str.length > 6 && (str.slice(0, 6) == "qslop=")) {
		//not the best way to determine if this is a clone search, but currently the fastest
		//clone search str always starts with qslop.
		methodType = "POST";
		$.ajax({
			type: methodType,
			url: searchURL,
			data: str,
			success: showResults,
			error: showErrorResults
		});
	} else {
		$.ajax({
			type: methodType,
			url: searchURL+"?"+str,
			success: showResults,
			error: showErrorResults
		});
	}
}

/**
 * hlInfo = highlighting[docs[i].id]
 * For clone search, hlInfo is an array of three-field object
 *                 (src->"hightlight code", start->lineno, end->lineno)
 * For other search, hfInfo is one field object
 *                 (src->"highlight source code")
 */
function addHighlight(hlInfo, clicklink) {
	var r="",
	    i=0 ;
	if (hlInfo && hlInfo instanceof Array) {
		for (i=0; i< hlInfo.length; i++) {
		    var lines = hlInfo[i].src.split("\n");
		    var lineno = hlInfo[i].start;
		    var src = "";
		    for (var l = 0; l < lines.length; l++) {
			src += '<span class="ln_no">' + (lineno + l) + '</span>';
			src += lines[l] + "\n";
		    }
         	    r += addOneFragment($.trim(src), clicklink);
		}
	} else {
		//non clone search
		r += addOneFragment(hlInfo.src, clicklink);
	}
	return r;
}

/**
 * Add one piece of highlight,
 * could have multiple pieces returned.
 * @param src
 * @param clicklink
 * @return
 */
function addOneFragment(src, clicklink) {
	var result = "";
	result += "<a onclick=" + clicklink + ">";
	result += "<" + "div class=hl>";
	result += "  <" +"pre>";
	// 左揃い:tabcodeをスペースに変換して表示
	result += (src == null) ? "ハイライト情報がありません。" :src.toString().replace(/\t/g," ");
 	result += "  <" +"/pre>";
	result += "<" + "/div>";
	result += "<" +"/a>";
	return result;
}

function addPkgAction(pkg, currentParams) {
	//return pkg; //return for the time being.
	var psplit = langInfo.curLang ==='C'? "::":".",
	    pkgs = pkg.split(psplit),
	    result = "",
	    curpkg = "",
	    i ;
	for (i=0; i<pkgs.length; i++) {
		curpkg += pkgs[i];
		if (i != pkgs.length - 1) {
			curpkg += psplit;
		}
		result += '<span onclick="pkgAction(this, \'' + curpkg + '\')" style="cursor:pointer;">' + pkgs[i] + '</span>';
		if (i != pkgs.length - 1) {
			result += psplit;
		}
	}
	return result;
}

function pkgAction(obj, node) {
	var dialogP = [getDialogX(obj), getDialogY(obj)],
	    currentParams = prersp.responseHeader.params,
	    params = $.extend({}, currentParams),
	    paramsfq, html,
            negFq, negFqInfo, posFq, posFqInfo,
	    filter = "pkg:" + node; //this will return pkg.filterXXX for pkg.filter filter
	if (node.charAt(node.length-1) == '.' || node.charAt(node.length-1) == ':')
		filter += "*";
	negFq = {"fq" : "-"+filter};
	negFqInfo = negFq;
	posFq  = {'fq' : filter};
	posFqInfo = {'fq' : "+"+filter};
	if (params.fq) {
	    paramsfq = params.fq;
		if (paramsfq instanceof Array) {
			paramsfq= paramsfq.join(" AND ");
	    }
		posFq.fq = posFq.fq + " AND " + paramsfq;
		negFq.fq = negFq.fq + " AND " + paramsfq;
	}
    params = $.extend({}, params, {"start" : 0});
	html =
		"<li><a onclick=\"searchRefinement('" +
		toURLParamString($.extend(params, posFq)) +
		"\','"+posFqInfo.fq+"')\" style=\"cursor:pointer;\">" +
		"[+] " + node + "</a></li>" +
		"<br>"+
		"<li><a onclick=\"+searchRefinement('"+
		toURLParamString($.extend(params, negFq)) +
		"\','"+negFqInfo.fq+"')\" style=\"cursor:pointer;\">" +
		"[-] " + node + "</a></li>" +
		"";
	incrementalRefineQuery(html, dialogP);
}

function incrementalRefineQuery(html, offset) {
	var params = {title: "絞り込み", width:"340", height:"70", opacity: "0", minHeight: "90", minWidth: "300",
			position: offset, close: function(ev, ui){$(this).dialog('destroy');} };
	$("#refinement").html(html);
	$("#refinement").dialog(params);
}

function searchRefinement(paramStr,refFq) {
	queried += " "+ refFq;
	//queried += "AND unit:" + langInfo.unit[langInfo.curLang];
  	goSearchStr(paramStr);
	$("#refinement").dialog("close");
}


function paging(rsp) {
	var pageshtml = "",
	    numFound = rsp.response.numFound,
	    rows,
	    params, pstr, cstr,
	    prePage, nextPage, currentPage, i,j,
	    index = 0;

        rows = rsp.responseHeader.params.rows;
	if (numFound == 0 ) {
		return pageshtml;
        }

	pageshtml +="<div id=\"pagelink\" class=pagelink>";
	currentPage = rsp.responseHeader.params.start ? Math.floor(rsp.responseHeader.params.start / rows + 1) : 1;
	    hapl = currentPage-(rows/2-1);
	    rest = pages-currentPage - (rows / 2);
	if (rest < 0) {
	  hapl +=  rest;
	}


	params = rsp.responseHeader.params;
	delete params.start;
	pstr = toURLParamString(params) + "&start=";

	while ( hapl < 1) {
		index++;
	    hapl++;
	}

	if (currentPage > 1) { // pre-page
	        prePage=currentPage -1 ;
		cstr = pstr + 1;
	 	pageshtml += '<li id="imglink" class="first-page"> <a class="vlink" onclick="goSearchStr(\'' + pstr +"')\">";
	    pageshtml += "先頭</a></li>";
	    cstr = pstr + (prePage -1) * rows;
	 	pageshtml += '<li id="imglink" class="prev-page"> <a class="vlink" onclick="goSearchStr(\'' + cstr +"')\">";
	    pageshtml += "前へ</a></li>";
	}

	for ( i = hapl; i<currentPage; i++ ){
	    cstr = pstr + (i - 1) * rows;
	    pageshtml += '<li>';
	    pageshtml += "<a class=\"vlink\" onclick=\"goSearchStr(\'" + cstr + "\')\">";
	    pageshtml += i;
	    pageshtml += "</a></li>";
	}

	pageshtml += '<li class="current-page">' + currentPage + '</li>';

	index += currentPage + rows/2;
	for (j=currentPage+1; ((j<=pages ) && (j<=index)); j++) {
	    cstr = pstr + (j - 1) * rows;
	    pageshtml +="<li>";
	    pageshtml +="<a class=\"vlink\" onclick=\"goSearchStr(\'"+ cstr +"\')\">";
	    pageshtml += j;
	    pageshtml += "</a></li>";
	}
	if (currentPage < pages) { // pre-page
	    nextPage=currentPage+1 ;
	    cstr = pstr + (nextPage - 1) * rows;
	 	pageshtml +="<li id=\"imglink\" class=\"next-page\"> <a class=\"vlink\" onclick=\"goSearchStr(\'"+ cstr +"\')\">";
	    pageshtml += "次へ</a></li>";

		cstr = pstr + (pages-1)*rows;
	 	pageshtml +="<li id=\"imglink\" class=\"last-page\"> <a class=\"vlink\" onclick=\"goSearchStr(\'"+ cstr +"\')\">";
	    pageshtml += "最後</a></li>";
	}

	pageshtml +="</div>";
	return pageshtml;
}

function showResults(str, textStatus, xhr){
  var html = "",
      rsp, pageshtml,numFound,rows,
      docs, highlighting, hlTerms, hlOffset,
      oneItem, clicklink, p, lineno,
      i, srcPath, prjName, srcDir,
      pid, id, clsFile, hlInfo, paneParams,
      cloneFrags, clsAttr, tempName, srcurl;

  try {
	  rsp = eval("("+str+")"); // use eval to parse Solr's JSON response
	  prersp = rsp;
	  pageshtml = " ";
	  numFound = rsp.response.numFound;
	  rows = rsp.responseHeader.params.rows;

	  var params = rsp.responseHeader.params;
          var all = $.extend({},params,{"rows": numFound, "start": 0});

	  pages = Math.ceil(numFound / rows);
	  html += '<div id="resultInfo"><table><tr align="left"><td><span>Found ' +
 			  '<a class="vlink" onclick="goSearchStr(\'' + toURLParamString(all) + '\')"><em>' + addDelimiter(numFound) + '</em></a>' +
			  ' items (' + addDelimiter(pages) +' pages)' +
	  		  ' in ' + rsp.responseHeader.QTime / 1000 + ' seconds for [<span id="queried"></span>].</span>' +
        '</td></tr></table></div>';
	  pageshtml = paging(rsp);
	  html += pageshtml;
	  html += '<div id="showResults" class="tabody">';
	  docs = rsp.response.docs;
	  highlighting = rsp.highlighting;  //To note: highlighting is not included in rsp.response

	  hlTerms = rsp.responseHeader.params.qt == "clone" ? "" : rsp.hlTerms;
	  hlOffset = rsp.responseHeader.params.qt == "clone" ? "" : rsp.hlOffset.split(",");

	  for ( i=0; i < docs.length; i++) {
	      oneItem = '<br>';
	      srcPath = docs[i].location;
	      prjName = docs[i].prj;
	      srcDir = srcPath.substring(0, srcPath.lastIndexOf("/"));
	      id = docs[i].id ;
	      pid = docs[i].pid;

	      clsFile = srcPath.substring(srcPath.lastIndexOf("/")+1);

	      hlInfo = highlighting[docs[i].id];
	      paneParams = "'" + pid + "','"+ srcPath + "','" + clsFile  + "', '"
				+docs[i].begin+ "', '" + escape(hlTerms) + "'";
	      cloneFrags = getCloneFrags(hlInfo);
		  if (cloneFrags.length !== 0) {
			  paneParams += ", " +  "[" + cloneFrags + "]";
		  }
		  clicklink = '"addACodePane(this, ' + paneParams + ')"';
		  clsAttr = "srcName slink";
		  if (document.getElementById(pid+'/'+srcPath)) {
		    clsAttr += " slink_visited";
		  }
	      oneItem += '<span class="' + clsAttr + '" title="開く" onclick=' + clicklink + '>';
	      tempName = docs[i].id.substring(docs[i].id.lastIndexOf('/')+1);
		  oneItem += tempName.substring(tempName.indexOf('#')+1);
	      oneItem += "</span>";
	      if (null != docs[i].pkg && docs[i].pkg != "undefined" && docs[i].pkg != "") {
	      	oneItem += " in " + addPkgAction(docs[i].pkg, rsp.responseHeader.params);
	      }
	      srcurl = getsrcurl(pid,srcPath);
	      oneItem += '<div class="line2"><a class="plink" href="#" onclick="showFileNotes(\'' + srcPath + '\',\'' + pid + '\')">' + srcPath + '</a>';
	      oneItem += ' of <a class="prjName plink" title="プロジェクト情報" href="#" onclick="showPrjInfo(\''+ pid +'\')">' + prjName + '</a>';

	      if (docs[i].license && docs[i].license.length > 0 && docs[i].license != "unknown") {
	      	  oneItem += ' under <span title="ライセンス" class="license">' + docs[i].license + '</span>';
	      }
	      oneItem += '.</div>';

	      lineno = docs[i].begin;
	      if (hlOffset != "") {
		    lineno = lineno + parseInt(hlOffset[i]);
	      }
	      paneParams = "'" + pid + "','"+ srcPath + "','" + clsFile  + "', '" +lineno+ "', '" + escape(hlTerms) + "'";
	      cloneFrags = getCloneFrags(hlInfo);
		  if (cloneFrags.length !== 0) {
			  paneParams += ", " +  "[" + cloneFrags + "]";
		  }
		  clicklink = '"addACodePane(this, ' + paneParams + ')"';
	      oneItem += addHighlight(hlInfo, clicklink);

	      html += oneItem;
	      // if (rememberQuery) qStringQueue.push();  // search succeeded, let's remember it for first search
	  }
	  html += "<br/>";
	  html += pageshtml;
	  html += "</div>";
  } catch (exception) {
	  html = exception;
  }

  fillResults(html);
  resizeSearchResult();
}

function showErrorResults(xhr, textStatus, errorThrown) {
	var resultNode = $("#resultsC"),
	    messages;
	/*
	if (textStatus) {
		resultNode.append("<p>textStatus is " + textStatus + "</p>");
	}

	if (errorThrown) {
		resultNode.append("<p>error thrown is " + errorThrown);
	}
	if (xhr.statusText) {
		resultNode.append("<p>xhr.statusText is " + xhr.statusText);
	}
	*/
	if (xhr.status == "401") {
		alert("セッションがタイムアウトしました。\nログイン画面に遷移します。");
		document.location = contextPath;
		return;
	}

	if (xhr.responseText) {
		//resultNode.append("<p>response text is</p>");
		messages = xhr.responseText;
		messages = messages.replace(/<head>.*<\/head>/g, "");
		messages = messages.substr(0,messages.indexOf("<HR ")) +"</body></html>";
		resultNode.append(messages.replace(/h1>/g, "p>"));
	}
}


function getDialogX(element) {
	var x = 0,
	    e = element;
	while(e) {
		x += e.offsetLeft;
		e = e.offsetParent;
	}
	if (x > element.offsetParent.offsetWidth - 300) {
		x -= (300 - (element.offsetParent.offsetWidth - x));
		x -= element.scrollHeight;
	}
	return x;
}

function getDialogY(element) {
	var e, y = 0;
	for ( e = element; e; e = e.offsetParent)
		y += e.offsetTop;
	for (e = element.parentNode; e && e != document.body; e = e.parentNode) {
		if (e.scrollTop)
			y -= e.scrollTop;
	}
	if (y+(element.offsetHeight*6) > element.offsetParent.clientHeight)
		y -=(element.offsetHeight*7 + 5);
	else
		y += (element.offsetHeight + 5);
	return y;
}

/**
 * return pairs of line numbers that need highlight
 * @param hlInfo {src: xxx, start: xxx, end: xxx}
 * @return [start, end, start, end, start, end ...]
 */
function getCloneFrags(hlInfo) {
	var i, frags="";
	if (hlInfo && hlInfo instanceof Array) {
		for (i=0; i<hlInfo.length; i++) {
			if (frags.length == 0) {
				frags = hlInfo[i].start;
			} else {
				frags += "," + hlInfo[i].start;
			}
			frags += "," + hlInfo[i].end;
		}
	}
	return frags;
}

/**
 * replace the resultsC content with new data
 */
 function fillResults(data) {
	 document.getElementById("resultsC").innerHTML =data;
  	 $("#queried").text(queried);
	 codeTabs.tabs('select', 0);
 }

 function fillPrjInfo(data) {
	 document.getElementById("prjC").innerHTML =data;
	 codeTabs.tabs('select', 0);
 }

function addDelimiter(numbs) {
	var val  = String(numbs),
	    tmpv;
	while (val != (tmpv= val.replace(/^([+-]?\d+)(\d\d\d)/,"$1,$2"))) {
		val = tmpv;

	 }
	return val;
}

function delDelimiter(numbs) {
	return String(numbs).replace(/,/g,"");
}

function setInputText() {
	$(":input[type='text']").focus(function(){
        $(this).css("background","#EBF9F5");
	}).blur(function(){
        $(this).css("background","");
    });
}

function showInfoPane() {
    $("#contentContainer").removeClass("expand");
	$("#infoContainer").show();
	$("#resizePaneBtnH").removeClass("ui-icon-circle-arrow-w");
	$("#resizePaneBtnH").addClass("ui-icon-circle-arrow-e");
	$("#resizePaneBtnH").attr({ title: "広げる"});
}

/* set search language and licence */
function setUserInfo() {
	var node = document.getElementById("simpleSearchLang").langs.options;
	if (loginLang !== "") {
		langInfo.curLang = loginLang;
	} else {
		langInfo.curLang = getSelectedVal(node);
	}
	setSelectOption(node,langInfo.curLang);
	setLicenseInfo();
}

function setLicenseInfo(){
	var servletPath = contextPath + "/licenselist/llist" ,
	    dummydata = ["ASL","GPL"];
	if (integrate ) {
		$.get(servletPath, {}, function(data) {
			var data = eval(data);
			$("#license").autocomplete(data);
		});
	} else {
		$("#license").autocomplete(dummydata);
	}
}

function setSelectOption(node,lang) {
	var i;
	for (i = 0; i< node.length; i++) {
		if (node[i].value == lang) {
			node[i].selected = "selected";
		}
	}
}

function getSelectedVal(node) {
	var i;
	for (i = 0; i< node.length; i++) {
		if (node[i].selected == "selected") {
			return node[i].value ;
		}

	}
	return 'java';
}


function setCompleteProject() {
	var servletPath = contextPath + "/projectlist/plist" ,
            i, html;
	var projectList = new Array(0);
 	$.get(servletPath, {}, function(data) {
	   var data = eval(data);
	   for (i=0; i< data.length; i++) {
	       projectList.push(data[i].title);
	   }
	   $("#cloneprj").autocomplete(projectList);
	   $("#optprj").autocomplete(projectList);
	   $("#noteprj").autocomplete(projectList);
	});
}

function showProjectList(key) {
	var servletPath = contextPath + "/projectlist/plist";
	$.get(servletPath, {}, function(data) {
		var data = eval(data);
		if (key == 'license') {
			data.sort(function(a, b) {
    			    if(a.license < b.license) return -1;
    			    if(a.license > b.license) return 1;
    			    if(a.title < b.title) return -1;
    			    if(a.title > b.title) return 1;
    			    return 0;
			});
		}
		var html = '<h2>Project List:</h2>';
		html += '<div class="project-list">';
		html += '<div class="row">';
		if (key == 'license') {
			html += '<p class="header cell"></p>';
                	html += '<p class="header cell"><a href="#" onclick="showProjectList()">プロジェクト名</a></p>';
                	html += '<p class="header cell"><a href="#" onclick="">ライセンス</a><img src="img/down.gif"></p>';
		} else {
			html += '<p class="header cell"></p>';
                	html += '<p class="header cell"><a href="#" onclick="">プロジェクト名</a><img src="img/down.gif"></p>';
                	html += '<p class="header cell"><a href="#" onclick="showProjectList(\'license\')">ライセンス</a></p>';
		}
		html += '</div>';
		for (var i=0; i< data.length; i++) {
                        var action = "showPrjInfo('{1}')".replace("{1}", data[i].name);
			html += '<div class="row"  onclick="' + action + '">';
                        html += '<p class="cell">' + (i + 1) + '</p>';
                        html += '<p class="cell prjName plink">' + data[i].title + '</p>';
                        html += '<p class="cell prjLicense">' + data[i].license + '</p>';
			html += '</div>';
		}
		html += '</div>';
		fillResults(html);
	});
}

function showSourceFiles(prj, pid, lang, key) {
	var servletPath = contextPath + "/projectlist/plist";
	$.get(servletPath, {'pid': pid, 'lang': lang}, function(data) {
		var data = eval(data);
		if (key == 'lines') {
			data.sort(function(a, b) {
    			    if(a.lines < b.lines) return 1;
    			    if(a.lines > b.lines) return -1;
    			    if(a.path < b.path) return -1;
    			    if(a.path > b.path) return 1;
    			    return 0;
			});
		}
		var html = '<h2>Source Files in ' + prj + ':</h2>';
		html += '<div class="source-files">';
		html += '<div class="row">';
        html += '<p class="header cell"></p>';
		var action = "showSourceFiles('{1}', '{2}', '{3}', '{4}')";
                action1 = action.replace('{1}', prj).replace('{2}', pid).replace('{3}', lang).replace('{4}', "path");
                action2 = action.replace('{1}', prj).replace('{2}', pid).replace('{3}', lang).replace('{4}', "lines");
		if (key == 'lines') {
                	html += '<p class="header cell"><a href="#" onclick="' + action1 + '">ファイル名</a></p>';
                	html += '<p class="header rcell"><a href="#" onclick="">行数</a> <img src="img/down.gif"></p>';
		} else {
                	html += '<p class="header cell"><a href="#" onclick="">ファイル名</a> <img src="img/down.gif"></p>';
                	html += '<p class="header rcell"><a href="#" onclick="' + action2 + '">行数</a></p>';
		}
		html += '</div>';
		for (var i=0; i< data.length; i++) {
			var path = data[i].path;
	      		var file = path.substring(path.lastIndexOf("/")+1);
		  	var link = "addACodePane(this,'{1}','{2}','{3}',0);";
			link = link.replace("{1}", pid).replace("{2}", path).replace("{3}", file);
			html += '<div class="row" onclick="' + link + '">';
                        html += '<p class="cell">' + (i + 1) + '</p>';
                        html += '<p class="cell srcName slink">' + path + '</p>';
                        html += '<p class="cell srcLines">' + formatNumber(data[i].lines) + '</p>';
			html += '</div>';
		}
		html += '</div>';
		fillResults(html);
	});
}

function showPersonalNotes(prj, pid) {
	showProjectNotes(prj, pid, "private");
}

function showPublicNotes(prj, pid) {
	showProjectNotes(prj, pid, "public");
}

function showProjectNotes(prj, pid, kind) {
	var servletPath = contextPath + "/admin/note/noteServlet";
	var param = {
		'mode': 'list',
		'kind': kind,
		'prjName': pid
	};
	$.get(servletPath, param, function(data) {
		var data = eval("(" + data + ")");

		var html = '<h2>';
		if (kind == "public") {
			html += '公開ノート';
		} else {
			html += '個人ノート';
		}
		html += ' in ' + prj + ':</h2>';
		html += '<ol>';
		var list = data.list;
		for (var i=0; i< list.length; i++) {
			html += '<li>'
			var path = list[i].path;
	      		var file = path.substring(path.lastIndexOf("/")+1);
		  	var link = "addACodePane(this,'" + list[i].pid + "','"
				+ list[i].path + "','" + file + "',0);";
			html += '<span class="srcName slink" onclick="' + link + '">';
			html += list[i].path;
			html += '</span>';
			html += '</li>';
			html += '<div width="80%" style="margin: 0.2em 0em;" id="notes_' + list[i].id + '"></div>';
			html += '<div style="clear: both;">&nbsp;</div>';
		}
		html += '</ol>';

		fillResults(html);

		for (var i=0; i< list.length; i++) {
			var id = "#notes_" + list[i].id;
			var table = $(id);
			var array = new Array(1);
			array[0] = list[i];
			fillNoteList(table, array, -1);
		}
	});
}

function showHelpWindow() {
	var newwin = window.open("help/index.html");
	if (window.focus) {newwin.focus();}
}

function setLoadingMark() {
    $(".loading").ajaxStart(function() {
		$(".loading").css("background", "url(./img/loading.gif)  no-repeat");
    });

    $(".loading").ajaxStop(function() {
        $(".loading").css("background", "");
    });
}

function gethtmlurl(pid,fpath) {
	var htmlurl;
	htmlurl =  'download/html?pid='+ encodeURI(pid) +'&fpath=' +encodeURIComponent(fpath) + '&type=html';
	return htmlurl;
}

function getsrcurl(pid,fpath) {
	return 'download/file?pid='+ encodeURI(pid) +'&fpath=' + encodeURIComponent(fpath) + '&type=src';
}
