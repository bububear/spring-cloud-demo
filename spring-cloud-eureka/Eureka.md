# Spring Cloud Eureka

## 目录
1. 前言
2. 构建服务注册中心
3. 服务注册与发现
4. Eureka的基础架构
5. Eureka的服务治理机制
6. Eureka的配置

## 前言
### 服务治理
&emsp;随着微服务应用的不断增加，静态配置会越来越难以维护，并且随着业务的不断发展，集群规模、服务位置、服务命名都会发生变化，手动维护的方式极易发生错误或是命名冲突问题。因此需要服务治理框架对微服务实例进行管理，服务治理是微服务架构中最核心的功能和模块，主要用来各个微服务实例的自动化注册和发现。
#### 服务注册
&emsp;在服务治理框架中，通常都会有一个**服务注册中心**。

&emsp;**每一个微服务实例向注册中心登记自己提供的服务，将主机、端口号、版本号、通信协议等一些信息告知注册中心。**

&emsp;**注册中心按服务名分类组织服务清单。**

&emsp;**服务注册中心需要以心跳的方式监测服务清单中的服务是否可用，如果不可用，需要将不可用的服务实例进行剔除。**
#### 服务发现
&emsp;**服务间的调用通过向服务名发起请求调用实现。** 服务调用方在调用提供方的接口时，并不知道提供方的具体地址。

&emsp;**服务调用方需要从注册中心获取所有服务的实例清单，才可以实现对具体服务实例的访问。**

&emsp;**服务调用方在发起调用时，会以某种策略取出一个具体的服务实例进行服务调用(客户端负载均衡)。**

&emsp;**在实际的环境中，为了提供性能，并不会采用每次都向服务注册中心获取服务的方式进行服务的调用，并且不同的应用场景在缓存和服务剔除等机制上可以采用不同的实现策略。**

#### Netflix Eureka
&emsp;Spring Cloud Eureka采用Netflix Eureka来实现服务注册与发现，包含**客户端**和**服务端**组件。
##### Eureka服务端(服务注册中心)
&emsp;**支持高可用配置。**

&emsp;**依托于强一致性提供良好的服务实例可用性。**

&emsp;**服务注册中心之间可以通过异步模式互相复制各自的状态。**
##### Eureka客户端
&emsp;**主要用于服务的注册和发现。**

&emsp;**客户端可以通过注解和参数配置的方式实现注册与发现。**

&emsp;**Eureka客户端向注册中心注册自身提供的服务并周期性地发送心跳来更新它的服务租约。**

&emsp;**Eureka客户端从服务端查询当前注册的服务信息并把它们缓存到本地并周期性的刷新服务状态。**

## 构建服务注册中心
### 构建注册中心(位于spring-cloud-eureka-server的Module下)
```java
package cn.sh.eureka.server; //代码位于该包下
```
1.准备pom.xml
```
    <!--导入Spring Boot Starter-->
    <!--
        父项目parent配置指定为spring-boot-starter-parent的2.0.3.RELEASE版本,
        该父项目中定义了Spring Boot版本的基础依赖以及一些默认配置内容。
        比如，配置文件application.properties的位置等。
    -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.3.RELEASE</version>
    </parent>        
    <dependencies>
        <!--导入Eureka服务端(注册中心)依赖jar包-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka-server</artifactId>
            <version>1.2.7.RELEASE</version>
        </dependency>
    </dependencies>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Finchley.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```
2.准备配置文件application.properties
```properties
#端口号
server.port=9000

eureka.instance.hostname=localhost

#禁止注册中心注册自己
eureka.client.register-with-eureka=false

#禁止注册中心搜索服务
eureka.client.fetch-registry=false

eureka.client.serviceUrl.defaultZone=http://${eureka.instance.hostname}:${server.port}/eureka/
```
3.编写注册中心代码(@EnableEurekaServer)
```java
package cn.sh.eureka.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author az6343
 * @EnableEurekaServer 启动一个注册中心
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaServer {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServer.class, args);
    }
}
```

