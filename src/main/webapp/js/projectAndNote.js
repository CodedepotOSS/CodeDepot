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

/** -*- mode:c++; tab-width: 4; indent-tab-mode: nil -*- */
// $Id: interact.js 2354 2017-11-10 04:33:38Z fang $

var fixedTabs = {
		// changes should be made if search.html is changed.
		resultsT: "resultsT",
		prjT: "prjT",
		noteT: "noteT",
		noteC: "noteC"
};
var _activeObjs = {
		file: null,  /* must be equal to the id of the code content div, should be renamed to codeContentDivId */
		fid: "", /* the suffix following _ (including _ itself) in line number #lnDDDfid and code line #lcDDDfid */
		infoTab: null,  /* refer to the active tab on the right which provides info for the file */
		// modify by xff start
		pid:"",
		// modify  end
		tab: null /* refer to the active tab on the left, which is either a search result or a program */
		};

function initializeActiveObjs() {
    _activeObjs.infoTab = document.getElementById(fixedTabs.prjT);
    _activeObjs.tab = document.getElementById(fixedTabs.resultsT);
}

function highlight_src(note_id, line_from, line_to, hl) {
	//alert(note_id + "-----" + line_from + "-----" + line_to);

	// modfiy   2010/01/08 start
	$('.highlightedLine').removeClass("highlightedLine");
	$(".lnPane").removeClass("highlighted");
	// modfiy   2010/01/08 end

	// hl is boolean value ture: hightlight false: no hightlight
	var context = $(_activeObjs.tab.hash);
	var bgn = line_from;
	var end = line_to;
	var lid =  "";
	if (bgn >= 0 && end >= 0) {  // some lines selected
		for (var l=bgn; l<=end; l++) {
			lid = "#lc"+ l + _activeObjs.fid;
			if (hl) {
				$(lid, context).addClass("highlightedLine");
			} else {
				$(lid, context).removeClass("highlightedLine");
			}
		}
	} else { // all lines selected
		// failed to get two line numbers, must be global note
		if (hl) {
			$(".lnPane", context).addClass("highlighted");
		} else {
			$(".lnPane", context).removeClass("highlighted");
		}
	}

	// modfiy   2010/01/14 start
	  context = null;
	  bgn = null;
	  end = null;
	  lid = null;
	// jump
	  jumpIntoSourceCodeContextTarget(line_from);
	// modfiy   2010/01/14 end
}

function removeAddNoteForm(resetToBeNoted) {
	if (resetToBeNoted && resetToBeNoted == true) deHighlightToBeNoted();
	var top = document.getElementById("addNoteWrapper");
	var n = document.getElementById("addNoteForm");
	if (n != null) {
		top.removeChild(n);
		return true;
	}
	n = document.getElementById("addNoteDone");
	if (n != null) {
		top.removeChild(n);
		return true;
	}
	return false;
}

// adding notes to selected code
// TODO: put all related functions into this object
var toBeNoted = {
	fileId: null,  //the file to be highlighted, need to check if the current id is the same
	start: -1,
	end: -1,
	hilightClass: "toBeNotedClass",
	x: null,
	y: null
};

// reset toBeNoted to its default value
function setDefaultToBeNoted() {
	toBeNoted.content = null;
	toBeNoted.start = -1;
	toBeNoted.end = -1;
	toBeNoted.x = null;
	toBeNoted.y = null;
	// highlightClass does not change
}

function highlightToBeNoted() {
	if (toBeNoted.content == null) return;
	var ln;
	if (toBeNoted.start >= 0 && toBeNoted.end >= 0) {
		for (ln = toBeNoted.start; ln <= toBeNoted.end; ln++) {
			$("#lc"+ln+_activeObjs.fid, toBeNoted.content).addClass(toBeNoted.hilightClass);
			$("#ln"+ln+_activeObjs.fid, toBeNoted.content).addClass(toBeNoted.hilightClass);
		}
	}
}

function deHighlightToBeNoted() {
	if (toBeNoted.content == null) return;
	/*
	var ln;
	if (toBeNoted.start >= 0 && toBeNoted.end >= 0) {
		for (ln = toBeNoted.start; ln <= toBeNoted.end; ln++) {
			$("#lc"+ln+_activeObjs.fid, toBeNoted.content).removeClass(toBeNoted.hilightClass);
		}
	}
	*/
	$("."+toBeNoted.hilightClass, toBeNoted.content).removeClass(toBeNoted.hilightClass);
	setDefaultToBeNoted();
}

/****
 * return the line number of the event
 * return null if it fails to get line number
 */
function getLineNo(event, direction) {
    var lineNo = null,
		x = getX(event),
		y = getY(event),
		elem = getOriginalTarget(event),
		regMatches,
		stmt, tmpx;

	direction = direction || 1;

	for (var i = 0; i < 8; i++) {
		tmpx = 9;
        do {
 			// still look at the original element first, just in case some
			// browser does not support elementFromPoint yet.
			if ($(elem).is("span") && $(elem.parentNode.parentNode).is(".lnClass")) {
				regMatches = elem.id.match(/ln(\d+)/);
				if (regMatches) {
					return regMatches[1] - 0;
				}
			}
			// in case we passed the lnClass pane in some layouts
			stmt = $(elem).is(".stmt") ? elem : $(elem).parents(".stmt").get(0);
            if (stmt && $(stmt).is(".stmt")) {
                regMatches = stmt.id.match(/lc(\d+)/);
				if (regMatches) {
					return regMatches[1] - 0;
				}
            }
			tmpx = tmpx + 8;
			elem = document.elementFromPoint(tmpx, y);
		} while (tmpx < x);
		//look up or down
		y = y + (direction * 5);
		elem = document.elementFromPoint(x, y);
    }

    return null;
}

