<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String htmlPath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>搜索结果</title>
    <!-- bootstrap -->
    
    <link href="bootstrap/css/bootstrap.css" rel="stylesheet" />
    <link href="bootstrap/css/bootstrap-responsive.css" rel="stylesheet" />
    <link href="bootstrap/css/bootstrap-responsive.css" rel="stylesheet" />
    <link href="bootstrap/js/bootstrap.min.js" rel="stylesheet" />
	    			  	<link rel="stylesheet" href="css/speech-input.css">
						<link rel="stylesheet" href="css/demo.css">	
    <script src="js/jquery-1.11.3.min.js"></script>
    <script src="bootstrap/js/bootstrap-typeahead.js"/>
    
	
    <!--  link rel="stylesheet" type="text/css" href="css/floating-scroll.css"/>
    <script src="js/jquery.sticky-div.js"></script>
    <script src="js/jquery.floating-scroll.js"></script> -->

    <script src="bootstrap/js/bootstrap-typeahead.js"></script>
    <!-- global font styles -->
    <style type="text/css">
        body,a,p,input,button{font-family:Arial,Verdana,"Microsoft YaHei",Georgia,Sans-serif}
        body{
        	margin-left: 0;
      		background-size: cover;
      		padding-top:70px;
     	}
     	h3{
     		font-size: 1.5em;
		    margin: 0.5em 0 0.4em;
		    font-family: sans-serif;
		    font-weight: normal;
     	}

    </style>
</head>
<body>
<%
	String currentQuery=(String) request.getAttribute("currentQuery");
	int currentPage=(Integer) request.getAttribute("currentPage");
	String[] autoComplete = (String[]) request.getAttribute("autoComplete");
	String[] recommendWords = (String[]) request.getAttribute("recommendWords");
%>

<script>
	$(document).ready(function($) {
	   // Workaround for bug in mouse item selection
	   $.fn.typeahead.Constructor.prototype.blur = function() {
	      var that = this;
	      setTimeout(function () { that.hide() }, 250);
	   };
	 
	   $('#appendedInputButton').typeahead({
	      source: function(query, process) {
	         return [
	         <% for (int i = 0; i < autoComplete.length; ++i) { %>
	         	"<%= autoComplete[i] %>",
	         <% } %> ""];
	      }
	   });
	})
</script>


<div class = "container">
	<div class="navbar navbar-default navbar-fixed-top">
	   	<div id = "content-div" class = "row-fluid" style="background:#F5F5F5; color:#FFF">
	   	<!-- div class="container">
	   	<div class="row" -->
	  		<div style="text-align:left;">
	  		  <div style="display:inline-block">
		  		<h3 class="text-warning">
					<img src="main2.png" class="img-rounded" style="height:40px; width:40px; ">  	
					<font size="6" color="#CD8500">iSearch</font>
				  	<!-- font size="6" color="#CD6839">Search</font -->
				</h3>
			  </div>

			  <div style="display:inline-block">
				
				<form id="form1" name="form1" method="get" action="THUServer" 
			  		class="form-search">	    
				    <label>
				      <input autocomplete="off" data-provide="typeahead" 
				      		data-items="4" name="query" value="<%=currentQuery%>" 
				      		id="appendedInputButton" type="text" size="70" 
				      		style = "width:410px;height:30px" data-items="4"
				      		class="speech-input" onfocus="style.backgroundColor='#FFFFFF'" 
				      		onblur="style.backgroundColor='#DCDCDC'" data-patience="3"
				      		lang="zh-Hans"/>
				      <input type="submit" name="Submit" value="搜索" style="margin-top:0px"
				      		class = "btn btn-primary" />
			   		</label>
			  	</form>
			  </div>
			</div>
		</div>
	</div>
</div>

