# 毕业设计说明书
## 一、java部分说明
java版本：java1.8  
IDE：IntelliJ IDEA  
整个最终项目位于 ``` com.lw ``` 之下，```material``` 中放的是本次项目中使用的```.ifc``` 文件，```work``` 中放的就是提取关键信息的 ```java``` 代码，而关键信息被存储到另外的 ```html_json``` 文件夹中，供 ```three.js``` 使用。
## 二、html与json部分说明
编辑器：VScode（要安装 Liver Server插件，搭建本地小型服务器以使用本地 ```.json``` 文件）  
运行环境：Google Chrome  
```html_json``` 文件夹中 ```.json``` 文件是用于存储关键数据，```.html``` 文件用于可视化这些数据，其余的 ```.js``` 文件是提供给 ```html``` 文件中的 ```JS``` 代码引用。