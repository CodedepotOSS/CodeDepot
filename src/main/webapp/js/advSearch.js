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
// $Id: advSearch.js,v 1.9 2009/03/31 06:55:02 ye Exp $

var clauseNum = 3,
    advQuery,
    inputAreaOpened = true ,
    scopeOpened = false ,
    fullResults = false;

function advSearch() {
	var qtext, qstr;

	advQuery.reset();
	qtext = document.getElementById("advqtext").value;
	qtext = $.trim(qtext);
	if (qtext.length == 0) {
	   return false;
	}
	//show queried in results list
	queried =  document.getElementById("advqinfo").value;
	qstr = advQueryRewrite(qtext);
	goSearchQStr(qstr);
}

//Clause field
function Field(op, name, val) {
   this.op = op;
   this.name = name == undefined? null : name;
   this.val = val == undefined ? null : val;
}

Field.prototype.setOp = function(op) {
   this.op = op;
};

Field.prototype.setVal = function(val) {
   val.replace(/^\s*(\S.*\S)\s*$/, '$1');
   this.val = val;
};

Field.prototype.setName = function(name) {
	this.name = name;
};

Field.prototype.getName = function() {
	return this.name;
};

Field.prototype.reset = function() {
	this.op = '';
	this.name = null;
	this.val = null;
};

Field.prototype.toQString = function(first) {
	if ( this.name && this.name.length > 0 && this.val && this.val.length > 0 ) {
		if (first) {
			// first clause, no op
			return this.name + ":" + qStringEscape(this.val);
		} else {
			return (this.op ? this.op + " " : "") + this.name + ":" + qStringEscape(this.val);
		}
	}
	return "";
};

// to be deleted
Field.prototype.toString = function() {
   if (this.val == "") return ("");
   var result = "";
   if (this.val.indexOf(" ") < 0  && "+-".indexOf(this.val.charAt(0)) < 0) {
       result = this.val;
   } else {
       result = "(" + this.val + ")";
   }
   return(this.op + " " + this.name + ":" + result);
};
// End of Field

advQuery = {
	clauses : {}, //at least one is needed
	filters : {},
	unit : "file",
	lang : "java",
	termString : "",
	filterString : "",
	fieldNameMap : {
			"any" : "any",
			"code": "code",
			"comment": "comment",
			"class name" : "cls",
			"function name" : "fdef",
			"function call": "fcall",
			"parameter type" : "inTypes",
			"return type" : "outType"
	},

	reset : function() {
		var key, val, node;
		this.clauses = {};
		this.filters = {};
		this.termString = "";
		this.filterString = "";

		// 言語の選択
		$("#advSearchDiv .objlang").each(function() {
		    advQuery.lang = this.options[this.selectedIndex].value;
		});

		// Unit の選択
		this.unit = "file";
		if (this.lang.toLowerCase() == "c") {
			node = $("#advSearchDiv #CCUnit input:radio[name=cunit]:checked");
		  	this.unit = node.val();
                }
		if (this.lang.toLowerCase() == "java") {
			node = $("#advSearchDiv #JavaUnit input:radio[name=unit]:checked");
		  	this.unit = node.val();
		}

		// 検索文字列
		$("#advSearchDiv .term").each(function() {
		    key = this.parentNode.id;
		    val = this.value;
		    advQuery.setClauseFieldVal(key, val);
		});

		// AND/OR選択
		$("#advSearchDiv .andor").each(function() {
		    key = this.parentNode.id;
		    val = this.options[this.selectedIndex].value;
		    advQuery.setClauseFieldOp(key, val);
		});

		// フィールド選択
		$("#advSearchDiv .field").each(function() {
		    key = this.parentNode.id;
		    val = this.options[this.selectedIndex].value;
		    advQuery.setClauseFieldName(key, val);
		});

		// フィルター指定
		$("#advSearchDiv #scope input").each(function() {
			advQuery.filters[this.name] = $.trim(this.value);
		});

		// フィルター指定
		$("#advSearchDiv #scope select").each(function() {
			advQuery.filters[this.name] = $.trim(this.options[this.selectedIndex].value);
		});

		// 検索文字列の更新
		this.updateTermString();

		// フィルター文字列の更新
		this.updateFilterString();
	},

	addClause : function(key, op, field, val) {
		this.clauses[key] = new Field(op, field, val);
	},

	setClauseFieldName : function(key, name) {
		if (this.clauses[key] == undefined) {
			this.addClause(key, null, null, null);
		}
		var mappedName = this.fieldNameMap[$.trim(name)];
		this.clauses[key].setName(mappedName);
		this.updateTermString();
	},

	setClauseFieldVal : function(key, val) {
		if (this.clauses[key] == undefined) {
			this.addClause(key, null, null, null);
		}
		this.clauses[key].setVal($.trim(val));
		this.updateTermString();
	},

	setClauseFieldOp : function(key, op) {
		if (this.clauses[key] == undefined) {
			this.addClause(key, null, null, null);
		}
		this.clauses[key].setOp($.trim(op));
		this.updateTermString();
	},

	setFilter : function(field, val) {
		this.filters[field] = val;
		this.updateFilterString();
	},

	setUnit: function(val) {
		this.unit = val;
		this.update();
	},

	setLang: function(val) {
		this.lang = val;
		this.update();
	},

	setLangField: function(val) {
		this.lang = val;
	},


	updateTermString : function() {
		var keys = new Array(), i=0, q;

		this.termString = "";
		for (keys[i++] in this.clauses) {}/*empty body to put properties in the array */
		keys.sort();
		for (i=0; i < keys.length; i++) {
			if (this.termString.length == 0) {
				this.termString = this.clauses[keys[i]].toQString(true);
			}
			else {
				q = this.clauses[keys[i]].toQString();
				if (q.length > 0) {
				    this.termString += " " + q;
				}
			}
		}
		this.update();
	},

	updateFilterString: function() {
		var fv, f;

		this.filterString = '';
		for (f in this.filters) {
			if (f=="license" && this.filters[f] == "any") continue;
			fv = qStringEscape(this.filters[f]);
			if (fv.length == 0) continue;
			if (f!=='unit') {
				this.filterString += " " + f + ":" + fv;
			}
		}
		this.update();
	},

	update : function() {
		var qinfo, qfield, qstr;
		// this is showing very advanced text, let it be this now
		qinfo = document.getElementById("advqinfo");
		qinfo.value = (this.termString.length > 0 ? this.termString :"*:*") + " "+this.filterString ;

		qfield = document.getElementById("advqtext");
		qstr = (this.termString.length > 0 ? this.termString : "*:*") + " " + this.filterString;
		qfield.value = encodeURIComponent(qstr) + "&fq=lang:" + encodeURIComponent(this.lang) +
			(this.unit == "both" ? "" : "&dq=unit:" + encodeURIComponent(this.unit));

	}

};

