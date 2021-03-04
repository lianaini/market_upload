#使用方法

##1.使用规范
* 如果不想自行编译，可直接使用dest目录下的包  
* 使用时，需要将文件放置在source文件夹内，并命名为source.apk，
  这里会根据config文件的channel值对source.apk先修改渠道名，只支持walle方式获取渠道名  
* 跟账号相关的信息均需要用户自行配置在config.json文件中，每个字段均有注释，可以自行处理添加
* 只支持应用更新apk，不支持新增apk
  

* **小米平台：**  
进入管理后台，往下拉，可以看到图1  
![图1]（./img/小米1.png）  
点击后进入图2  
![图2]（./img/小米2.png）
  

* **华为平台：**  
进入管理后台，进入应用信息，如图1所示  
![图1]（./img/华为1.png）
点击顶部全部应用，找到Connect API 如图2所示：  
![图2]（./img/华为2.png） 
进入Connect API 页面  
如果没有API客户端则创建，如图3所示：  
![图3]（./img/华为3.png）   
  

* **VIVO平台**  
进入管理中心，下拉找到 开放能力->API传包，如图1：
![图1]（./img/VIVO1.png）  
如果没有申请Api传包权限，需要先申请，如图2：  
![图2]（./img/VIVO2.png）  

##2.注意：
* 必须已经配置好了java环境  
* 因为小米平台使用了BC库来进行加密，jar包在运行小米平台时可能会遇到  
`java.lang.SecurityException: JCE cannot authenticate the provider BC`  
的问题，具体处理方法是 ：[参考网页](https://blog.csdn.net/qq_32327553/article/details/73883440) 
    + 1.找到java的运行目录，定位到`jre/lib/security/java.security` 文件，在文件内添加
        添加一行：
        `security.provider.11=org.bouncycastle.jce.provider.BouncyCastleProvider`  
        这里的11是序号，根据已有的往下排就行  
        
    + 2.添加扩展文件，将项目内`bcprov-jdk15on-1.64.jar`文件复制到 `jre/lib/ext/`目录下
        
* 根目录下有个`mi.dev.api.public.cer`文件，需要自行替换为在小米平台申请下载的文件，注意该文件名不能修改

##3.使用：
    java -jar ./upload.jar
    