### 构建微服务应用(服务提供者)
1.准备pom.xml
```
    <!--导入Spring Boot Starter-->
    <!--
        父项目parent配置指定为spring-boot-starter-parent的2.0.3.RELEASE版本,
        该父项目中定义了Spring Boot版本的基础依赖以及一些默认配置内容。
        比如，配置文件application.properties的位置等。
    -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.0.3.RELEASE</version>
    </parent>
    <dependencies>
        <!--增加Web支持-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!--增加Eureka客户端支持-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
            <version>1.2.5.RELEASE</version>
        </dependency>
    </dependencies>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Finchley.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```
2.准备配置文件application.properties
```properties
#端口号
server.port=8000

#服务名称
spring.application.name=produce-service

#服务注册中心地址
eureka.client.serviceUrl.defaultZone=http://localhost:9000/eureka/
```
3.编写produce-service服务的代码

代码位于spring-cloud-eureka-client模块下
```java
package cn.sh.eureka.produce; //代码包位置
```

### 高可用注册中心搭建
&emsp;Eureka Server的高可用实际上就是讲自己作为服务向其他服务注册中心注册自己，这样可以形成一组互相注册的服务注册中心，以实现服务清单的互相同步，达到高可用的效果。

&emsp;**相关实现在spring-cloud-eureka-server模块下，注意在host配置文件中配置peer1和peer2的转换**

&emsp;**将代码通过maven编译成jar包**

&emsp;通过java命令启动两个注册中心
```jshelllanguage
java -jar spring-cloud-eureka-server-1.0.jar --spring.profiles.active=peer1
java -jar spring-cloud-eureka-server-1.0.jar --spring.profiles.active=peer2
```
&emsp;**启动之后，会发现在注册中心peer1的面板上的available-replicas中出现http://peer2:9002/eureka/，同样，在注册中心peer2面板上的available-replicas中出现http://peer1:9001/eureka/**

eureka.instance.prefer-ip-address=true, 使用IP地址的方式指定注册中心的地址,默认false，以主机名定义注册中心地址

### 服务发现与消费
&emsp;服务消费者的主要目标是发现和消费服务。其中**服务发现由Eureka客户端完成**,**服务消费由Ribbon完成**。
#### Ribbon简单介绍
&emsp;Ribbon是一个基于HTTP和TCP的客户端负载均衡器，它可以通过在客户端中配置的ribbonServerList服务实例列表去轮询访问以达到负载均衡的作用。

&emsp;Ribbon与Eureka结合使用时，ribbonServerList服务实例列表会被DiscoveryEnabledNIWSServerList重写，扩展成从Eureka注册中心中获取服务列表。

&emsp;Ribbon与Eureka结合使用时，采用NIWSDiscoveryPing取代IPing，它将职责委托给Eureka来确定服务实例是否启动。

#### 构建消费者
1.pom.xml

&emsp;增加对Ribbon的支持
```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-ribbon</artifactId>
        <version>1.2.7.RELEASE</version>
    </dependency>
```
2.相关代码在spring-cloud-eureka-consumer模块下


## Eureka详解
### 基础结构
1. 服务注册中心：Eureka服务端，提供服务注册和发现的功能
2. 服务提供者：提供服务的应用，将自己提供的服务注册到Eureka，供其他应用发现
3. 服务消费者：消费者从注册中心发现服务列表，然后调用对应的服务(Ribbon或Feign)

**备注: 一般一个应用既是服务提供者也是服务消费者。**
### 服务治理机制

&emsp;Eureka服务体系图，**后期补充**

#### 服务提供者
##### 服务注册
&emsp;服务提供者会以Rest请求的方式注册到注册中心上，在请求过程中会携带自身的一些元数据信息。注册中心在收到请求后，会将元数据信息保存到一个双层Map结构中,外层的key是服务名称，内层的key是具体的服务实例名称。

