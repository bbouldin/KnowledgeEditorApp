<%@ page contentType="text/html;charset=UTF-8" %>

<http-equiv="Content-Type" content="text/html; charset=UTF-8">
<head>
  <title></title>
<g:javascript src="jquery-2.1.1.min.js" />
<!--g:resource dir="css" file="TextAnalyzer.css" absolute="true"/ -->
%{--<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'TextAnalyzer.css', absolute: 'true')}"/>--}%
<asset:stylesheet src="TextAnalyzer.css"/>
<style>
.bigsubmitbutton {
    width: 30%;
    height: 10%;
}
.bigcheckbox {
    width: 5%;
    height: 5%;
}
.bigfonts {
    font-size: xx-large
}
</style>
</head>

<body>

<div class="container">
    <!-- h3>Knowledge Entry Tool</h3 -->
    <div class="all_text">
        Process Text: <br>
        <g:form action="event">
            <!-- g:textField name="string" value="${defaultPath}" size="85" /-->
            <input type="text" name="string" value="${partAnalyzedText}" size="70" id="mainTextInput"/>
            <g:submitButton name="processType" style="color:#666666;" class="save mysubmit" id="processType_all" value="Process"/>
            <g:submitButton name="processType" style="color:#666666;" class="save mysubmit" id="processType_step" value="Process Step"/>
        </g:form>
    </div>

    <g:if test="${aText != null || interimGraph != null}">
        <g:set var="someGraph" value="${(aText != null) ? aText : interimGraph}" />
        <g:set var="allowEdits" value="${(aText != null) ? false : true}" />
        <div class="all_text">
            KB NL Graph:<br>
            <div class="text">
                <g:each status ="i" var="aBuildKBNLGraph" in="${someGraph.getKbNLGraphs()}"> <!-- was aText or interimGraph-->
                    <div class="sent">
                        <g:each status ="j" var="aKBNLNode" in="${aBuildKBNLGraph.getKbNLGraph().getKbNLNodes()}">
                            Node ${j}: <span>${aKBNLNode.getSemanticPossibilities()}</span>
                            <input type="text" class="aKBNLNode_values" id="aKBNLNode_${i},${j}" value ="" placeholder="Enter a valid Cyc constant separated by comma." style="display:none"/>
                            <g:if test="${allowEdits}">
                                <button type="button" class="aKBNLNode_enter" id="${i},${j}" style="display:none">enter</button>
                                <button type="button" class="aKBNLNode_edit" id="${i},${j}">edit</button>
                            </g:if>
                            <br>
                            <div class="grayout" style="color:lightgray">&nbsp;&nbsp;&nbsp;&nbsp;NL: ${aKBNLNode.getNlParse()}</div><br>
                        </g:each>
                        <br>
                        <g:each status ="j" var="aRelEdge" in="${aBuildKBNLGraph.getKbNLGraph().getDg().edgeSet()}">
                            Edge ${j}: ${aRelEdge.getEdge().getSemanticRelation()}<br>
                            &nbsp;&nbsp;&nbsp;&nbsp;From: ${aRelEdge.getFromNode().getSemanticPossibilities()}<br>
                            &nbsp;&nbsp;&nbsp;&nbsp;To&nbsp;&nbsp;&nbsp;&nbsp;: ${aRelEdge.getToNode().getSemanticPossibilities()}<br>
                            <div class="grayout" style="color:lightgray">&nbsp;&nbsp;&nbsp;&nbsp;NL: ${aRelEdge.getEdge().getNlParse()}</div>
                        </g:each>
                    </div>
                </g:each>
            </div>
        </div>
    </g:if>

    <g:form action="assertSent">
        <div class="all_text">
            Assert selected sentences:<br>
            <g:each status="i" var="aText" in="${session["analyzed_texts"]}">
                <div class="text">
                    <g:each status="j" var="aTextSentKE" in="${aText.getPotentialKE()}">
                        <div class="sent">
                            Sentence: ${aTextSentKE.getBuildKBNLGraph().getSentenceString()} <br>
                            <g:each status="k" var="aKEComb" in="${aTextSentKE.getCombinations()}">
                                <div class="ke_comb">
                                    <g:each status="l" var="aKESent" in="${aKEComb.getSentences()}">
                                        <input name="items" value="${i},${j},${k},${l}" type="checkbox" class="mycheck"/>
                                        <input name="negLits" value="${i},${j},${k},${l}" type="checkbox" class="mycheck"/>
                                        ${aKESent} <br>
                                    </g:each>
                                    <g:if test="${!aKEComb.getTerms().isEmpty()}">
                                        <br>
                                        <g:each status="l1" var="aKETerm" in="${aKEComb.getTerms()}">
                                            <table> <!--border="1"-->
                                                <tr>
                                                    <td style="width:60px">
                                                        ${aKETerm}
                                                    </td>
                                                    <td style="width:380px">
                                                        ${aKETerm.getSboData().get("constantName")}
                                                    </td>
                                                </tr>
                                            </table>
                                        </g:each>
                                    </g:if>
                                </div>
                            </g:each>
                        </div>
                    </g:each>
                </div>
            </g:each>
            <g:submitButton name="kbInteraction" class="save mysubmit" value="Assert"/>
            <g:submitButton name="kbInteraction" class="save mysubmit" value="Assert Rule"/>
            <g:submitButton name="kbInteraction" class="save mysubmit" value="Query"/>
        </div>
    </g:form>

    <div class="all_text">
        Query Results:
        <g:if test="${resSet != null}">
            ${resSet}
        </g:if>
    </div>

    <g:form action="clearAnalyzedTextCache">
        <g:submitButton name="blah2" class="save mysubmit" value="Clear Text Cache"/>
    </g:form>

    <g:form action="clearAPICache">
        <g:submitButton name="blah2" class="save mysubmit" value="Clear API Cache"/>
    </g:form>


    <!--div class="got_result">
      something....
    </div-->

    <script>

        $('button.aKBNLNode_edit').click(function() {
            // var x = ".aKBNLNode_values#" + $(this).attr("id");
            var $thisNode = $(this);
            $.post("/TestCycApp/event/getNewConstantName", {node_id: $thisNode.attr("id")})
                    .done (function( data ) {
                        $thisNode.prev().prev().attr("value", data);
                    });
            // $(this).prev().prev().attr("value", data_out);
            $(this).prev().css("display", "inline-block");
            $(this).prev().prev().css("display", "inline-block");

            // alert($(this).prev().attr("id"));
        });

        $('button.aKBNLNode_enter').click(function() {
            var $thisNode = $(this);
            $.post("/TestCycApp/event/modifyKBNLNode", {node_id: $thisNode.attr("id"), value: $thisNode.prev().attr("value")})
                    .done (function( data ) {
                        // thisNode.prev().prev().attr("value", data);
                        $thisNode.prev().prev().html(data);
                    });
            $(this).css("display", "none");
            $(this).prev().css("display", "none");

        });

        $('div.grayout').hover(
                function () {
                    $(this).css("color", "black");
                }, function () {
                    $(this).css("color", "lightgray");
                }
        );

        //      $('div.got_result').click(
        //        function () {
        //          $.ajax({url: "/TestCycApp/event/index"}).done (function( data ) {  alert( data );});
        //        }
        //      );

        // $('input#processType_step').prop("disabled", true);
        $('input#processType_step').css("color", "black");
        $('input#processType_all').css("color", "black");

        if( /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ) {
            $(".mysubmit").addClass("bigsubmitbutton").addClass("bigfonts");
            $(".mycheck").addClass("bigcheckbox").addClass("bigfonts");
            $(".container").addClass("bigfonts");
            $("#mainTextInput").addClass("bigfonts").attr("size", 45);
        }

    </script>
</div>
</body>
</html>
