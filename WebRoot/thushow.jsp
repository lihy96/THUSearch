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
    <link rel="stylesheet" type="text/css" href="css/floating-scroll.css"/>
    <script src="js/jquery-1.11.3.min.js"></script>
    <script src="js/jquery.sticky-div.js"></script>
    <script src="js/jquery.floating-scroll.js"></script>

    <script src="bootstrap/js/bootstrap-typeahead.js"></script>
    <!-- global font styles -->
    <style type="text/css">
        body,a,p,input,button{font-family:Arial,Verdana,"Microsoft YaHei",Georgia,Sans-serif}
        body{
      		background-size: cover;
      		padding-top:70px;
     	}

    </style>
</head>
<body>
<%
	String currentQuery=(String) request.getAttribute("currentQuery");
	int currentPage=(Integer) request.getAttribute("currentPage");
%>


<div class = "container">
	<div class="navbar navbar-default navbar-fixed-top">
	   	<div id = "content-div" class = "row-fluid" style="background:#F5F5F5; color:#FFF">
	   	<!-- div class="container">
	   	<div class="row" -->
	  		<div style="text-align:center;">
	  		  <div style="display:inline-block">
		  		<h3 class="text-warning" style="height:50%;">
					<img src="main2.png" class="img-rounded" style="height:40px; width:40px; ">  	
					<font size="6" color="#CD8500"> Tsinghua</font>
				  	<font size="6" color="#CD6839">Search</font>
				</h3>
			  </div>

			  <div style="display:inline-block">				
				<form id="form1" name="form1" method="get" action="THUServer" 
			  		class="form-search">	    
				    <label>
				      <input autocomplete="off" data-provide="typeahead" 
				      		data-items="4" name="query" value="<%=currentQuery%>" 
				      		id="appendedInputButton" type="text" size="70" 
				      		style = "width:400px;" data-items="4" />
				      <input type="submit" name="Submit" value="  搜索   " style="margin-top:0px"
				      		class = "btn btn-primary" />
			   		</label>
			  	</form>
			  </div>
			</div>
			<!-- div class="span4" style="margin-top: 20px;">
			  	<form id="form2" name="form2" method="get" action="THUServer" class="form-search">   		   
				    <label>
				    	<input type="submit" name="Submit" value="  搜索   "  class = "btn btn-primary" />
				    </label>
			  	</form>
			</div-->

		<!-- /div -->
		</div>
	</div>





<div class = "row-fluid">	
  	<div class="span2">
	<!-- this is for recommend -->
	</div>

	<div class = "span8">
  	<table class = "table table-hover">
  	<%
  	String[] htmlTags=(String[]) request.getAttribute("htmlTags");
  	String[] htmlPaths=(String[]) request.getAttribute("htmlPaths");
  	String[] absContent=(String[]) request.getAttribute("absContent");

  	String[] imgPaths=(String[]) request.getAttribute("imgPaths");
  	if(htmlTags!=null && htmlTags.length>0){
  		for(int i=0;i<htmlTags.length;i++){%>
  		<p>
  		<tr>
  			<!-- title -->
	  			<a href="<%=htmlPaths[i]%>"><%=(currentPage-1)*10+i+1%>. 
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
	  			</a>
 		</tr>
 

		
		<tr>
		<div class="row-fluid">
			
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
  		</tr>

  		</div>

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

  
  	<div class="span2">
	<!-- this is for recommend -->
	</div>
  
  
</div>
	
</div>


</body>