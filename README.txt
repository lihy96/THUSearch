** THUSearch **

** 项目说明 **
本项目旨在搭建一个搜索校内站内的搜索引擎，使用java语言编写，
调试环境为Eclipse,Ubuntu16.04。

** 项目准备 **
> 需要安装maven工具,下载依赖包
	$ mvn clean
	$ mvn package
	$ mvn dependency:copy-dependencies
	会在项目目录下创建一个target/文件夹，依赖包存储在target/dependency中

** 项目运行 **
	1. 使用Eclipse运行
		Eclipse内置Tomcat运行，将target/dependency/目录下的所有jar包导入工程
		
	2. 将打包好的thusearch.war文件放置在tomcat根目录下
		TODO : 尚未测试

** 开发人员 **
> 李昊阳
> 王龙涛