/****
 * action for note
 */
function stmtMouseDown(event) {
    var lineNo = null, elem;

	event = event || window.event,
	elem = getOriginalTarget(event);

	lineNo = getLineNo(event);
    if (lineNo == null) {
        return true; //do nothing, pass to other event handlers
    }
	deHighlightToBeNoted();
	toBeNoted.start = lineNo;
	// toBeNoted.end = -1;
	toBeNoted.x = getX(event);
	toBeNoted.y = getY(event);
	toBeNoted.content = $(elem).parents(".ui-tabs-panel").get(0); //used to speed up dom look
	return false; //event consumed, cancel further action
}

/***
 * action for note
 */
function stmtMouseUp(event) {
    var lineNo = null, end, x, y, elem;

	event = event || window.event;
	elem = getOriginalTarget(event);

	// sanity check, mouse down-up operation must take place in the same file
	if (toBeNoted.content !== $(elem).parents(".ui-tabs-panel").get(0)) {
		// doing nothing
		setDefaultToBeNoted();
		return true;
	}
	x = getX(event);
	y = getY(event);

	if (Math.abs(x - toBeNoted.x) < 3 && Math.abs(y - toBeNoted.y) < 3) {
		toBeNoted.start = toBeNoted.end = -1;
		return true;
	}

	lineNo = getLineNo(event, -1);
	if (lineNo == null) {
		return true; //do nothing, pass to other event handlers
	}

	if (lineNo < toBeNoted.start) {
		toBeNoted.end = toBeNoted.start;
		toBeNoted.start = lineNo;
	} else {
		toBeNoted.end = lineNo;
	}
	highlightToBeNoted();
	return false; //cancel further actions
}

function mark_lineno(note_id, line_from, line_to, jumpFlag) {
	if (line_from < 0 || line_to < 0) {
		///no thing to do
		return ;
	}
	// if marked already, no action
	var line = $('#ln' + line_from + _activeObjs.fid); //the line to be marked, a jquery object

	// modify  2009/01/20 start
	//if (line.find("IMG").length > 0) return;
	// mark the line only if it is not already marked

	if (line.find("IMG").length > 0){
		var  img = line.find("IMG");
		img.remove();
	}
	// modify  2009/01/20 end

	// mark the start line only if it is not already marked
	line.prepend(
			// modify  2009/01/16 start  add note_id
			"<IMG src='./img/comment.png' style='width:1.0em; height:1.0em'" +
			"onClick=showNotesBasedOnStartingLine("+line_from+",'" + note_id + "') " +
			" />"
	);

	 //
	 if(jumpFlag){
		 jumpIntoSourceCodeContextTarget(line_from);
	 }
	// modify  2009/01/16 end
}


/***
 *
 * highlight note method  created
 *
 * @param lineno
 * @return
 */
function highlight_note(note_id){
	$('.highlightedNote').removeClass("highlightedNote");
	var noteIDArray = note_id.split('/');
	for(var i = 0; i < noteIDArray.length; i++){
		$("#" + noteIDArray[i]).addClass("highlightedNote");
	}
}

/***
 *
 * Source Code page site move  created
 *
 * @param lineno
 * @return
 */
function jumpIntoSourceCodeContextTarget(lineno){
	var beginLine = lineno;
	if(beginLine <= 0){
		beginLine = 1;
	}
	// jump
	var lcid = 'lc'+ beginLine + _activeObjs.fid ;
	var	viewTop = $("#"+ lcid);
	var tabody = ".tabody";
	jumpIntoContext(tabody, viewTop);
}

/***
 *  Note page site move  created
 * @param noteID
 * @return
 */
function jumpIntoNoteContextTarget(noteID){
	var tabody = ".noteScroll";
	var	viewTop = $("#"+ noteID);
	jumpIntoContext(tabody, viewTop);
}

/***
 *  jump method  created
 * @param contextBody
 * @param top
 * @return
 */
function jumpIntoContext(contextBody, top){
	var vol = top.offset().left;
	//$("#"+ui.panel.id).scrollTo(viewTop, {margin:true, offset:{left: -vol, top: 0}});
	$(contextBody).scrollTo(top, {margin:true, offset:{left: -vol, top: 0}});

}

function showNotesBasedOnStartingLine(ln,noteID) {
	// $('#lns'+ln).toggle();
	// showActiveTab(document.getElementById("noteT"), "info");
	infoTabs.tabs('select', "#"+fixedTabs.noteC);

	if ($("#noteMainInfo").is(':visible')) {
		showNoteList();
	}

	jumpIntoNoteContextTarget(noteID.split('/')[0]);
	highlight_note(noteID);
	// modify   2010/01/14 end

	showInfoPane();
}


/// return the fid following line numbers in lcDDDfid, lnDDDfid
/// fid currently generated during the conversion of java to html
function getFid(context) {
	var firststmt=$(".stmt", context).get(0);
	if (firststmt) {
		return firststmt.id.replace(/^lc\d+/, "");
	} else {
		return '';
	}
}
