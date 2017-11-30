/** -*- Mode:c++; tab-width: 4; indent-tab-mode: null -*- */
//$Id$
	// ハイライトJson
	var noteHighlightJson = {};
	   function setPrjAndNotePageInit(){
		   // ノート追加・変更画面部分Display=none
		   noteTabChildDisplayNone();
		   // ノート画面部分Display=none
		   noteTabMainDisplayNone();
	   }

		var maxAddTabCount = 3;
		var openedTabCount = 3;
	    var tabArray = new Array(false,false,false);
	 	// 選択のタブIndex
	    var tabIndex = 0;
	    var openTabCount = 0;
        function createNewTab(){
        	for(var i = 0;  i < tabArray.length; i++){
				if( !tabArray[i]){
					tabIndex = i;
					tabArray[i] = true;
					break;
				}
			}
        	openTabCount++;
			// ノート追加・変更画面
			var notePageName ="#note";
			$("#infoTabs").find("li").eq(tabIndex + 2).show();
			$(notePageName + tabIndex ).show();
        }

	  /***********************************************************************
		 *
		 * ノート追加・変更画面部分Display=none
		 */
		function noteTabChildDisplayNone(){
			var notePageName ="#note";
			// ノート画面#1,#2,#3 hide
			for(var i = 0; i < 3; i++){
				$("#infoTabs").find("li").eq(i + 2).hide();
				$(notePageName + i ).hide();
			}
			infoTabs.tabs('option', 'selected', 0);
		}

	  /***********************************************************************
		 *
		 * ノート画面部分Display=none
		 */
		function noteTabMainDisplayNone(){
			// ノート画面タイトル部分
			$("#noteTitleTab").css("display","none");
			// ノート画面概要部分
	       	$("#noteMainInfo").css("display","none");
	       	// ノート画面詳細部分
	       	$("#notelist").css("display","none");
	       	// アクセス制御 メッセージ
	        $("#permitMsg").css("display","none");
		}

		 // ファイル名表示用
	     var pageShowpath = "";
	     // ノート一覧詳細画面フラグ
	     var isNoteDetailPageFlag = true;

	     /*******************************************************************
			 *
			 * 初期化画面の値
			 *
			 */
	     function clearNoteDetailInfo(){
		    // 初期化
		    pageShowpath = "";
	 	    isNoteDetailPageFlag = true;
 	        // ファイルID
    	    $("#hiddenfileID").val("");
    	    // 本画面のhidden域．プロジェクト識別子名をセットする
	        $("#hiddenprjName").val("");
		    // 本画面のhidden域．ファイルのパスをセットする
	        $("#hiddenpath").val("");
	     }

	     /*******************************************************************
			 *
			 * ノート一覧画面初期化の方法
			 *
			 */
	     function loadNoteInfo(path,prjName){
		    // ノート画面部分Display=none
	    	noteTabMainDisplayNone();
			// 初期化画面の値
	    	clearNoteDetailInfo();
	    	noteHighlightJson = {};

	    	// プロジェクト識別子名を取得していない、空白画面を表示する
	     	if (isStringEmpty(path)){
				return false;
	         }
	         // ファイルのパスを取得していない、空白画面を表示する
	         if(isStringEmpty(prjName)){
	             return false;
	         }

	        var mode ="init";
	     	var pathArray = path.split(/\/|\\/);
	     	// ベース パス名
	     	pageShowpath = pathArray[pathArray.length-1];

	     	// 本画面のhidden域．プロジェクト識別子名をセットする
	     	$("#hiddenprjName").val(prjName);
			// 本画面のhidden域．ファイルのパスをセットする
	     	$("#hiddenpath").val(path);
			var paramJsonData ={"mode":mode
					   ,"prjName":prjName
					   ,"path":path
						};
			// ノート一覧画面の情報を取得する
	     	$.ajax({
				 	type:"GET"
					,dataType:"json"
					,url: root_path + "/admin/note/noteServlet"
					,data:paramJsonData
					,success:loadNoteInfoCallBack
					,error:dealErrors
				});
	     }

	     /*******************************************************************
			 *
			 * ノート一覧画面初期化CallBackの方法
			 *
			 */
	     function loadNoteInfoCallBack(data, textStatus){
		      // アクセス権限ができない
		      if(!data.permit){
		    	     // アクセス制御 メッセージ
			     $("#permitMsg").css("display","");
			     $("#permitMsg").html(permit_Msg);
			     // ノート一覧詳細画面
			     infoTabs.tabs('option', 'selected', 1);
				 return;
			  }

			  // ノート件数
	          var noteCount = data.noteCount;
	          if (noteCount == 0){
	          	$("#noteShowHref").css("display","none");
	          }else{
				  $("#noteShowHref").css("display","");
			   }
	          var fileID = data.fileID;
	          // 詳細部分hidden
	      	  $("#notelist").css("display","none");
	          // ファイルID取得成功の場合、本画面のhidden域．ファイルIDをセットする
	          if(fileID != -1){
	        	  $("#hiddenfileID").val(fileID);
			  }
	 	      // ノート一覧詳細画面フラグ
	 	      isNoteDetailPageFlag = false;
	      	  // 画面のファイル名show
			  $("#sourceName").text(pageShowpath);
			  $("#noteRecordCount").text(noteCount + "件ノートがあります。");
			  // ノートTitle部分show
	      	  $("#noteTitleTab").css("display","");
	      	  // ノート概要部分show
	      	  $("#noteMainInfo").css("display","");

			 if (noteCount > 0) {
				 showNoteList();
			 }
	      }

	 	/***********************************************************************
		 *
		 * ノート画面詳細部分を取得するメソッド
		 *
		 */
		function showNoteList(){
			var mode = "list";
			// 本画面概要部分hidden
			$("#noteMainInfo").css("display","none");
		  	// 本画面のhidden域に、ファイルIDを取得する
			var fileID = $("#hiddenfileID").val();
			var paramJsonData ={"mode":mode
					   		   ,"fileID":fileID
						};
			$.ajax({
				 type:"GET"
				,dataType:"json"
				,url: root_path + "/admin/note/noteServlet"
				,data:paramJsonData
				,success:showNoteListCallBack
				,error:dealErrors
			});
		}

		/***********************************************************************
		 *
		 * ノートでのユーザ名表示
		 *
		 */
		function showUsername(loginID, bean) {
		    if (null == bean.email || bean.email == "undefined" || bean.email == "") {
			if (bean.cuserid == loginID) {
				return '<span class="mynote">' + bean.username + '</span>';
		    	} else {
				return bean.username;
			}
		    } else {
			if (bean.cuserid == loginID) {
	 	    		return '<span class="mynote">' + bean.email + '</span>';
		    	} else {
				return bean.email;
			}
		    }
		}

		/***********************************************************************
		 *
		 * ノート画面詳細部分を取得するCallBackメソッド
		 *
		 */
		function showNoteListCallBack(data, textStatus){
			var listTable = $("#notelist");
			fillNoteList(listTable, data.list, data.loginId, true);
			resizeSearchResult();
			// 画面詳細部分
			$("#notelist").css("display","");
			// ノート一覧詳細画面フラグ
		    isNoteDetailPageFlag = true;
		}

		function fillNoteList(table, lists, loginID, link){
			table.empty();
			$.each(lists, function (i,n){
					var bean = lists[i];
					var linesInfo = "";
					// ノート情報．開始行数 と ノート情報．終了行数
					if (bean.linefrom == -1 && bean.lineto == -1){
						linesInfo ="全体";
					}else if(bean.linefrom == -1){
						linesInfo = "先頭  ― " + bean.lineto +"行";
					}else if(bean.lineto == -1){
						linesInfo = bean.linefrom + " ― 最後";
					}else {
						linesInfo = bean.linefrom + " ― " + bean.lineto +"行";
					}

					// ノートID
					var param_noteID = "note_" +  bean.id;

					var opeanoteHtml = "";
					// 本人が作成したノートだけが編集／削除できる
					if( bean.cuserid == loginID){

						opeanoteHtml = "<td background='" + root_path + "/admin/images/bg.gif' class='heightpadding editWidth rightAlign'><a class='notelink' href='javascript:modifyNodeInfo(" + i + ");'>編集</a></td>" +
	                    			   "<td  background='" + root_path + "/admin/images/bg.gif' class='heightpadding rightAlign'><a  class='notelink' href='javascript:deleteNodeInfo(" + i + ");'>削除</a></td>";
					}else{
						opeanoteHtml = "<td  class='heightpadding' background='" + root_path + "/admin/images/bg.gif'>"+
	                    			   "</td>" +
	                    			   "<td class='heightpadding' background='" + root_path + "/admin/images/bg.gif'>" +
	                    			   "</td>";
					}
					var lineLink = "";
					if (link == true) {
						lineLink += "<a class='notelink' href=javascript:line_click('" + param_noteID + "'," + bean.linefrom + "," + bean.lineto + ")>" + linesInfo + "</a>";
					} else {
						lineLink += "<label>" + linesInfo + "</label>";
					}
					table.append(
						           "<div id ='" + param_noteID + "' class='noteListTable'>" +
						            "<table class='table-tab-content'>" +
						                "<tr>" +
						                	"<td class='heightpadding numWidth' background='" + root_path + "/admin/images/bg.gif' repeat='repeat'>" +
						                	  	"<nobr>" +
						                		"<input type='hidden' id='nodeID" + i + "'" +" value='" + bean.id + "'/>" +
						                        "<strong>" + lineLink + "</strong>" +
						                        "</nobr>" +
						                    "</td>" +
						                     opeanoteHtml +
						                 "</tr>" +
						             "</table>" +
						             "<label>" +
					                 	"<textarea id='contents' class='element textarea_readOnly medium' rows='3' readonly='readonly' name='contents'>" +
					                 			replaceTextAreaBR(bean.contents)+
					                 	"</textarea>" +
					                  "</label>" +
					                  "<table class='table-tab-content'>" +
						                "<tr>" +
						                    "<td class='numWidth heightpadding'>" +
						                    	"<label>" +
						                    		showUsername(loginID, bean) +
						                    	"</label>" +
						                    "</td>" +
						                    "<td class='word-content-second2 rightAlign heightpadding'>" +
						                        "<nobr><div id ='mtime'>" +
						                    		"<label>" +
						                        		bean.mtime +
						                        	"</label>" +
						                        "</div></nobr>" +
						                    "</td>" +
						                "</tr>" +
						             "</table>" +
						           "</div>"
					);

					// ノート毎に次の JavaScript 関数 SIA
					if (bean.linefrom > 0){
						var notehighlightVar = "noteHL" + bean.linefrom;
						if(noteHighlightJson[notehighlightVar] == undefined){
							noteHighlightJson[notehighlightVar] = param_noteID;
						}else{
							var noteHLStr = noteHighlightJson[notehighlightVar];
							if(noteHLStr.indexOf(param_noteID) < 0){
								noteHLStr = noteHLStr + "/" + param_noteID;
							}
							noteHighlightJson[notehighlightVar] = noteHLStr;
						}
						mark_lineno(noteHighlightJson[notehighlightVar], bean.linefrom, bean.lineto,false);
					}
			});
			lists = null;

		}

		 /*******************************************************************
			 *
			 * ハイライト関数
			 *
			 */
		 function line_click(note_id, line_from, line_to){
			if(line_from > 0 || line_to > 0){
				if(line_from < 0){
					line_from = 1;
				}
				if(line_to < 0){
					line_to = parseInt($(".lnPane span:last-child").text());
				}
			}

			highlight_src(note_id, line_from, line_to,true);
			line_to =null;
			$(".highlightedNote").removeClass("highlightedNote");
			$("#" + note_id).addClass("highlightedNote");
			note_id = null;
			line_from = null;
        }

	  /***********************************************************************
		 *
		 * ノート情報追加メソッド
		 *
		 */
       function addNoteInfo(){
       	var mode ="add";
       	var paramJsonData = {"mode":mode
								,"prjName":$("#hiddenprjName").val()};
			$.ajax({
				 type:"GET"
				,dataType:"json"
				,url: root_path + "/admin/note/noteServlet"
				,data:paramJsonData
				,success:addNoteInfoCallBack
				,error:dealErrors
			});
       }

     /***********************************************************************
		 *
		 * ノート情報追加メソッド
		 *
		 */
       function addNoteInfoCallBack(data,textStatus){

          // アクセス制御のチェック失敗の場合
           if(!data.permit){
				alert(permit_Msg);
				return;
           }

       	  // 3個のノート追加・変更画面が開かれている場合、エラーメッセージを表示して、処理を終了する。
          	if(openTabCount > maxAddTabCount - 1){
          	 	 alert(note_Open_Msg);
				 return;
			}
          	createNewTab();

            // ラインfrom.value = toBeNoted.start;
            if (toBeNoted.start >= 0){
            	var txtFrom = "txtFrom" + tabIndex;
            	$("#" + txtFrom).val(toBeNoted.start);
             }
            // ラインto.value = toBeNoted.end;
            if(toBeNoted.end >= 0) {
    	       	var txtTo = "txtTo" + tabIndex;
                $("#" + txtTo).val(toBeNoted.end);
             }

			// ベースパス名をセットする
			$("#sourceNameChild" + tabIndex ).text($("#sourceName").text());
			// ノート追加・変更画面のプロジェクト識別子名をセットする。
			$("#childPrjName" + tabIndex).val($("#hiddenprjName").val());
			// ノート追加・変更画面のファイルのパスをセットする。
			$("#childPath" + tabIndex).val($("#hiddenpath").val());
			// ノート追加・変更画面のファイルIDをセットする。
			$("#childfileID" + tabIndex).val($("#hiddenfileID").val());

			// ノート追加・変更画面#tabIndex
			infoTabs.tabs('option', 'selected', tabIndex + 2 );
       }


       /***********************************************************************
		 *
		 * ノート情報変更メソッド
		 *
		 */
       function modifyNodeInfo(index){
       		// 3個のノート追加・変更画面が開かれている場合、エラーメッセージを表示して、処理を終了する。
       		if(openTabCount > maxAddTabCount - 1){
       			alert(note_Open_Msg);
				return;
            }
       		var mode ="modify";
            var textName = "nodeID" + index;
            var noteID = $("#" + textName).val();
			var paramJsonData = {"mode":mode
		 						,"noteID":noteID
		 						,"prjName":$("#hiddenprjName").val()};
			$.ajax({
				 type:"GET"
				,dataType:"json"
				,url: root_path + "/admin/note/noteServlet"
				,data:paramJsonData
				,success:modifyNodeInfoCallBack
				,error:dealErrors
			});
       }

       /***********************************************************************
		 *
		 * ノート情報変更CallBackメソッド
		 *
		 */
       // リセットの場合用
       var pageBean = new Array(2);
       function modifyNodeInfoCallBack(data,textStatus){

	       	// アクセス制御のチェック失敗の場合
	           if(!data.permit){
					alert(permit_Msg);
					return;
	           }
	       	createNewTab();
	       	var bean = data.noteInfoList;
	       	// 選択のタブIndex tabIndex
	       	var txtFrom = "txtFrom" + tabIndex;
	       	var txtTo = "txtTo" + tabIndex;
	       	var txtcontents = "txtcontents" + tabIndex;
	       	var chkFlag = "checkbox" + tabIndex;
	       	var sourceName = "sourceNameChild" + tabIndex;
	       	var childnodeID = "childnodeID" + tabIndex;
	       	// ノート画面のbean
	       	var noteBean = new NoteInfoBean();
			// ベースパス名をセットする
			$("#" + sourceName).text($("#sourceName").text());
			// ラインFromをセットする
			if(bean.linefrom != -1){
				$("#" + txtFrom).val(bean.linefrom);
				noteBean.linefrom = bean.linefrom;
			}
			// ラインToをセットする
			if(bean.lineto != -1){
				$("#" + txtTo).val(bean.lineto);
				noteBean.lineto = bean.lineto;
			}
			// 本文をセットする
			$("#" + txtcontents).val(replaceTextAreaBR(bean.contents));
			noteBean.cons = bean.contents;
			// ノート公開フラグをセットする
			if(bean.publicFlag){
				$("#" + chkFlag).attr("checked", true);
			}else{
				$("#" + chkFlag).attr("checked", false);
			}
			noteBean.publicFlag = bean.publicFlag;
			// ノートのbeanをセットする。
			pageBean[tabIndex] = noteBean;

			// ノートIDをセットする
		    $("#" + childnodeID).val(bean.id);
		 	// ノート追加・変更画面のプロジェクト識別子名をセットする。
			$("#childPrjName" + tabIndex).val($("#hiddenprjName").val());
			// ノート追加・変更画面のファイルのパスをセットする。
			$("#childPath" + tabIndex).val($("#hiddenpath").val());
			// ノート追加・変更画面のファイルIDをセットする。
			$("#childfileID" + tabIndex).val($("#hiddenfileID").val());

		 	// ノート追加・変更画面#tabIndex
			infoTabs.tabs('option', 'selected', tabIndex + 2 );
       }

       /***********************************************************************
		 *
		 * ノート情報登録処理 (新規/変更)
		 *
		 */

      // 登録タブのindex
      var tabItemIndex = 0 ;
      // mode
      var submitMode  = "";
      function submitTabInfo(itemIndex){

      	$(".highlightedLine").removeClass("highlightedLine");
  		$(".lnPane").removeClass("highlighted");

       	// 登録タブのindex
      	tabItemIndex = itemIndex;
        var txtFromName = "txtFrom" + itemIndex;
      	var txtToName = "txtTo" + itemIndex;
      	var txtcontentsName = "txtcontents" + itemIndex;
      	var publicFlagName = "checkbox" + itemIndex;
      	var childnodeID = "childnodeID" + itemIndex;
			var mode = "";
			var paramJsonData = {};
			// ノートを公開の値
			var notePublicFlag = "" ;
			if ( $("#" + publicFlagName).attr('checked')){
				notePublicFlag = "on";
			}else{
				notePublicFlag = "off";
			}
			// ラインFrom
			paramJsonData["lineFrom"] = $("#" + txtFromName).val();
			// ラインTo
			paramJsonData["lineTo"] = $("#" + txtToName).val();
			// 本文
			var contentsValue = $("#" + txtcontentsName).val();
			contentsValue = contentsValue.replace(/\r\n/g,'<br />');
			paramJsonData["contents"] = contentsValue;
			// ノートを公開
			paramJsonData["publicFlag"] = notePublicFlag;
			// ノート追加・変更画面のプロジェクト識別子名
			paramJsonData["project"] = $("#childPrjName" + itemIndex).val();
			// ノート追加・変更画面のファイルのパス
			paramJsonData["path"] = $("#childPath" + itemIndex).val();

      	if(isStringEmpty($("#"+ childnodeID).val())){
      		mode = "add";
      		// ノート追加・変更画面のファイルIDファイルID
      		paramJsonData["fileID"] = $("#childfileID" + itemIndex).val();
          }else{
          	mode = "modify";
      		// ノートID
      		paramJsonData["noteID"] = $("#"+ childnodeID).val();
          }
      	// mode
  		paramJsonData["mode"] = mode;
  		submitMode = mode;
      	$.ajax({
				 type:"POST"
				,dataType:"json"
				,url: root_path + "/admin/note/noteEditServlet"
				,data:paramJsonData
				,success:submitTabInfoCallBack
				,error:dealErrors
			});
      }

      // 遷移元はノート追加・変更画面のフラグ
      var isNoteEditMoveFlag = false;
      /***********************************************************************
		 *
		 * ノート情報登録処理CallBack (新規/変更)
		 *
		 */
		function submitTabInfoCallBack(data,textStatus){
			var warnMsgDiv = "noteMsg" + tabItemIndex;
			var warn = data.warn;
			if (null != warn && "" != warn)
			{
				$("#" + warnMsgDiv).html(warn);
				return;
			}
			  // ノート追加の場合
			 if ("add" == submitMode){
				 setFileID(data.fileID, tabItemIndex);
			 }
			// 一覧のプロジェクト識別子名
			var parentprjName = $("#hiddenprjName").val();
			// ノート追加・変更画面のプロジェクト識別子名
			var childprjName = $("#childPrjName" + tabItemIndex).val();
			// 一覧のファイルのパス
			var parentpath = $("#hiddenpath").val();
			// ノート追加・変更画面のファイルのパス
			var childpath = $("#childPath" + tabItemIndex).val();

			// 一覧タブのファイルと更新されたノートのファイルは同じ場合、一覧タブを更新する
			if(parentprjName == childprjName && parentpath == childpath){
				// ノート一覧画面（詳細モード）
	      		if(isNoteDetailPageFlag){
	      			showNoteList();
	          	}else{ // ノート一覧画面（概要モード）
	          		// 本画面のhidden域にプロジェクト識別子名を取得する
	              	var project = $("#hiddenprjName").val();
	           		// 本画面のhidden域にファイルのパス名を取得する
	              	var path = $("#hiddenpath").val();
	              	// ノート一覧画面概要
	              	loadNoteInfo(path,project);
	              	// 遷移元はノート追加・変更画面のフラグ
	              	isNoteEditMoveFlag = true;
	          	}
			}else{// 一覧タブのファイルと更新されたノートのファイルは違う場合、一覧タブを更新しない
			}

				if ("add" == submitMode){
					var param_noteID = "note_" +  data.noteID;
					var marklinefrom = $("#txtFrom" + tabItemIndex).val();
					if (isStringEmpty(marklinefrom)){
						marklinefrom = -1;
					}
					var marklineto = $("#txtTo" + tabItemIndex).val();
					if (isStringEmpty(marklineto)){
						marklineto = -1;
					}

					if(marklinefrom > 0){

						var notehighlightVar = "noteHL" + marklinefrom;
						if(noteHighlightJson[notehighlightVar] == undefined){
							noteHighlightJson[notehighlightVar] = param_noteID;
						}else{
							var noteHLStr = noteHighlightJson[notehighlightVar];
							if(noteHLStr.indexOf(param_noteID) < 0){
								noteHLStr = param_noteID +  "/" + noteHLStr;
						}
							noteHighlightJson[notehighlightVar] = noteHLStr;
						}

					// ノート毎に次の JavaScript 関数 SIA
						mark_lineno(noteHighlightJson[notehighlightVar], marklinefrom, marklineto,true);
					}
				}

			// ノート追加・変更画面の内容をクリア
            clearTabContext(tabItemIndex);

  		  	// 画面の切替用 ノート一覧画面を遷移する
	        hideAndSelectPage(tabItemIndex, 1);
      }

    /***************************************************************************
	 *
	 * ファイルIDをセットする。
	 *
	 */
	  function setFileID(fileID,index){
		 // 本画面プロジェクト識別子名
		 var indexPrjNameValue = $("#childPrjName" + index).val();
		 // 本画面ファイルのパス
		 var indexPathNameValue = $("#childPath" + index).val();

       	 for(i = 0; i < openedTabCount; i++){
           	 if( i != index){
				if ( indexPrjNameValue == $("#childPrjName" + i).val() && indexPathNameValue == $("#childPath" + i).val()){
					// ノート追加・変更画面のファイルID
					$("#childfileID" + i).val(fileID);
				}
             }
         }
	  }

       /***********************************************************************
		 *
		 * ノート情報削除メソッド
		 *
		 */
       function deleteNodeInfo(index){
            var mode = "del";
       		// 本画面のhidden域に、ファイルIDを取得する
			var fileID = $("#hiddenfileID").val();
            // ノートID
       		var textName = "nodeID" + index;
            var noteID = $("#" + textName).val();
            var delFlag = window.confirm(delete_Msg);
			if(delFlag){
				var paramJsonData = {"mode":mode
									,"noteID":noteID
									,"prjName":$("#hiddenprjName").val()
									,"fileID":fileID};
		 	$.ajax({
   			 	type:"GET"
   				,dataType:"json"
   				,url: root_path + "/admin/note/noteServlet"
   				,data:paramJsonData
   				,success:deleteNodeInfoCallBack
   				,error:dealErrors
   			});
			}
       }

       /***********************************************************************
		 *
		 * ノート情報削除CallBackメソッド
		 *
		 */
       function deleteNodeInfoCallBack(data,textStatus){

       	// アクセス制御のチェック失敗の場合
           if(!data.permit){
				alert(permit_Msg);
				return;
           }
       	var warn = data.warn;
       	// エラーメッセージを表示する
       	if(null != warn){
           	$("#error_msg").text(warn);
				return;
           }
       	// 本画面再表示する
       	showNoteListCallBack(data,textStatus);
       }

    /***************************************************************************
	 *
	 * 「中止」ボタンをクリック
	 *
	 */
     function closeBtnHandler(itemIndex){
          // ノート追加・変更画面の内容をクリア
          clearTabContext(itemIndex);
          // 画面の切替用 ノート一覧画面を遷移する
          hideAndSelectPage(itemIndex, 1);
     }

     /***********************************************************************
		 *
		 * ノート追加・変更画面の内容をクリア
		 *
		 */
     function clearTabContext(itemIndex){
          // ノート追加・変更画面の内容をクリア
  	      clearTabValue(itemIndex);

		  var childPrjName = "childPrjName" + itemIndex;
		  var childPathName = "childPath" + itemIndex;
		  var childFileID = "childfileID" + itemIndex;
		  var childnodeID = "childnodeID" + itemIndex;
		  // ノートIDhidden域
		  $("#" + childnodeID).val("");
		  // ノート追加・変更画面のプロジェクト識別子名
		  $("#" + childPrjName).val("");
		  // ノート追加・変更画面のファイルのパス
		  $("#" + childPathName).val("");
		  // ノート追加・変更画面のファイルID
		  $("#" + childFileID).val("");

		  // tabArrayの値再利用する
		  tabArray[itemIndex] = false;
      	  openTabCount = openTabCount - 1;

      }

      /***********************************************************************
		 *
		 * 内容をクリア
		 *
		 */
       	function clearTabValue(itemIndex){
    	    var txtFromName = "txtFrom" + itemIndex;
       		var txtToName = "txtTo" + itemIndex;
       		var txtcontentsName = "txtcontents" + itemIndex;
       		var publicFlagName = "checkbox" + itemIndex;
       		var noteErrorMsg = "noteMsg" + itemIndex;

       		// ラインFrom
			$("#" + txtFromName).val("");
			// ラインTo
			$("#" + txtToName).val("");
			// 本文
			$("#" + txtcontentsName).val("");
			// ノートを公開
			$("#" + publicFlagName).attr("checked", true);
			// エラーメッセージ
			$("#" + noteErrorMsg).html("");

       }

        /***********************************************************************
		 *
		 * 「リセット」ボタンをクリック
		 *
		 */
      function resetBtnHanlder(itemIndex){
   	 	var txtFrom = "txtFrom" + itemIndex;
       	var txtTo = "txtTo" + itemIndex;
       	var txtcontents = "txtcontents" + itemIndex;
       	var chkFlag = "checkbox" + itemIndex;
       	var sourceName = "sourceNameChild" + itemIndex;
       	var childnodeID = "childnodeID" + itemIndex;
       	if(isStringEmpty($("#" + childnodeID).val())){
       		clearTabValue(itemIndex);
        }else{
           	var tabBean = pageBean[itemIndex];
   			// ラインFromをセットする
   			$("#" + txtFrom).val(tabBean.linefrom);
   			// ラインToをセットする
   			$("#" + txtTo).val(tabBean.lineto);
   			// 本文をセットする
   			$("#" + txtcontents).val(replaceTextAreaBR(tabBean.cons));
   			// ノート公開フラグをセットする
   			if(tabBean.publicFlag){
   				$("#" + chkFlag).attr("checked", true);
   			}else{
   				$("#" + chkFlag).attr("checked", false);
   			}
   			var noteErrorMsg = "noteMsg" + itemIndex;
   			// エラーメッセージ
			$("#" + noteErrorMsg).html("");
        }
      }

    /***************************************************************************
	 *
	 * 画面の切替用
	 */
      function hideAndSelectPage(index, selectIndex){

	      var notePageName ="#note";
	      // ノート画面#1或いは#2或いは#3 hide
		  $("#infoTabs").find("li").eq(index + 2 ).hide();
		  $(notePageName + index ).hide();
		  // ノート一覧画面を遷移する
		  infoTabs.tabs('option', 'selected', selectIndex);
      }

     /***********************************************************************
		 *
		 * 空文字列であるかどうかをチェックする
		 */
      function isStringEmpty(strValue){
     	if(null == strValue || strValue == "undefined" || strValue == ""){
				return true;
         }else{
				return false;
         }
      }

      function dealErrors(XMLHttpRequest, textStatus, errorThrown){
    		window.location.href = root_path + errorPage;
      }

     // ノート画面のbean
     var NoteInfoBean = function(){
			this.linefrom = "";
			this.lineto = "";
			this.cons = "";
			this.publicFlag = true;
     };

     function loadProject(name){
    	// アクセス制御 メッセージ
	    $("#errorMsg").css("display","none");
  		var param = {"pid":name};
  		$.ajax({
  		type: "GET",
  		dataType: "json",
  		url:  root_path + "/admin/project/prjDetailForNormal",
  		data: param,
  		success: initProjectDetailCallBack,
  		error: dealErrors
  		});
  	}

  	function initProjectDetailCallBack(data, textStatus){

  	    $("#prjInfoTb").css("display","none");
  	    $("#dwUrl").css("display","block");
  	    $("#siteUrl").css("display","block");
  		var warn = data.warn;
  		var projectInfo = data.projecInfo;
  		// アクセス制御のチェック失敗の場合
        if(!data.permit){
        	$("#errorMsg").css("display","");
        	$("#errorMsg").html(permit_Msg);
  		  	infoTabs.tabs('option', 'selected', 0);
        	return;
        }
  		if (null != warn)
  		{
  			$("#errorMsg").text(warn);
  			return;
  		}else{
  		    $("#prjInfoTb").css("display","block");
  		}
  		if ("" != projectInfo.site_url){
  			$("#site-img").css("display","inline");
  			$("#siteUrl").attr("href",projectInfo.site_url);
  		}else{
  			$("#site-img").css("display","none");
  			$("#siteUrl").css("display","none");
  		}
  		if ("" != projectInfo.download_url){
  			$("#dw-img").css("display","inline");
  			$("#dwUrl").attr("href",projectInfo.download_url);
  		}else{
  			$("#dw-img").css("display","none");
	  		$("#dwUrl").css("display","none");
  		}
  		$("#projectName").text(projectInfo.title);
  		$("#projectDescription").text(projectInfo.description);
  		$("#projectRestricted").val(projectInfo.restricted);
  		$("#projectIgnores").val(projectInfo.ignores);
  		$("#projectLicense").val(projectInfo.license);
  		var listTable = $("#listTable");
  	    listTable.empty();
  		var sumlines = 0;
  		var sunNum  = 0;
  		listTable.append(
              "<tr>" +
              " <th width='13%' class='td-head'>" +
  			  "言語" +
              "</th>" +
              "<th width='25%' class='td-head'>" +
              "ファイル数" +
              "</th>" +
              "<th width='44%' class='td-head'>" +
              "行数" +
              "</th>" +
	      "<th width='18%' class='td-head'>&nbsp;</th>" +
          	"</tr>");
  	$.each(data.srcList, function(i, n) {
  	        var bean = data.srcList[i];
          	listTable.append(
              "<tr>" +
              " <td class='td-normal' align='left'>" +
  			  bean.lang +
              "</td>" +
              "<td class='td-normal' >" +
              "<div align='right'>" +
              convert(bean.num) +
              "</div>" +
              "</td>" +
              "<td class='td-normal' >" +
              "<div align='right'>" +
              convert(bean.lines) +
               "</div>" +
              "</td>" +
	      "<td class='td-normal'><div align='center'><a href='javascript:showSourceFiles(" +
		'"' + projectInfo.title + '","' + data.pid + '","' + bean.lang + '");' +
		"'>参照</a></div></td></tr>");
  		    sumlines=sumlines+bean.lines;
  		    sunNum=sunNum+bean.num;
          	});
   	listTable.append(
              "<tr>" +
              " <td class='td-total' align='left'>" +
  			"Total" +
              "</td>" +
              "<td class='td-total'>" +
              "<div align='right'>" +
              convert(sunNum) +
              "</div>" +
              "</td>" +
              "<td class='td-total'>" +
              "<div align='right'>" +
              convert(sumlines) +
              "</div>" +
              "</td>" +
	      "<td class='td-total'>&nbsp;</td>" +
          	"</tr>");

              $("#private_notes").text(addDelimiter(data.notes.personalNotes));
              $("#private_notes_link").attr("href", "javascript:showPersonalNotes('" +
			projectInfo.title + "','" + data.pid + "');");

              $("#public_notes").text(addDelimiter(data.notes.publicNotes));
              $("#public_notes_link").attr("href", "javascript:showPublicNotes('" +
			projectInfo.title + "','" + data.pid + "');");

   		// プロジェクト画面
//   		infoTabs.tabs('option', 'selected', 0);
  	}

  	function convert( number ) {
  				var _number = new Number( number );
  				var negative_flag = _number < 0 ? true : false;
  				_number = Math.abs(_number).toString();
  				var _length = _number.length;
  				var _result = '';
  				if ( _length <= 3 ) {
  					_result = _number;
  				} else {
  					var residue = _length % 3;
  					var commas = (residue == 0) ? (_length / 3 - 1) : Math.floor( _length / 3 );
  					_result = _number.substr( 0, residue ? residue : residue = 3 );
  					var _first_flag = true;
  					for ( var i = 0; i < commas; i++ ) {
  						_result += ',' + _number.substr( _first_flag ? residue : residue += 3, 3 );
  						_first_flag = false;
  					}
  				}
  				if (negative_flag) {
  					_result = '-' + _result;
  				}
  				return _result;
  	}

  	function replaceTextAreaBR(itemValue){
  		return itemValue.replace(new RegExp("<br />","gm"),"\r\n");
  	}




