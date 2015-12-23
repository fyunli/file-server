# file-server
一个简单的通过SOCKET上传文件SERVER

## usage

使用以下代码启动服务器端：

```
new FileServer(7878).start(); 
```

使用以下代码从客户端上传文件到服务器端：

```
new UploadClient(server, port).upload(file);
```

具体请参考代码。:XD