&emsp;eureka.client.register-with-eureka，如果该参数的值等于false，不会进行注册。

##### 服务同步
&emsp;如果两个服务注册在两个不同的注册中心上，两个注册中心互相注册成为服务(集群)，此时，当服务提供者向其中一个注册中心发起请求时，该注册中心会将请求准发给集群中的其他注册中心，从而实现注册中心之间的服务同步。

&emsp;由于服务同步的存在，服务提供者的信息可以在任意一台注册中心上获取。

##### 服务续约
&emsp;在服务注册完成之后，服务提供者需要维护一个心跳来告知注册中心服务实例处于正常运行状态中，防止注册中心将正常的服务实例剔除出注册中心。上述操作就成为服务续约。

属性 | 含义
---|---
eureka.instance.lease-renewal-interval-in-seconds | 服务续约任务调用的间隔时间，默认时间30s
eureka.instance.lease-expiration-duration-in-seconds | 服务失效时间（表示注册中心至上一次收到客户端的心跳之后，等待下一次心跳的超时时间，在这个时间内若没收到下一次心跳，则将移除该客户端实例），默认90s

#### 服务消费者
##### 获取服务
&emsp;启动服务消费者时，服务消费者会向注册中心发起一个Rest请求，来获取注册中心维护的服务实例清单。但是为了提高性能，注册中心会维护一份只读的服务清单返回给客户端，该缓存的服务清单会每隔30s刷新一次。

&emsp;eureka.client.fetch-registry，如果该参数被设置为false，无法向注册中心获取服务清单。

&emsp;eureka.client.registry-fetch-interval-seconds，缓存清单的刷新时间，默认30s。

##### 服务调用
&emsp;服务消费者获得服务清单后，可以根据服务名获取具体服务实例列表（元数据信息），根据自己的策略选择具体的服务实例进行调用。

&emsp;Eureka有Region和Zone的概念，一个Region中会有多个Zone，每个客户端都需要注册到一个Zone中，所以客户端对应一个Region和一个Zone。**在服务进行调用时，优先访问同一个Zone中的服务提供方，若访问不到，再访问其他Zone。**

##### 服务下线
&emsp;当服务实例**正常关闭**时，服务实例会发送一个服务下线的Rest请求给注册中心。注册中心在收到请求后，会将该服务实例的状态置为DOWN，并且将下线时间广播出去。

#### 服务注册中心
##### 失效剔除
&emsp;当服务实例未正常下线时(内存溢出、网络故障)，服务注册中心未能收到服务下线的Rest请求。注册中心在启动时会创建一个定时任务，默认每隔一段时间(60s)将当前清单中超时(服务失效时间,默认90s)没有续约的服务进行剔除。

##### 自我保护
&emsp;注册中心在运行期间，会统计心跳失败比例在15分钟内是否低于85%，如果出现低于的情况，注册中心会将当前服务实例的注册信息保护起来，让这些实例不会过期。**但是，在保护期时间内，如果实例出现问题，那么服务调用者很容易拿到该实例调用失败，所以服务调用者必须要有容错机制（请求重试、断路由器等）。**

&emsp;eureka.server.enable-self-preservation，如果该值设置为false，则不启用自我保护机制，默认值为true

## 源码分析
&emsp;分析源码，可以以Eureka客户端和Eureka服务端作为切入点。
### Eureka客户端
在应用获取服务列表和向注册中心注册成为服务时只做了两件事：
1. 在启动类上使用@EnableEurekaClient或者@EnableDiscoveryClient注解
2. 在application.properties中添加eureka.client.serviceUrl.defaultZone

#### @EurekaDiscoveryClient
&emsp;该注解的主要作用是用来开启一个DiscoveryClient的实例。

#### org.springframework.cloud.client.discovery.DiscoveryClient类

&emsp;**类图以后补充**

