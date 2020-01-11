    xnetter是基于netty的网络架构，目的是简单易用。
    用户只需要关心基础数据和业务逻辑，对网络通信的过程、网络数据的编解码、加解密、路由转发无
需关心。目前实现了http/https、websocket/wss、tcp和udp网络过程的封装。其中tcp和udp支持加
解密， 只不过目前加密算法只有RC4，后面再扩展其他的。

1 netty介绍
    netty是jboss提供的一个java开源框架，netty提供异步的、事件驱动的网络应用程序框架和工具，
用以快速开发高性能、高可用性的网络服务器和客户端程序。也就是说netty是一个基于nio的编程框
架，使用netty可以快速的开发出一个网络应用。

2 http
    通过HttpServer启动http服务器，该服务会自动扫描Action路径下的所有类进行注册。下面是http
请求的整个流程。已经支持https，通过HttpConf去配置。
    HttpClient(WEB) -> HttpHandler -> HttpRouter -> Decode -> Encode -> Action
    HttpServer接收到客户端的连接，都会启动一个新的HttpHandler。当HttpHandler接收到客户端
数据请求，会根据请求路径找HttpRouter查询相应的Action。调用Decoder对Request请求参数和请求内
容进行解析，并通过Encoder将解析结果转换为Action的响应函数需要的参数数组，最后执行该响应函数，
并将执行结果返回给客户端。
    http请求参数到Action响应函数参数的映射，支持下面的几种方式：
    基础类型 -> 基础类型
    数组    -> 数组/List/Set
    JSON    -> Map/Bean

3 websocket
    通过HttpServer启动websocket服务器，该服务会自动扫描Action路径下的所有类进行注册，只不过
这里的Action需要实现接口WSockAction。websocket初始化时，由HttpClient发起Get请求，其中Headers
里面包含“Upgrade”=“websocket”的键值对。这时服务器需要启动握手流程，并且把从网络处理里面把
HttpHandler移除，加入新的WSockHandler。以后数据通信就由WSockHandler负责了。
    已经支持wss，通过HttpConf去配置。客户端通过wss访问时，不支持IP和端口，应该通过域名的形式来
访问。

4 tcp
    通过继承Server来启动服务器，继承Client来启动客户端。这里需要涉及到的配置参数由Manager.Conf
完成。每一个连接，都会实例化一个Handler来接收并处理数据。下面是tcp的接收和发送流程。
    Recv: Client -> Decode -> Handler -> Dispatcher -> Action
    Send: Handler -> Encode -> Client
    为了简便大家使用，提供了Protocol作为收发数据的基础实现，大家也可以仿照Protocol来实现其他的
通信数据框架。但是需要自己实现自己的编解码器（继承自Coder)、分发器（继承自Dispatcher）、自己的
处理器（继承自Handler）。当然业务逻辑处理的Action是必须的。

    为了大家使用方便，提供了MultiClient类，它可以维护多个客户端，分别去连接不同的服务器。如果
相应的服务器发生变化，可以通过方法updateClients去调整。但有一点需要记住的是：每一个客户端需要
有一个remoteId来做标识，所以建立连接的时候，需要主动调用MultiClient的registClient方法去设置
该Client的remoteId。

5 udp
    upd的实现流程跟tcp很类似。只不过服务器需要继承自UdpServer，客户端需要继承自UdpClient。
udp是无连接的，客户端发送数据总是向指定的IP和端口发送，服务器启动单线程监听端口并接收数据。
为了提高服务器处理性能，用RecvHandler来接收数据，并从对象池handlers里面获取一个UdpHandler来
处理数据。udp的处理器需要继承自UdpHandler，可以仿照ProtocolUdpHandler来写。
    注意：udp是无连接的，所以服务器发送数据只能原路返回。

TODO LIST
1 http参数校验
2 http支持文件上传、文件下载
3 考虑其他协议的实现