<div class = "row-fluid" style="text-align:left">
	<div class="span1"></div>
	<div class="span5" style="text-align:left">
	  	<table class = "table table-hover">
		  	<%
		  	String[] htmlTags=(String[]) request.getAttribute("htmlTags");
		  	String[] htmlPaths=(String[]) request.getAttribute("htmlPaths");
		  	String[] absContent=(String[]) request.getAttribute("absContent");
		  	String[] imgPaths=(String[]) request.getAttribute("imgPaths");
		  	String[] spellCheckWords = (String[]) request.getAttribute("spellCheckWords");
		  	
			if(spellCheckWords!=null && spellCheckWords.length>0){
		  		%><tr><td>
		  		
		  		
		  		<img src="idea64.png" class="img-rounded" style="height:24px; width:24px; ">
		  		<font size="3" color="#CD8500">你要找的是不是： </font>
		  		
		  		<%
		  		for(int i=0; i < Math.min(spellCheckWords.length, 3); i++){
		  			if (spellCheckWords[i] != null && spellCheckWords[i].equals(currentQuery)) continue;
			  		%><a class="text-info" href="/THUSearch/servlet/THUServer?query=<%= spellCheckWords[i] %>&Submit=Submit">
			  			<font size="3" color="#1E90FF"> <%= spellCheckWords[i] %></font>
			  		</a>&nbsp; <%
			  	}
		  		%></td></tr> <%
	  		}
		  	
		  	if(htmlTags!=null && htmlTags.length>0){
		  		for(int i=0;i<htmlTags.length;i++){%>
	  		<p>
	  			<tr>
	  				<td>
		  			<!-- title -->
		  			<a href="<%=htmlPaths[i]%>"><font size="4"><%=(currentPage-1)*10+i+1%>. 
		  			<%
		  				String title = htmlTags[i];
		  				int tix = title.indexOf(currentQuery);
		  				if (tix != -1) {
		  					String first = title.substring(0, tix);
		  					String last = title.substring(tix+currentQuery.length());%>
				  			<%=first %><mark><%=currentQuery %></mark><%=last %>
		  				<%} 
		  				else {%>
		  					<%=htmlTags[i] %>
		  				<%} %>
		  			</font></a>
		  			
		  			</br>
					
					<!-- 8 is the length of /mirror/ -->
					<font size="3" color="#80CF80"><%=htmlPaths[i]%></font>
		  			
		  			<!-- /td -->
		 		<!-- /tr -->
		 			</br>
				<!--  tr -->
					<!-- td -->
					<div class="row-fluid" align="left">	
						<% if(imgPaths[i] != null) { %>
							<div class = "span2">
								<img  src="<%=imgPaths[i]%>">
							</div>
							<div  class = "span8">
						<% } else { %>
							<div class = "span10">
						<% } %>
						
			
			  			<%
			  				String content = absContent[i];
			  				int idx = content.indexOf(currentQuery);
			  				if (idx != -1) {
			  					String first = content.substring(0, idx);
			  					String last = content.substring(idx+currentQuery.length());%>
					  			<%=first %><mark><%=currentQuery %></mark><%=last %>
			  				<% }
			  				else {%>
			  					<%=content %>
			  				<%}%>
			
						</div>
					</div>
					
					</td>
		  		</tr>
			</p>
	  		<%}; %>
	  	<%}else{ %>
	  		<p><tr><h3>no such result</h3></tr></p>
	  	<%}; %>
	  	</Table>
	  	<div class = "pagination">
		  	<ul>
				<%if(currentPage>1){ %>
					<li><a href="THUServer?query=<%=currentQuery%>&page=<%=currentPage-1%>">上一页</a></li>
				<%}; %>
				<%for (int i=Math.max(1,currentPage-5);i<currentPage;i++){%>
					<li><a href="THUServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a></a></li>
				<%}; %>
				<!-- <strong><%=currentPage%></strong> -->
				    <li class="disabled"><a href = ""><%=currentPage%></a></li>
				<%for (int i=currentPage+1;i<=currentPage+5;i++){ %>
					<li><a href="THUServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a></li>
				<%}; %>
				    <li><a href="THUServer?query=<%=currentQuery%>&page=<%=currentPage+1%>">下一页</a></li>
			</ul>
		</div>
	</div> 
	<div class="span6" >
		<div >
			</br>
			</br>
			</br>
			</br>
			</br>
			<table style="margin-left:100px; border-left: medium inset #E0E0E0; border-top: hidden" class="table">
				<%
			  	if(recommendWords!=null && recommendWords.length>0){
			  		%><tr><td> 
			  			
			  			<h4 class = "text-success" ><img src="network.png" class="img-rounded" style="height:24px; width:24px; "></img>
			  				<font  color="#CD8500">相关搜索：</font></h4>
			  				</br>
			  				 <%
						  	for(int i=0; i < recommendWords.length;i++){ 
						  	
						  		if (recommendWords[i] == null) continue;
						  			%>
						  		<a class = "text-warning" href="/THUSearch/servlet/THUServer?query=<%= recommendWords[i] %>&Submit=Submit">
						  		<font size="3" color="#1E90FF"><%= recommendWords[i] %></font>
						  		</a>
					  			<br/>
				  			<% }
			  		%></td></tr> <%
			  	}
			  	%>
	  		</table>
  		</div>>
	</div>
</div>

<script src="js/speech-input.js"></script>
</body>