类 | 作用
--- | ---
org.springframework.cloud.client.discovery.DiscoveryClient | Spring Cloud的接口，定义了发现服务的常用抽象方法，这样做的好处是可以**屏蔽服务治理的细节，可以方便的切换不同的服务治理框架，不需要改动程序代码，只需要添加一些针对服务治理框架的配置**
com.netflix.appinfo.InstanceInfo.LookupService | 定义了Eureka发现服务的抽象方法
com.netflix.discovery.EurekaClient | 定义了Eureka发现服务的抽象方法，继承LookupService
org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient | Spring中的DiscoveryClient接口的实现，对Eureka发现服务的封装，实现EurekaClient接口，Eureka接口继承LookupService接口
com.netflix.discovery.DiscoveryClient | 实现EurekaClient接口，真正的服务发现实现类

#### com.netflix.discovery.DiscoveryClient类
通过头部的注释信息，我们可以得到以下信息：

1.DiscoveryClient类主要用于帮助客户端和注册中心互相协作。

2.Eureka客户端主要负责的任务：
1. 向注册中心注册服务实例
2. 向注册中心进行服务租约
3. 当服务关闭期间，向注册中心取消租约
4. 查询注册中心的服务实例列表

3.Eureka客户端需要配置一个注册中心的URL列表

#### Eureka客户端注册中心URL列表进行配置
&emsp;关键类：**com.netflix.discovery.endpoint.EndpointUtils**

&emsp;关键方法：**Map<String, List<String>> getServiceUrlsMapFromConfig(EurekaClientConfig clientConfig, String instanceZone, boolean preferSameZone)**

```java
    /**
     * Get the list of all eureka service urls from properties file for the eureka client to talk to.
     *
     * @param clientConfig the clientConfig to use
     * @param instanceZone The zone in which the client resides
     * @param preferSameZone true if we have to prefer the same zone as the client, false otherwise
     * @return an (ordered) map of zone -> list of urls mappings, with the preferred zone first in iteration order
     */
    public static Map<String, List<String>> getServiceUrlsMapFromConfig(EurekaClientConfig clientConfig, String instanceZone, boolean preferSameZone) {
        Map<String, List<String>> orderedUrls = new LinkedHashMap<>();
        String region = getRegion(clientConfig);
        String[] availZones = clientConfig.getAvailabilityZones(clientConfig.getRegion());
        if (availZones == null || availZones.length == 0) {
            availZones = new String[1];
            availZones[0] = DEFAULT_ZONE;
        }
        logger.debug("The availability zone for the given region {} are {}", region, availZones);
        int myZoneOffset = getZoneOffset(instanceZone, preferSameZone, availZones);

        String zone = availZones[myZoneOffset];
        List<String> serviceUrls = clientConfig.getEurekaServerServiceUrls(zone);
        if (serviceUrls != null) {
            orderedUrls.put(zone, serviceUrls);
        }
        int currentOffset = myZoneOffset == (availZones.length - 1) ? 0 : (myZoneOffset + 1);
        while (currentOffset != myZoneOffset) {
            zone = availZones[currentOffset];
            serviceUrls = clientConfig.getEurekaServerServiceUrls(zone);
            if (serviceUrls != null) {
                orderedUrls.put(zone, serviceUrls);
            }
            if (currentOffset == (availZones.length - 1)) {
                currentOffset = 0;
            } else {
                currentOffset++;
            }
        }

        if (orderedUrls.size() < 1) {
            throw new IllegalArgumentException("DiscoveryClient: invalid serviceUrl specified!");
        }
        return orderedUrls;
    }
```
1.加载Region、Zone

&emsp;通过getRegion函数，从配置文件中读取一个Region返回，所以一个微服务应用只能属于一个Region，如果不进行配置，默认值default，**eureka.client.region该属性可以配置Region**

