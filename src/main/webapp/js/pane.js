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
/** -*- Mode: c++; Tab-width: 4 -*-
$Id: pane.js 2354 2017-11-10 04:33:38Z fang $
*/


var codeTabs, infoTabs;
var notabspace = false;
function setTabs() {
	var codeTabSettings = {
			cache: false,
			show: function(event, ui) {
				if (ui.tab != _activeObjs.tab) removeAddNoteForm(true);
				_activeObjs.tab = ui.tab;
				_activeObjs.fid = getFid(ui.panel);
				if (ui.tab.id != fixedTabs.resultsT) {
					var noteParam = ui.tab.id;
					var noteParamArray = noteParam.split('/');
					var pidName = noteParamArray[0];
					var fPath = noteParam.substring(pidName.length + 1);
					showPrjAndNotes(fPath, pidName);
					$('.highlightedLine').removeClass("highlightedLine");
					$(".lnPane").removeClass("highlighted");
				} else {
					noteTabMainDisplayNone();
				}
				return true;
			}
	};
	var infoTabSettings = {
		cache: true,
		show: function(event, ui) {
			_activeObjs.infoTab = ui.tab;
		}
	};
	codeTabs = $("#codeTabs").tabs(codeTabSettings);
	infoTabs = $("#infoTabs").tabs(infoTabSettings);
	var codeTabTemplate = '<li class="codeTab"><a href="#{href}"><span>#{label}</span>' +
		'&nbsp<a class="close" onClick="removeTab(event); return false;" style="cursor:pointer;">X</a></a></li>';
	codeTabs.tabs('option', 'tabTemplate', codeTabTemplate);
	setHelpInfo('simpleSearchDiv');
}

function removeTab(event) {
	if (!event) event = window.event; //IE fix, IE does not pass event to its handler
	var element = event.target;
	if (!element) element = event.srcElement; //IE does not have target
	//might be faster to use dom structure directly
	var tab = $(element).parents("li").get(0);
	var idx = $('li', codeTabs).index(tab);	// 2 for resize button.
	if (!$(tab).hasClass("ui-tabs-selected")) {
		codeTabs.tabs('select', idx);
		return false;
	}
	_activeObjs.tab = null;
	_activeObjs.fid = null;

	//var tabContent = $(".ui-tabs-panel", codeTabs).get(idx); //current tab panel
	var tabHash = $('a', tab).get(0).hash;
	var tabCode = $(tabHash+" .tabody", codeTabs).get(0);
	//$(tabHash + " .stmt").unbind("mousedown mouseup");
	//Source code が表示失敗した場合に、メッセージを表示しない
	if (tabCode) {
	  tabCode.innerHTML = "Closing pane...";
	}
	//実際にタブを削除する
	codeTabs.tabs('remove', idx);
	if (event.stopPropagation) {
		event.stopPropagation();
	} else {
		event.cancelBubble = true;  //IE fix
	}
}

function nextTabId() {
	nextTabId.id += 1;
	return "ctabs-Id-"+nextTabId.id;
}
// A sequence id for all tabs
nextTabId.id = 1;

/* Help Info*/
function setHelpInfo(divName) {

    var urlval = "help/help.html";

    var div = document.getElementById("simpleSearchHelp");

    if (div == null) {
	$.ajax({
	  type: "GET",
	  url: urlval,
	  success: function(html) {
		$("#helpC").html(html);
	  }
	});
    }

    switch (divName) {
	case 'advSearchDiv':
		$("#simpleSearchHelp").hide();
		$("#advSearchHelp").show();
		$("#cloneSearchHelp").hide();
		$("#noteSearchHelp").hide();
		break;
	case 'cloneSearchDiv':
		$("#simpleSearchHelp").hide();
		$("#advSearchHelp").hide();
		$("#cloneSearchHelp").show();
		$("#noteSearchHelp").hide();
		break;
	case 'noteSearchDiv':
		$("#simpleSearchHelp").hide();
		$("#advSearchHelp").hide();
		$("#cloneSearchHelp").hide();
		$("#noteSearchHelp").show();
		break;
	default:
		$("#simpleSearchHelp").show();
		$("#advSearchHelp").hide();
		$("#cloneSearchHelp").hide();
		$("#noteSearchHelp").hide();
		break;
    }
}

function showHelpInfo(divName) {
	setHelpInfo(divName);
	infoTabs.tabs('select', "#helpC");
	showInfoPane();
}

/***
 * ノート一覧画面表示（タブ）

 * @param fpath
 * @param pid
 * @return
 */
function showFileNotes(fpath,pid){
	loadNoteInfo(fpath,pid);
	infoTabs.tabs('select', "#noteC");
	showInfoPane();
}

/***
 *
 *プロジェクト詳細情報画面表示（タブ）
 * @param pid
 * @return
 */
