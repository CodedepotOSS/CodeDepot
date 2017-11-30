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
var cloneFilter = {
	prj: '',
	pkg: '',
	file: ''
};

function cloneSearch(nodeId) {

	//alert("here");
	var qstr, qtext, prjstr, slopv, node,
            langVal, langfield, unitName, params;
	qstr = document.getElementById(nodeId).value;
	qtext = $.trim(qstr);
	prjstr = document.getElementById('cloneprj').value;
	pkgstr = document.getElementById('clonepkg').value;
	codestr = document.getElementById('clonecode').value;
	filestr = document.getElementById('clonefile').value;
	filterStr = '';

	if (qtext.length == 0) {
		return false;
	}

	//var slopv = document.getElementById("slop").value;
	 slopv = $('input[name=slop]:checked').val();
	node = document.getElementById("cloneSearchLang").langs;
	langVal = node.options[node.selectedIndex].value;
	langfield = 'lang:' + langVal;
	unitName = 'm*';

	if (prjstr.length >0)
		filterStr += ' prj:('+ prjstr + ')';
	if (codestr.length >0)
		filterStr += ' codestr:('+ codestr + ')';
	if (langVal == 'java') {
	    filterStr += pkgstr.length >0 ? ' pkg:('+ pkgstr + ')' : '';
	    unitName = "class";
	} else {
	    filterStr += filestr.length >0 ? ' file:('+ filestr + ')'  : '';
	    unitName = "file";
	}

	params = {
				qt: "clone",
				defType: "codedepot",
				q: qstr,
				fq: "(unit:"+ unitName + " " + langfield + filterStr+")",
				lang: langVal,
				qslop: slopv,
				dslop: slopv};
	if (qtext.length > 40)  {
		queried = qtext.substr(0,39) + "...";
	} else {
			queried = qtext;
   	}
	if ( filterStr.length > 0)
		queried += " AND " + filterStr;
	goSearch(params, "POST");
	return false;
}

function setCloneFilter(node) {
	if (node !== null) {
		switch (node.name) {
			case 'prj':
				cloneFilter.prj = node.value;
				break;
			case 'pkg':
				cloneFilter.pkg = node.value;
				break;
			case 'file':
				cloneFilter.file = node.value;
				break;
			case 'code':
				cloneFilter.code = node.value;
				break;
			default:
				break;
		}
	}
}