&emsp;通过getAvailabilityZones函数，如果没有为Region配置Zone，默认值是defaultZone，因此注册中心URL列表的默认配置参数为eureka.client.serviceUrl.defaultZone。**eureka.client.availability-zone.<regionName>可以配置Region下面的Zone，Zone的配置有多个（用,分隔）。**

2.加载serviceUrls

&emsp;按照一定的方法以此加载每个Zone中的urls，存放在一个Map<String, List<String>>中。

&emsp;当使用Ribbon来调用服务时，Ribbon的默认策略是优先访问和客户端处于同一个Zone的微服务实例。

&emsp;在获取到客户端配置的serviceUrls之后，就可以进行服务的注册，详情请看下面。

#### 向注册中心注册服务（服务注册）
&emsp;关键类：com.netflix.discovery.DiscoveryClient

&emsp;关键方法：void initScheduledTasks()，DiscoveryClient的构造函数会对此方法进行调用

&emsp;该方法主要用来启用定时任务，主要包括获取服务Task、服务注册、服务续约(心跳)，本次先着重看服务注册的逻辑

```java
    /**
     * Initializes all scheduled tasks.
     */
    private void initScheduledTasks() {
        
        //此处是该方法的其他逻辑
        
        if (clientConfig.shouldRegisterWithEureka()) {
            int renewalIntervalInSecs = instanceInfo.getLeaseInfo().getRenewalIntervalInSecs();
            int expBackOffBound = clientConfig.getHeartbeatExecutorExponentialBackOffBound();
            logger.info("Starting heartbeat executor: " + "renew interval is: {}", renewalIntervalInSecs);

            // Heartbeat timer
            
            // InstanceInfo replicator
            instanceInfoReplicator = new InstanceInfoReplicator(
                    this,
                    instanceInfo,
                    clientConfig.getInstanceInfoReplicationIntervalSeconds(),
                    2); // burstSize

            statusChangeListener = new ApplicationInfoManager.StatusChangeListener() {
                @Override
                public String getId() {
                    return "statusChangeListener";
                }

                @Override
                public void notify(StatusChangeEvent statusChangeEvent) {
                    if (InstanceStatus.DOWN == statusChangeEvent.getStatus() ||
                            InstanceStatus.DOWN == statusChangeEvent.getPreviousStatus()) {
                        // log at warn level if DOWN was involved
                        logger.warn("Saw local status change event {}", statusChangeEvent);
                    } else {
                        logger.info("Saw local status change event {}", statusChangeEvent);
                    }
                    instanceInfoReplicator.onDemandUpdate();
                }
            };

            if (clientConfig.shouldOnDemandUpdateStatusChange()) {
                applicationInfoManager.registerStatusChangeListener(statusChangeListener);
            }

            instanceInfoReplicator.start(clientConfig.getInitialInstanceInfoReplicationIntervalSeconds());
        } else {
            logger.info("Not registering with Eureka server per configuration");
        }
    }
```
1.判断是否允许客户端向注册中心注册

&emsp;eureka.client.register-with-eureka 该参数要设置成true，默认值为true

&emsp;如果上述参数值为true，则执行注册任务

2.新建InstanceInfoReplicator，利用其来进行注册

&emsp;该类实现了Runnable接口，观察run方法，在进行注册时实际调用的是com.netflix.discovery.DiscoveryClient类的register()方法，下面是run方法的源码
```java
    public void run() {
        try {
            discoveryClient.refreshInstanceInfo();

            Long dirtyTimestamp = instanceInfo.isDirtyWithTime();
            if (dirtyTimestamp != null) {
                discoveryClient.register();
                instanceInfo.unsetIsDirty(dirtyTimestamp);
            }
        } catch (Throwable t) {
            logger.warn("There was a problem with the instance info replicator", t);
        } finally {
            Future next = scheduler.schedule(this, replicationIntervalSeconds, TimeUnit.SECONDS);
            scheduledPeriodicRef.set(next);
        }
    }
```

3.看一下DiscoveryClient.register()做了什么

