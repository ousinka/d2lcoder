## 自已搭建MarkdownNice转公众号格式神器

> 墨滴社区是一个看颜值的文章社区,技术、数学、文学文章汇集,极具趣味性,其开发的 mdnice 编辑器是一款 Markdown 微信编辑器,拥有良好的兼容性、海量主题样式、免费的图床。

mdnice其强大的markdown支撑、一键排版公众号文章、完美的公式转化都是很吸引人的地方。详细的功能介绍，可以见 [墨滴官网](<https://product.mdnice.com/>)。

但是，但是，官方的APP只有试用期7天试用期，年费大约98元/设备, 虽然可以在线转换公众号，但必须要登录等限制。


好在，mdnice是开源的，我们可以便捷地搭建自己的私有服务。

## 前提准备
- 服务器
- 域名
- SSL域名证书(非必须)
- Vue开发环境
- Git工具

## 下载官方代码

~~~
//下载代码
git clone https://github.com/mdnice/markdown-nice.git
cd markdown-nice

//安装vue依赖
npm i

//编译
npm run build
~~~

## 部署

将dist目录下的文件打包放置到服务器上。

## 添加域名解析

以我百度云为例，在 **域名服务(BCD)** -> **域名管理**  下找到相应的域名，点击 **解析** -> **添加解析** 增回一条A记录。

## Nginx配置(无证书版)
在Nginx的conf目录下，找到nginx.conf 在http节点下添加如下内容：

~~~
server {
    listen 80;
    #填写绑定证书的域名
    server_name md.d2lcoder.com;
    location / {
        # 要与部署的路径对应
        root /soft/webapps/mdnice;
        index index.html index.htm;
    } 
}
~~~

## Nginx配置(证书版)

如果有SSL证书，则可以让http自动跳转到https。在Nginx的conf目录下，找到nginx.conf 在http节点下添加如下内容：

~~~
server {
    listen 80;
    #填写绑定证书的域名
    server_name md.d2lcoder.com;
    #把http的域名请求转成https
    return 301 https://$host$request_uri; 
}

server {
    #SSL 访问端口号为 443
    listen 443 ssl; 
    #填写绑定证书的域名
    server_name md.d2lcoder.com; 
    #证书文件名称
    ssl_certificate 1_md.d2lcoder.com_bundle.crt; 
    #私钥文件名称
    ssl_certificate_key 2_md.d2lcoder.com.key; 
    ssl_session_timeout 5m;
    #请按照以下协议配置
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2; 
    #请按照以下套件配置，配置加密套件，写法遵循 openssl 标准。
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE; 
    ssl_prefer_server_ciphers on;
    location / {
        # 要与部署的路径对应
        root /soft/webapps/mdnice;
        index index.html index.htm;
    }
}
~~~

## 重启Nginx

~~~shell
nginx -s reload
~~~

## 访问
在浏览器中输入 [https://md.d2lcoder.com](https://md.d2lcoder.com) 查看效果。

**关注公众号，每周讲一些学习知识。**

![](https://www.d2lcoder.com/wechat.jpg)