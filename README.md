# file-server

通过 SOCKET 上传文件到 SERVER 的简单实现

## usage

使用以下代码启动服务器端：

```
new FileServer(port).start(); 
```

使用以下代码从客户端上传文件到服务器端：

```
new UploadClient(server, port).upload(file);
```

具体请参考代码。:XD