&emsp;在该方法中，通过发送Rest请求进行注册操作，在发送请求时会传入一个InstanceInfo，该对象就是客户端的元数据信息。

#### 服务获取
&emsp;服务获取任务也是在initScheduledTasks方法中启动，由于客户端需要不断获取服务端维护的服务实例清单，因此该任务会以定时任务启动，在启动过程会首先获取配置文件中eureka.client.registry-fetch-interval-seconds参数配置的值(默认是30s),
然后根据配置的参数每30s（按照实际配置的值)获取一次服务。实际获取服务列表的时候也是发送Rest请求。
```java
    /**
     * Initializes all scheduled tasks.
     */
    private void initScheduledTasks() {
        if (clientConfig.shouldFetchRegistry()) {
            // registry cache refresh timer
            int registryFetchIntervalSeconds = clientConfig.getRegistryFetchIntervalSeconds();
            int expBackOffBound = clientConfig.getCacheRefreshExecutorExponentialBackOffBound();
            scheduler.schedule(
                    new TimedSupervisorTask(
                            "cacheRefresh",
                            scheduler,
                            cacheRefreshExecutor,
                            registryFetchIntervalSeconds,
                            TimeUnit.SECONDS,
                            expBackOffBound,
                            new CacheRefreshThread()
                    ),
                    registryFetchIntervalSeconds, TimeUnit.SECONDS);
        }
    }
```

#### 服务续约
&emsp;服务在注册到注册中心后，需要维持一个心跳去续约，防止被剔除，因此服务续约和服务注册是成对存在，在同一个if条件里。首先会获取配置文件参数中eureka.instance.lease-renewal-interval-in-seconds的值，该值默认30s，随后启动定时任务，每隔30s(根据实际配置的值)向注册中心发一次Rest请求，表明自己还活着。
```java
    /**
     * Initializes all scheduled tasks.
     */
    private void initScheduledTasks() {

        if (clientConfig.shouldRegisterWithEureka()) {
            int renewalIntervalInSecs = instanceInfo.getLeaseInfo().getRenewalIntervalInSecs();
            int expBackOffBound = clientConfig.getHeartbeatExecutorExponentialBackOffBound();
            logger.info("Starting heartbeat executor: " + "renew interval is: {}", renewalIntervalInSecs);
            // Heartbeat timer
            scheduler.schedule(
                    new TimedSupervisorTask(
                            "heartbeat",
                            scheduler,
                            heartbeatExecutor,
                            renewalIntervalInSecs,
                            TimeUnit.SECONDS,
                            expBackOffBound,
                            new HeartbeatThread()
                    ),
                    renewalIntervalInSecs, TimeUnit.SECONDS);
        }
    }
```

#### 服务注册中心处理
&emsp;注册中心处理Rest请求的类位于com.netflix.eureka.resources包下

&emsp;比如com.netflix.eureka.resources.ApplicationResource类中的addInstance()方法主要用来处理客户端的注册事件。

&emsp;注册中心在收到客户端发送注册请求以后，会首先对客户端的信息进行校验，校验过后会调用org.springframework.cloud.netflix.eureka.server.InstanceRegistry的register()方法来进行服务注册。

&emsp;register()方法会调用publishEvent()方法将服务注册的事件传播出去，紧接着调用父类com.netflix.eureka.registry.AbstractInstanceRegistry中的register()实现，该方法会将InstanceInfo中的元数据信息保存到一个ConcurrentHashMap中。

&emsp;该HashMap有两层数据结构，正如我们之前所说，第一层的Key存储服务名（InstanceInfo中的appName属性），第二层Key存储服务实例名称(InstanceInfo中的instanceId属性)

## 配置详解
Eureka客户端的配置主要有以下两个方面：
1. 服务注册相关的配置信息，包括注册中心的地址、服务获取的时间间隔、可用区域（Zone）等；
2. 服务实例相关的配置信息，包括服务实例的名称、IP地址、端口号、健康检查路径等。

