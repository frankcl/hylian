# Hylian

* 产品地址：https://hylian.manong.xin

## 特点

* 基于oauth的单点登录
* 用户信息管理：租户->用户两层体系
* ACL权限管理

## 单点登录

### 登录时序图

![login](https://github.com/frankcl/hylian/blob/main/hylian-client/images/sso_login.png)

### 注销时序图

![logout](https://github.com/frankcl/hylian/blob/main/hylian-client/images/sso_logout.png)

### 如何使用

#### Maven引入工件

```xml
<dependency>
    <groupId>xin.manong</groupId>
    <artifactId>hylian-client</artifactId>
    <version>${hylian.version}</version>
</dependency>
```

#### 启动单点登录拦截

* 方式1：启动单点登录servlet filter
  * 应用入口增加注解：xin.manong.hylian.client.annotation.EnableHylianGuard
  ```java
  @EnableHylianGuard
  public class Application {

    /**
     * 应用入口
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
  }
  ```
  * application.yml增加配置信息
  ```yaml
  hylian:
    client:
      appId: xxx                            #应用ID
      appSecret: xxxxxx                     #应用秘钥
      serverURL: http://hylian-server/      #单点登录服务地址
    filter:
      filterOrder: 1                        #过滤器顺序，默认-1000
      includePatterns:                      #拦截URL列表，默认拦截/*
        - /*
      excludePatterns:                      #拦截排除URL列表
        - /xxx
        - /favicon.ico
  ```
  
* 方式2：启动单点登录spring拦截器
  * 应用入口增加注解：xin.manong.hylian.client.annotation.EnableHylianInterceptor
  ```java
  @EnableHylianInterceptor
  public class Application {

    /**
     * 应用入口
     *
     * @param args 参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
  }
  ```
  * 编写spring MVC配置，引入拦截器
  ```java
  @Configuration
  public class InterceptorConfig implements WebMvcConfigurer {
    @Resource
    private HylianInterceptor interceptor;
  
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor).addPathPatterns("/**").
            excludePathPatterns("/login", "/favicon.ico");
    }
  }
  ```
  * application.yml增加配置信息
  ```yaml
  hylian:
    client:
      appId: xxx                            #应用ID
      appSecret: xxxxxx                     #应用秘钥
      serverURL: http://hylian-server/      #单点登录服务地址
  ```

