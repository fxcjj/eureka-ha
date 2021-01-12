1 eureka集群高可用部署
    1.1 Eureka界面说明
    1.2 同一台机器搭建3个节点及问题
    1.3 将eureka节点分别部署


2.2 eureka集群高可用部署
2.2.1 Eureka界面说明

当eureka server中配置的lease-expiration-duration-in-seconds
小于
eureka client中配置的lease-renewal-interval-in-seconds时。
在eureka页面（http://localhost:8761）会出现以下：
RENEWALS ARE LESSER THAN THE THRESHOLD. THE SELF PRESERVATION MODE IS TURNED OFF. THIS MAY NOT PROTECT INSTANCE EXPIRY IN CASE OF NETWORK/OTHER PROBLEMS.

a) System Status
系统状态信息

b) DS Replicas
指的是可用的服务副本，全称 Discovery Server Replicas，译为发现服务副本。
这里显示的内容，取决于eureka.client.service-url.defaultZone的配置，如该配置项配置为：
defaultZone: http://peer2:8762/eureka/,http://peer3:8763/eureka/

c) Instances currently registered with Eureka
当前在集群的eureka服务上注册的所有实例
* Application
该列显示每个实例的应用名称，可以通过spring.application.name和eureka.instance.appname进行配置，前者优先级更高

# AMIs

# Availability Zones

# Status
该列显示每个实例的instance ID和实例状态

1) 当配置了 instance-id, 同时也配置了 prefer-ip-address 时，则展示 instance-id 配置的值，如：UP (1) - 192.168.141.78:8761。
点击地址，当instance-id配置为主机名(${spring.cloud.client.hostname}:${server.port})或者ip(${spring.cloud.client.ip-address}:${server.port})时，如：instance-id: ${spring.cloud.client.hostname}:${server.port}，
则跳转地址为：http://192.168.141.78:8761/actuator/info，也就是说优先使用了ip。

2) 当配置了 instance-id, 未配置了 prefer-ip-address 时，则展示 instance-id 配置的值，如：UP (1) - 192.168.141.78:8761。
点击地址，当instance-id配置为主机名(${spring.cloud.client.hostname}:${server.port})或者ip(${spring.cloud.client.ip-address}:${server.port})时，如：instance-id: ${spring.cloud.client.hostname}:${server.port}，
则跳转地址为：http://peer1:8761/actuator/info

3) 当未配置 instance-id，未配置 prefer-ip-address 时，则展示的值取 instance-id 的默认值（当前eureka机器的主机名:应用名:端口，即 ${spring.cloud.client.hostname}:${spring.application.name}:${server.port}），如：192.168.141.78:eureka02:8761。
此时点击Status下的地址，则跳转地址为：http://peer1:8761/actuator/info

4) 当未配置 instance-id，配置了 prefer-ip-address 时，则展示的值取 instance-id 的默认值（当前eureka机器的主机名:应用名:端口，即 ${spring.cloud.client.hostname}:${spring.application.name}:${server.port}），如：192.168.141.78:eureka02:8761。
点击地址，则跳转地址为：http://192.168.141.78:8761/actuator/info，也就是说优先使用了ip。

d) General Info
当前实例基本状况

e) Instancce Info
实例信息

2.2.2 同一台机器搭建3个节点及问题
a) 各eureka配置及hosts文件配置
eureka01实例application.yml配置如下：
server:
  port: 8761
spring:
  application:
    name: eureka01
  # 加上这个，启动时一直使用8080端口！
#  profiles: peer1
eureka:
  # eureka实例相关配置
  instance:
    # eureka实例主机名
    hostname: peer1
    # 表示在eureka server在接收到上一个心跳之后等待下一个心跳的秒数（默认90秒），若不能在指定时间内收到心跳，则移除此实例，并禁止此实例的流量
    # 必须高于 lease-renewal-interval-in-seconds