&emsp;**客户端的配置类可以参考org.springframework.cloud.netflix.eureka.EurekaClientConfigBean**

&emsp;**服务端的配置类可以参考org.springframework.cloud.netflix.eureka.server.EurekaServerConfigBean**

### 服务注册相关的配置
&emsp;这些配置信息都以eureka.client作为前缀

#### 指定注册中心
&emsp;指定注册中心主要靠eureka.client.serviceUrl参数，该参数的配置值值存储在HashMap中，并且会有一组默认值，默认值的Key是defaultZone，value为http://localhost:8761/eureka/。

&emsp;当构建了高可用的服务注册中心集群时，参数的value可以配置多个注册中心(通过,分隔)。

&emsp;为了注册中心的安全考虑，需要为服务注册中心加入安全校验。因此客户端在配置注册中心serviceUrl中时需要加入相应的安全校验信息。比如http://<username>:<password>@localhost:9001/eureka/,其中username为用户名，password为密码。

#### 其他配置
参数名 | 说明 | 默认值
--- | --- | ---
enabled | 启用Eureka客户端 | true
registryFetchIntervalSeconds | 从服务注册中心获取注册信息(服务实例清单)的间隔时间，单位s | 30
instanceInfoReplicationIntervalSeconds | 更新实例信息的变化到Eureka服务端的间隔时间，单位s | 30
initialInstanceInfoReplicationIntervalSeconds | 最初更新实例信息到Eureka服务端的间隔时间，单位s | 40
eurekaServiceUrlPollIntervalSeconds | 轮询Eureka服务端地址更改的间隔时间，单位s。当我们与Spring Cloud Config配合，动态刷新Eureka的serviceUrl地址时需要关注该参数 | 300
eurekaServerReadTimeoutSeconds | 读取注册中心信息的超时时间，单位s | 8
eurekaServerConnectTimeoutSeconds | 连接注册中心的超时时间，单位s | 5
eurekaServerTotalConnections | 从Eureka客户端到所有Eureka服务端的连接总数 | 200
eurekaServerTotalConnectionsPerHost | 从Eureka客户端到每个Eureka服务端主机的连接总数 | 50
eurekaConnectionIdleTimeoutSeconds | Eureka服务端连接的空闲关闭时间 | 30
heartbeatExecutorThreadPoolSize | 心跳连接池的初始化线程数 | 2
heartbeatExecutorExponentialBackOffBound | 心跳超时重试延迟时间的最大乘数值 | 10
cacheRefreshExecutorThreadPoolSize | 缓存刷新线程池的初始化线程池数 | 2
cacheRefreshExecutorExponentialBackOffBound | 缓存刷新重试延迟时间的最大乘数值 | 10
useDnsForFetchingServiceUrls | 使用DNS来获取Eureka服务端的serviceUrl | false
registerWithEureka | 是否要将自身的实例信息注册到Eureka服务端 | true
preferSameZoneEureka | 是否偏好使用处于相同Zone的Eureka服务端 | true
filterOnlyUpInstances | 获取实例时是否过滤，仅保留UP状态的实例 | true
fetchRegistry | 是否从Eureka服务端获取注册信息 | true

### 服务实例类配置
&emsp;该配置可以参考org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean类，这些配置都以eureka.instance开头

#### 元数据
&emsp;在EurekaInstanceConfigBean类中有一大部分内容是对服务实例元数据的配置，它是Eureka客户端在向注册中心发送注册请求时，用来描述自身服务信息的对象。

&emsp;在使用Spring Cloud Eureka时，所有的配置信息都是通过EurekaInstanceConfigBean对象进行加载，但真正进行服务注册的时候，会包装成com.netflix.appinfo.InstanceInfo对象发送给Eureka服务端。

&emsp;在InstanceInfo类中，Map<String, String> metadata属性是自定义的元数据信息，其他的属性都是标准的元数据信息。

