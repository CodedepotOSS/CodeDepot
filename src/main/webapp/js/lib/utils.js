/** -*- mode:c++; tab-width: 4; indent-tab-mode: nil -*- */
/******
 * get (X, Y) coordinates of an event
 */
function getX(event) {
	event = event || window.event;
	return event.clientX || event.pageX;
}
function getY(event) {
	event = event || window.event;
	return event.clientY || event.pageY;
}
/*****
 * get the original element where the event takes place
 */
function getOriginalTarget(event) {
	event = event || window.event;
	return event.originalTarget || event.srcElement;
}


function toURLParamString(params, separatorChar) {
	var sepCh = '&'; //default separator
	if (separatorChar) {
		sepCh = separatorChar;
	}
	var ret = "";
	for (var p in params) {
		var v = params[p];
		// basically just for fp, which could have a list and each item should be
		// added as a separate fp clause, which has the effects of AND
		if (v instanceof Array) {
			for (var i in v) {
				//ret += "&" + p + "=" + escape(v[i]);
				ret = ret == ""? "" : ret + sepCh;
				ret += p + "=" + encodeURIComponent(v[i]); //i is index, not element
			}
		} else {
			//ret += "&" + p + "=" + escape(params[p]);
			ret = ret == ""? "" : ret + sepCh;
			ret += p + "=" + encodeURIComponent(v);
		}
	}
	//return(ret.replace(/^&/, "")); //get rid of the first &
	return(ret.replace(/'/g, "\\'"));
}