function advQueryRewrite(qtext) {
	qtext = "q=" + qtext;
	return qtext;
}

/* set keyword value to advQuery */
function setFieldTerms(node) {
	var parent = node.parentNode,
		clauseKey = parent.id,
		str = node.value,
		qtext = $.trim(str);
	advQuery.setClauseFieldVal(clauseKey, node.value);
}

/* set select value to advQuery */
function setFieldName(node) {
	var parent = node.parentNode,
	    selected = node.options[node.selectedIndex],
	    clauseKey = parent.id;
	advQuery.setClauseFieldName(clauseKey, selected.value);
}

function setLogicOp(node) {
	var op ,
	    parent = node.parentNode;
	op = node.options[node.selectedIndex];
	advQuery.setClauseFieldOp(parent.id, op.value);
}

function setUnit(u) {
    if (u == "method" || u == "m*") {
        showSigSection();
    } else {
	    hideSigSection();
    }
   	advQuery.reset();
}

function setLang(type,node) {
	var lang ;
	lang = node.options[node.selectedIndex].value;
	langInfo.curLang = lang;
	if (type == "advSearch") {
       	switch (langInfo.curLang) {
       	case 'java':
                showJavaUnit();
                break;
        case 'C':
                showCCUnit();
                break;
        default:
                hideUnit()
                break;
       	}
   		advQuery.reset();
	}

	if (type == "cloneSearch") {
		if ( lang != "java") {
			showFileFilter();
		} else  {
			hideFileFilter();
		}
	}
}

function setFilterByInput(node) {
	advQuery.setFilter(node.name, node.value);
}

function setFilterBySelect(node) {
	advQuery.setFilter(node.name, node.options[node.selectedIndex].value);
}
// remove extra space, tabs, add brackets if preceded with + or - or has multiple terms
// TODO: escape special characters reserved for query
function qStringEscape(s) {
	var str = $.trim(s);
	if (str.length > 0 && (str[0] == '+' || str[0] == '-' || str.indexOf(' ') > 0)) {
		return ("(" + str + ")");
	} else {
		return str;
	}
}

function showSigSection() {
    $("#sigSection").show();
}

function hideSigSection() {
    $("#sigSection").hide();
    $('#sigSection input').each(function(i) {
    	this.value="";
    	$(this).change();
    });

    $("#sigSection  option").each(function(i){
    	this.selected = this.defaultSelected;
    });
    $("#sigSection select").change();
}


function resetSelect(node) {
	$("option", node).each(function(i) {
		if (i == 0) {
			this.selected = "selected";
		}
	});
	$(node).change();
}

function resetAdvForm() {
	var node;
	$("#advSearchForm").trigger("reset");
	advQuery.reset();
	node = document.getElementById("advSearchLang").langs.options;
       setSelectOption(node, langInfo.curLang);
       switch (langInfo.curLang) {
         case 'java':
                       showJavaUnit();
                       break;
         case 'C':
                       showCCUnit();
                       break;
         default:
                       hideUnit();
                  break;
       }
	return true;
}