&emsp;在配置文件可以通过eureka.instance.\<properties>=\<value>的格式对标准化元数据直接进行配置，对于自定义元数据，可以通过eureka.instance.metedataMap.\<key>=\<value>的格式进行配置。

#### 实例名配置
&emsp;实例名，即InstanceInfo中的instanceId，它是区别同一服务中不同实例的唯一标识。

&emsp;在原生的Netflix Eureka中，实例名称采用主机名作为默认值，这样的弊端就是无法在同一主机上启动多个相同的服务实例。

&emsp;在Spring Cloud Eureka中，对实例名的默认值做了更合理的扩展，它采用了${spring.cloud.client.hostname}:${spring.application.name}:${spring.application.instance_id:${server.port}}

&emsp;实例名的命名规则，可以通过eureka.instance.instance-id进行配置，比如在客户端进行负载均衡调用时，需要启动多个端口进行调试，此时就可以修改eureka.instance.instance-id=${spring.application.name}:${random.int}

#### 端点配置
&emsp;在InstanceInfo中，可以看到一些URL的配置信息，如：homePageUrl、statusPageUrl、healthCheckUrl等，它分别代表了应用主页的URL、状态页的URL、健康检查的URL。

&emsp;状态页和健康检查的URL默认使用了spring-boot-actuator模块提供的/info和/health端点。

&emsp;为了服务的正常运作，必须确保/health端点是一个能够被注册中心可以访问的地址，否则注册中心不会根据应用的健康检查来更新状态（**只有eureka.client.healthcheck.enabled=true时才会以该端点对服务进行健康监测，否则会以客户端心跳的方式。**）。

&emsp;/info端点如果不正确，会导致在注册中心点击服务实例链接，会无法获取到服务实例提供的信息接口。

&emsp;在大多数情况下，并不需要修改URL的配置，但是不排除特殊情况需要对这些URL做配置。比如：为应用设置context-path，这时，所有spring-boot-actuator模块的监控端点都会增加一个前缀。示例配置如下：
```properties

server.servlet.context-path=/produce

eureka.instance.status-page-url-path=${server.servlet.context-path}/info

eureka.instance.health-check-url-path=${server.servlet.context-path}/health

```
&emsp;eureka.instance.status-page-url-path和eureka.instance.health-check-url-path两个参数均使用相对路径来配置。

&emsp;eureka.instance.status-page-url和eureka.instance.health-check-url两个配置参数使用绝对路径进行配置。对比上面可以发现，如果使用相对路径后面参数后缀都会跟着path

#### 健康检测
&emsp;默认情况下，Eureka中各个服务实例的健康检测并不是通过spring-cloud-actuator模块的/health节点，而是依靠客户端的心跳来保持服务的存活。

&emsp;默认的客户端心跳方式无法保证客户端提供正常的服务。比如微服务一般会有依赖的外部资源（如数据库、缓存、消息代理等），假如与这些外部资源无法联通，但是客户端心跳依旧存在，这就会导致调用出现问题。

&emsp;使用spring-boot-actuator模块的/health端点，后续会写个DEMO

#### 其他配置
参数 | 说明 | 默认值
--- | --- | ---
preferIpAddress | 是否优先以使用IP地址作为主机名的标识 | false
leaseRenewalIntervalInSeconds | 客户端向注册中心发送心跳的间隔时间,单位s | 30
leaseExpirationDurationInSeconds | 服务端在收到最后一次心跳后的等待时间上限，单位s。超过该时间会对服务进行剔除。| 90
nonSecurePort | 非安全的通信端口号 | 80
securePort | 安全的通信端口号 443
nonSecurePortEnabled | 是否启用非安全的通信端口号 | true
securePortEnabled | 是否启用安全的通信端口号 | true
appname | 服务名，默认会取spring.application.name的值 | unknown
hostname | 主机名 | 根据计算操作系统的主机名进行取值