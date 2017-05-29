<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
System.out.println(request.getCharacterEncoding());
response.setCharacterEncoding("utf-8");
System.out.println(response.getCharacterEncoding());
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
System.out.println(path);
System.out.println(basePath);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>搜乎，搜你所想</title>
	<!-- bootstrap -->
    <link href="servlet/bootstrap/css/bootstrap.css" rel="stylesheet" />
    <link href="servlet/bootstrap/css/bootstrap-responsive.css" rel="stylesheet" />

    <!-- global font styles -->
    <style type="text/css">
        body,a,p,input,button{font-family:Arial,Verdana,"Microsoft YaHei",Georgia,Sans-serif}
        body{
      		background-size: cover;
     	}
    </style>
</head>
<body background="servlet/bj2.jpeg">
	<center>
	<!-- position of title -->
	<div style="height:70px;margin-top:170px" > </div>
  	<div style="height:102px">
  	<!-- icon and title -->
  	<h1 class="text-warning"><img src="servlet/main2.png" class="img-rounded" style="height:55px; width:55px; ">
  	<font size="6" color="#CD8500">iSearch</font>
  	<font size="6" color="#CD6839">爱上搜索</font>
  	</h1>
  	<form id="form1" name="form1" method="get" action="servlet/THUServer" class="form-search" style="margin-top:20px">
    	<label>
      		<input name="query" type="text" size="50" id="appendedInputButton" style = "width:500px; height:40px"/>
    	</label>
    	<label>
    		<input class = "btn btn-primary" type="submit" name="Submit" value="Submit" style = "width:110px; height:40px; "/>
    	</label>
   	</form>
   	</div>
   </center>
</body>
</html>
