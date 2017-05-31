# THUSearch


## 项目说明

	本项目旨在搭建一个搜索校内站内的搜索引擎，使用java语言编写，
	调试环境为Eclipse,Ubuntu16.04。


## 项目准备


#### 需要安装maven工具,下载依赖包

	$ mvn clean
	$ mvn package
	$ mvn dependency:copy-dependencies
	会在项目目录下创建一个target/文件夹，依赖包存储在target/dependency中


#### Eclipse 说明：

1. 打开DynamicWeb Project
2. 连续两次next,修改ContentDirectory:WebContent -> WebRoot
3. 右键项目，Deployment Assembly 添加folder, /target/dependency -- WEB-INF/lib (注意WEB前没有斜杠)
4. Java Build Path >> Add JARS >> target/dependency目录下的所有jar包 >> Apply
5. Servers项目 >> Run as >> Run Configrations >> Arguments >> WorkDirectory >> 修改你的项目根目录
6. Servers栏目 >> Tomcat... >> Add and Remove... >> THUSearch
7. 切换到你的当前分支


## 项目运行

1. 使用Eclipse运行

	Eclipse内置Tomcat运行，将target/dependency/目录下的所有jar包导入工程
	
2. 将打包好的thusearch.war文件放置在tomcat根目录下

	TODO : 尚未测试


## Feature

	网页正文抽取，
	pagerank分析
	pdf,doc,docx,xml内容解析
	前端关键词高亮
	maven动态下载依赖包，ant编译
	ansj分词（人名识别，数字识别），IKAnalyzer效果不好
	jsoup解析html
	关键词更正
	相似单词
	自动补全
	图片提取、
	语音搜索


## Good case

	清华，计算机系			官网
	社会主意，tinghua		关键词更正
	师资队伍				图片提取
	

## 开发人员

李昊阳 前端 
王龙涛 后端 