function showPrjInfo(pid){
	loadProject(pid);
	infoTabs.tabs('select', "#prjC");
	showInfoPane();
}

/***
 *
 * @param fpath
 * @param pid
 * @return
 */
function showPrjAndNotes(fpath,pid){
	loadProject(pid);
	loadNoteInfo(fpath,pid);
}

function addACodePane(link, pid, fp, clsFile, start, hlTerms, hlLines) {

	var	panelId = nextTabId(),
		pfid = pid+'/'+fp,
		tab = document.getElementById(pfid),
		hlTermsPattern,
	    begin = start,
	    addTabCallback;

	if (hlTerms && hlTerms.length > 0) {
		//add ( ) to the hlTerms to make sure they are in a group
		// hlTerms like "solr*fig|highligh?|this*"
		try {
			hlTermsPattern = new RegExp("(" + unescape(hlTerms) + ")", "gi");
		} catch (ex) {
			htTermsPattern = null;
		}
	}
	if (tab === null) {
	    /* function called to show  source code */
	    addTabCallback = function(fp) {
		return function(event, ui) {
			ui.tab.id = pid+'/'+fp;
//       		$("#"+ ui.panel.id).css("background", "transparent url(./img/loading-arrow.gif) 150% 150% no-repeat");
			$("#"+ ui.panel.id).html("<p>loading source for [" + fp + "]<span class='loading'></span></p>");
			setLoadingMark();

			$.ajax({type: "get",
				url: gethtmlurl(pid, fp),
				success: function(data) {

					var hlData = data;
					if (hlTermsPattern) {
						hlData = highlightData(data, hlTermsPattern);
					}
					var fileInfo= '<div class="fileinfo">';
					var dirpath = fp.substr(0,fp.lastIndexOf("/")+1);
					fileInfo +='<table><tr><td text-align="left">'+ dirpath +'<span>'+clsFile+'</span>'+'</td>';
					var  srcurl = getsrcurl(pid,fp);
					fileInfo +='<td ><a class="vlink"  href="'+ srcurl +'">' + '<img align="right" src="./img/download.png" border="0" title="ダウンロード" alt="V" width="20"> ' + '</a></td></tr></table>';
					fileInfo +='</div>';
					var tabbodydiv = '<div class="showCode tabody" onMouseDown="stmtMouseDown(event)" onMouseUp="stmtMouseUp(event)">';
					var str = '<div class="srcPane">' + fileInfo+tabbodydiv+hlData+'</div></div>';
					//don't use jQuery's html() of 1.3.2,  it is too slow due to extra check we don't need

					document.getElementById(ui.panel.id).innerHTML = str;

					_activeObjs.fid = getFid(ui.panel);
					_activeObjs.tab = ui.tab;
					// modify  start
					_activeObjs.pid = pid;

					// highlight lines for clone frags
					if (hlLines && hlLines.length > 0) {
						highlightCodeByLines(hlLines, _activeObjs.fid);
						begin = hlLines[0];
					}
					// jump
					var lcid = 'lc'+ begin + _activeObjs.fid ;
					var	viewTop = $("#"+ lcid);
					var vol = viewTop.offset().left;
					$("#"+ui.panel.id+" .tabody").scrollTo(viewTop, {margin:true, offset:{left: -vol, top: 0}});

					/* toggleProject and toggleNotes by LDNS */
					if (integrate && pid !== "") {
						showPrjAndNotes(fp,pid);
					}
					viewTop = null;
					resizeSearchResult();
					return true;
				},
				error: function(xhr, textStatus, errorThrown) {
					$("#"+ui.panel.id).append('<p class="warningMsg"> ... Ajax '+ textStatus + ": " + xhr.statusText +  "</p>");
					_activeObjs.fid = '';
					_activeObjs.tab = ui.tab;
					return true;
				}});

			codeTabs.tabs('select', "#"+ui.panel.id);

			return true;
		};
		}(fp);

		// 'tabsadd' is jQuery event, when we bind new tabsadd event,
		// events that were bound before still exists and are called.
		codeTabs.unbind('tabsadd');
		codeTabs.bind('tabsadd', addTabCallback);

		var shortName = clsFile;
		//shortName = clsFile.split(".")[0];
		shortName =  clsFile.split(".")[0].substr(0,8);
		// Tab space check
		adjustTabArea(shortName);
		if ( !notabspace ) {
			codeTabs.tabs('add', '#' + panelId, shortName);
		}
		//codeTabs.tabs('add', '#' + panelId, clsFile);

		// highlight lines in code, cloneFrags are a list of numbers
		// Cannot do highlight process or other pane operatons here
		// because loading takes time and is done asynchronously
	} else {
		// get the already open code pane and update its notes
		codeTabs.tabs('select', tab.hash);
		// rehighlight
        //TODO if we remember the previous query, then we can skip
        // some rehighlight work if the query remains same
		//remove old clone highlights
		$(tab.hash+" .clnhl").removeClass("clnhl");
		//remove old term highlights
		removeHighlightTerms(tab.hash);
		// highlight lines in code, cloneFrags are a list of numbers
		if (hlLines && hlLines.length > 0) { highlightCodeByLines(hlLines, _activeObjs.fid); }
		if (hlTermsPattern) { reHighlightTerms(tab.hash, hlTermsPattern); }
				var lcid = 'lc'+ begin + _activeObjs.fid ;
				var	viewTop = $("#"+ lcid);
                var vol = viewTop.offset().left;
                $(tab.hash).scrollTo(viewTop, {margin:true, offset:{left: -vol, top: 0}});

	}
	if (link != null) {
	    $(link).addClass("slink_visited");
	}
	return false;
}