#    lease-expiration-duration-in-seconds: 90
    # 表示 Eureka Client 向 Eureka Server 发送心跳的频率（默认 30 秒），
    # 如果在 lease-expiration-duration-in-seconds 指定的时间内未收到心跳，则移除该实例。貌似在eureka server中配置没什么用处！
#    lease-renewal-interval-in-seconds: 20

    # 注册到eureka中的唯一标记，目前作用仅是显示
    # 默认取值为 主机名:应用名:端口，即 ${spring.cloud.client.hostname}:${spring.application.name}:${server.port}}
    # 当前启动Eureka的机器的IP: ${spring.cloud.client.ip-address}
    instance-id: ${spring.application.name}:${server.port}

    # 集群中各服务之间互相访问默认使用hostname进行，但是这样需要在各个机器上配置hosts文件。将该配置设为true，可以尽量使用ip进行通信
    prefer-ip-address: true
  client:
    service-url:
      # 集群环境中，其它的eureka节点
      defaultZone: http://peer2:8762/eureka/,http://peer3:8763/eureka/
    # 指示此实例是否应将其信息注册到eureka服务器以供其他人发现
    # 如果是单个节点，应该设置为false，表示自己就是注册中心，不用注册自己。
    # 在集群环境中，应该设置为true，表示注册自己（defaultZone不用填自己地址，填写其它eureka服务地址）
    # 默认true
    register-with-eureka: true
    # 默认true
    fetch-registry: true
  server:
    # 自我保护模式，默认打开（当出现网络分区导致丢失过多客户端时，不删除任何微服务）
    enable-self-preservation: false

eureka02实例application.yml配置如下：
server:
  port: 8762

spring:
  application:
    name: eureka02

eureka:
  # 当前eureka实例相关配置
  instance:
    # eureka实例主机名
    hostname: peer2
    # 注册到eureka中的唯一标记
    instance-id: ${spring.application.name}:${server.port}

  # 当前eureka client端相关配置
  client:
    # 注册中心地址
    service-url:
      defaultZone: http://peer1:8761/eureka/,http://peer3:8763/eureka/

  # 当前eureka server端相关配置
  server:
    enable-self-preservation: false


eureka03实例application.yml配置如下：
server:
  port: 8763

spring:
  application:
    name: eureka03

eureka:
  # 当前eureka实例相关配置
  instance:
    # eureka实例主机名
    hostname: peer3
    # 注册到eureka中的唯一标记，目前作用仅是显示
    instance-id: ${spring.application.name}:${server.port}
  # 当前eureka client端相关配置
  client:
    # 注册中心地址
    service-url:
      defaultZone: http://peer1:8761/eureka/,http://peer2:8763/eureka/

  # 当前eureka server端相关配置
  server:
    enable-self-preservation: false

本机hosts文件配置如下：
127.0.0.1 peer1
127.0.0.1 peer2
127.0.0.1 peer3

b) 测试
启动eureka01,eureka02,eureka03。
分别访问
http://localhost:8761/
http://localhost:8762/
http://localhost:8763/
可以看到在eureka的DS replicas一栏中，出现另外两个服务副本，这里是正常的。
但是在 General Info 一栏 unavailable-replicas 中出现其它两个服务，
而 available-replicas 没有服务。

问题1：为什么unavailable-replicas中出现服务，而 available-replicas 没有服务？
答：在同一台服务器上，当eureka集群中各个节点的eureka.instance.hostname值相同时，会被标记为 unavailable-replicas。
此处配置的eureka.instance.hostname为peer1/peer2/peer3。取的是hosts文件中对应的ip，因为三个ip相同，所以被标记为 unavailable-replicas。
如果要显示正常，则每个节点需要单独部署。

2.2.3 将eureka节点分别部署
a) 将eureka02部署到虚拟机131机器上，并启动。
eureka02配置文件如下：




b) 将eureka03部署到虚拟机135机器上，并启动。
eureka02配置文件如下：

c) 本机启动eureka01服务


d) 测试


// 源码
https://www.jianshu.com/p/8b69f97b0fdc
// 在线扩充eureka节点
https://www.jianshu.com/p/fbe5574ba6e8