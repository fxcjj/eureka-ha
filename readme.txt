1 Eureka界面说明
2 eureka集群高可用部署
    2.1 同一台机器搭建多个节点及问题
        2.1.1 同一个项目通过指定spring.profiles.active运行不同实例
        2.2.2 创建两个不同的项目
    2.2 将eureka节点独立部署

1 Eureka界面说明
当eureka server中配置的lease-expiration-duration-in-seconds
小于
eureka client中配置的lease-renewal-interval-in-seconds时。
在eureka页面（http://localhost:17651）会出现以下：
RENEWALS ARE LESSER THAN THE THRESHOLD. THE SELF PRESERVATION MODE IS TURNED OFF. THIS MAY NOT PROTECT INSTANCE EXPIRY IN CASE OF NETWORK/OTHER PROBLEMS.

a) System Status
系统状态信息

b) DS Replicas
指的是可用的服务副本，全称 Discovery Server Replicas，译为发现服务副本。
这里显示的内容，取决于eureka.client.service-url.defaultZone的配置，如该配置项配置为：
defaultZone: http://peer2:17652/eureka/,http://peer3:17653/eureka/

c) Instances currently registered with Eureka
当前在集群的eureka服务上注册的所有实例
* Application
该列显示每个实例的应用名称，可以通过spring.application.name和eureka.instance.appname进行配置，前者优先级更高

# AMIs

# Availability Zones

# Status
该列显示每个实例的instance ID和实例状态

1) 当配置了 instance-id, 同时也配置了 prefer-ip-address 时，则展示 instance-id 配置的值，如：UP (1) - 192.168.141.78:17651。
点击地址，当instance-id配置为主机名(${spring.cloud.client.hostname}:${server.port})或者ip(${spring.cloud.client.ip-address}:${server.port})时，如：instance-id: ${spring.cloud.client.hostname}:${server.port}，
则跳转地址为：http://192.168.141.78:17651/actuator/info，也就是说优先使用了ip。

2) 当配置了 instance-id, 未配置了 prefer-ip-address 时，则展示 instance-id 配置的值，如：UP (1) - 192.168.141.78:17651。
点击地址，当instance-id配置为主机名(${spring.cloud.client.hostname}:${server.port})或者ip(${spring.cloud.client.ip-address}:${server.port})时，如：instance-id: ${spring.cloud.client.hostname}:${server.port}，
则跳转地址为：http://peer1:17651/actuator/info

3) 当未配置 instance-id，未配置 prefer-ip-address 时，则展示的值取 instance-id 的默认值（当前eureka机器的主机名:应用名:端口，即 ${spring.cloud.client.hostname}:${spring.application.name}:${server.port}），如：192.168.141.78:eureka02:17651。
此时点击Status下的地址，则跳转地址为：http://peer1:17651/actuator/info

4) 当未配置 instance-id，配置了 prefer-ip-address 时，则展示的值取 instance-id 的默认值（当前eureka机器的主机名:应用名:端口，即 ${spring.cloud.client.hostname}:${spring.application.name}:${server.port}），如：192.168.141.78:eureka02:17651。
点击地址，则跳转地址为：http://192.168.141.78:17651/actuator/info，也就是说优先使用了ip。

d) General Info
当前实例基本状况

e) Instancce Info
实例信息

2 eureka集群高可用部署
2.1 同一台机器搭建多个节点及问题
2.1.1 同一个项目通过指定spring.profiles.active运行不同实例
本案例使用eureka01项目测试，在resources目录下建立如下配置文件。
bootstrap.yml, application-default.yml, application-peer1.yml, application-peer2.yml

bootstrap.yml配置如下：
spring:
  profiles:
    # 指定配置文件
    active: peer1
    # 无条件地激活配置文件
    include: default

application-default.yml配置如下：
#server:
#  port: 6161
spring:
  application:
    # 要使eureka界面 available-replicas 可用，必须spring.application.name相同或者eureka.instance.appname相同
    name: eureka-server
eureka:
  # eureka实例相关配置
  instance:
    # 集群中各服务之间互相访问默认使用hostname进行，但是这样需要在各个机器上配置hosts文件。将该配置设为true，可以尽量使用ip进行通信，默认false
    # 这里要设置成false，为true时，available-replicas为空
    prefer-ip-address: false
#    appname: eureka-server

application-peer1.yml配置如下：
server:
  port: 6161
eureka:
  instance:
    hostname: peer1
  client:
    service-url:
      # 集群环境中，配置其它的eureka节点地址
      defaultZone: http://peer2:6162/eureka/

application-peer2.yml配置如下：
server:
  port: 6162
eureka:
  instance:
    hostname: peer2
  client:
    service-url:
      # 集群环境中，配置其它的eureka节点地址
      defaultZone: http://peer1:6161/eureka/

b) 设置启动参数
在Edit Configuration...中，创建Eureka01Application-6161，在Configuration选项卡Environment一栏中，设置Program arguments为 --spring.profiles.active=peer1。
创建Eureka01Application-6162，在Configuration选项卡Environment一栏中，设置Program arguments为 --spring.profiles.active=peer2。

或者通过命令行运行项目: java -jar xxx.jar --spring.profiles.active=peer2

c) 配置hosts文件
127.0.0.1 peer1
127.0.0.1 peer2
127.0.0.1 peer3

d) 测试
启动Eureka01Application-6161,Eureka01Application-6162,user,product
分别访问
http://localhost:6161
http://localhost:6162
观察DS replicas一栏，出现另外一个服务副本，这里是正常的。
观察registered-replicas，出现除当前节点的其它服务。
观察unavailable-replicas，为空正常。
观察available-replicas，出现除当前节点的其它服务。

调用user模块接口
http://localhost:17656/test/test1
返回结果正常。此时停掉Eureka01Application-6161服务，再次访问，结果依然正常。

2.2.2 创建两个不同的项目
略

2.2 将eureka节点独立部署
a) 本案例使用eureka02,eureka03项目。
b) 分别将eureka02部署到虚拟机131机器上，并启动。将eureka03部署到虚拟机135机器上，并启动。
c) 测试
分别访问地址观察结果
http://192.168.6.131:17651
http://192.168.6.135:17652
观察DS replicas, registered-replicas, unavailable-replicas, available-replicas

// 源码
https://www.jianshu.com/p/8b69f97b0fdc
// 在线扩充eureka节点
https://www.jianshu.com/p/fbe5574ba6e8