function toggleSearchDiv(divName) {
	var searchForm, node;
	$(".searchDiv").hide();
	// Language select
	$("#"+divName).show();

	searchForm =  divName.substring(0,divName.indexOf("Div"))+"Lang";
	if (document.getElementById(searchForm)) {
	    node = document.getElementById(searchForm).langs.options;
	    setSelectOption(node, langInfo.curLang);
	}

	if(divName == "advSearchDiv") {
		advQuery.reset();
		switch (langInfo.curLang) {
		  case 'java':
				showJavaUnit();
				break;
		  case 'C':
				showCCUnit();
				break;
		  default:
				hideUnit();
			    break;
		}
		advQuery.setLangField($(".objlang option:selected").val());
		setSelectOption(node, langInfo.curLang);
	}
	if(divName == "cloneSearchDiv") {
		if  (langInfo.curLang == 'C') {
			showFileFilter();
		} else {
			hideFileFilter();
		}
	}
	setHelpInfo(divName);
}

//add more search clauses
function addSearchOption() {
	var cid = "clause"+clauseNum,
            preNum,
	    divName = 'clause2' ;
	if (clauseNum > 3) {
	    preNum = clauseNum - 1;
	    divName ="clause"+preNum;
	}
	//alert(cid+" insert after "+divName);

	if (clauseNum < 6) {
		$("#"+divName).clone(true).attr("id",cid).insertAfter($("#"+divName));
	    $("#"+divName+" img").hide();
		$('#'+cid+' #navigation').attr("value",'');
		clauseNum++;
	}
	advQuery.reset();
}

//delete the search clause
function delSearchOption() {
	if (clauseNum > 3) {
	    var del = clauseNum-1,
	        current = clauseNum-2,
		cid="clause"+ current,
		delid = "clause"+ del;
		//alert("delete " + delid);
		advQuery.setClauseFieldVal(delid, "", $("#advSearchDiv"));
		$('#'+delid).remove();
		clauseNum--;
	    $('#'+cid+ ' img').show();
	}
	advQuery.reset();
}


function showInputArea() {
	if (inputAreaOpened) {
		inputAreaOpened = false ;
		$("#syntax").hide();
		$("#scopewrapper").hide();
        $("#foldup").attr("src","./img/arrow-right.png");
        $("#foldup").attr("title","検索入力");

	} else {
		inputAreaOpened = true ;
		$("#syntax").show();
		$("#scopewrapper").show();
        $('#foldup').attr("src","./img/arrow-down.png");
        $('#foldup').attr("title","折畳み");

	}
}


function openWideResults() {
	if (fullResults) {
		fullResults = false ;
		$("#infoContainer").show();
		//$("#header").show();
		$("#contentContainer").css("right","25%");
		$("#contentContainer img").attr("title","全面表示");
	} else {
		fullResults = true;
		//$("#header").hide();
		$("#infoContainer").hide();
		$("#contentContainer").css("right","0%");
		$("#contentContainer img").attr("title","戻る");
	}
}

function showScope() {
	if (scopeOpened) {
		scopeOpened = false ;
		$("#scope").hide();
		$('#scopewrapper img').attr("src","./img/down_b.png");
		$('#scopewrapper img').attr("alt","+");
		$('#scopewrapper img').attr("title","検索範囲");
	} else {
		scopeOpened = true ;
		$("#scope").show();
		$('#scopewrapper img').attr("src","./img/right_b.png");
		$('#scopewrapper img').attr("alt","-");
		$('#scopewrapper img').attr("title","折畳む");
	}
}

function hideUnit() {
    $("#unit").hide();
	setUnit("file");
	$("#scope #adv_pkg").hide();
	$("#clole_pkgfilter").hide();
	$("#clole_filefilter").show();
}

function showCCUnit() {
    $("#unit").show();
    $("#JavaUnit").hide();
	$("#cfile").attr({checked: "checked"});
	$("#CCUnit").css('display', 'inline-flex')
	$("#scope #adv_pkg").show();
	$("#scope #adv_label_pkg").hide();
	$("#scope #adv_label_cns").show();
	hideSigSection();
	setUnit("file");
}

function showJavaUnit() {
    $("#unit").show();
    $("#CCUnit").hide();
	$("#javafile").attr({checked: "checked"});
	$("#JavaUnit").show();
	$("#scope #adv_pkg").show();
	$("#scope #adv_label_pkg").show();
	$("#scope #adv_label_cns").hide();
	setUnit("file");
}

function showFileFilter() {
    $("#clone_filefilter").show();
    $("#clone_pkgfilter").hide();
}

function hideFileFilter() {
    $("#clone_filefilter").hide();
    $("#clone_pkgfilter").show();
}

function getMainHeight() {
 	var wh = $(window).height() - 23;
	if ($("#header").is(':visible')) {
		wh = wh - $("#header").height();
	}
	return wh;
}

function getMainWidth() {
 	var ww = $(window).width() - 4;
	return ww;
}