function removeHighlightTerms(codeTabContents) {
	$(codeTabContents + " em.hl").each(function(i) {
		var parentStmt = $(this).parents(".stmt").get(0);
		if (parentStmt) {
			// there might be multiple highlight terms in one line
			// after the first one is processed, remaining ones have no more parent elements
			var oldStmt = parentStmt.innerHTML;
			//IE: em becomes EM in IE
			oldStmt = oldStmt.replace(highlightData.emPattern, "$1");
			parentStmt.innerHTML = oldStmt;
		}
	});
}

function reHighlightTerms(codeTabContents, termsPattern) {
	var cnode = $(codeTabContents + " .codePane").get(0);
	var oldhtml = cnode.innerHTML;
	cnode.innerHTML = highlightData(oldhtml, termsPattern);
}

function highlightData(str, termsPattern) {
	var txtArray = new Array();
	var srclen = str.length;
	//var re;
	var s;
	var intag = 1;
	var in_s = 0;
	var in_e = 0;
	var c;

	if (termsPattern == null) return str;

	while (in_e != -1 && in_e < srclen) {
		if (intag == 1) {
			in_e = str.indexOf(">", in_s);
			if (in_e == -1) {
				txtArray.push(str.substr(in_s));
				break;
			}
			txtArray.push(str.substr(in_s, in_e-in_s+1));
			in_s = ++in_e;
			intag = 0;
		}
		else {
			for (c = str.charAt(in_e); c != '<' && c != '&' && in_e < srclen - 1;) {
				c = str.charAt(++in_e);
			}
			s = str.substr(in_s, in_e - in_s);
			txtArray.push(s.replace(termsPattern, "<em class=\"hl\">$1</em>"));
			if (in_e == srclen - 1) {
				txtArray.push(str.charAt(in_e));
				break;
			}
			in_s = in_e++;
			if (c == '&') {
				while (in_e < srclen && in_e < in_s + 6 && c != ";") {
					c = str.charAt(in_e++);
				}
				if (c == ';') {
					//push character entities &quot; &lt; &gt; &#xxx;
					txtArray.push(str.substr(in_s, in_e - in_s));
					in_s = in_e;
				} else {
					//push & only
					txtArray.push(str.charAt(in_s));
					in_e = ++in_s;
				}
			} else {
				intag = 1;
			}
			// サイズの大きなファイルでは処理を中断する。
			if (in_s > 1000000) {
				txtArray.push(str.substr(in_s));
				break;
			}
		}
	}
	str = txtArray.join("");
	txtArray = null;
	return str;
}
// used to remove highlighted terms
// because IE automatically takes " away, while firefox automatically adds "
// IE also changes em to EM
highlightData.emPattern = new RegExp("<em class=\"?hl\"?>([^<]*)</em>", "gi");

// #8 A2
function adjustTabArea(s) {
	notabspace = false ;
	if ($("li.codeTab>a[class!='close']").length == 0)
		return;
	var totalWidth = codeTabs.width();
	var resultsTab = $("li>a#resultsT");
	var margine = resultsTab.offset().left;
	var laste = $("li.codeTab>a[class!='close']:last");
	var nextLeft = laste.offset().left + laste.width() + margine;
	var newTabWidth = (s.length+5)*(resultsTab.width()/resultsTab.text().length)+margine;
	var requiredLength = nextLeft + newTabWidth + margine;
	if (requiredLength > totalWidth) {
		notabspace = true ;
		alert("これ以上タブを開けません。不用なタブを閉じてください");
	}
}

//highlight code, specified by fid, according to the
//lines specified in "lines", which must have even number
//of integers
function highlightCodeByLines(lines, fid) {
	if (lines) {
		var len = lines.length & ~1;
 		for (var i=0; i < len; i++) {
			var sln = lines[i++]; //consumes two line numbers each time
			while (sln <= lines[i]) { //lines are inclusive both ends, use less than and equal
				$("#lc"+sln+fid).addClass("clnhl");
				sln++;
			}
		}
	}
}

