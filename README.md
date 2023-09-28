# security-keeper

## 特点

* 基于oauth的单点登录
* 用户信息管理：供应商->租户->用户三层体系
* 访问权限管理

## 单点登录

### 登录时序图

![login](https://github.com/frankcl/security-keeper/blob/main/images/sso_login.png)

### 注销时序图

![logout](https://github.com/frankcl/security-keeper/blob/main/images/sso_logout.png)

### 如何使用

#### Maven引入工件

```xml
<dependency>
    <groupId>xin.manong</groupId>
    <artifactId>security-keeper-sso-client</artifactId>
    <version>${security-keeper.version}</version>
</dependency>
```

#### 启动单点登录拦截

* 方式1：启动单点登录servlet filter
  * 应用入口增加注解：xin.manong.security.keeper.sso.client.annotation.EnableSecurityFilter
  ```java
  @EnableSecurityFilter
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
  app:
  security:
    sso:
      appId: xxx                            #应用ID
      appSecret: xxxxxx                     #应用秘钥
      serverURL: http://security-keeper/    #单点登录服务地址
      excludePatterns:                      #拦截排除URL列表
        - /login
        - /favicon.ico
  ```
  
* 方式2：启动单点登录spring拦截器
  * 应用入口增加注解：xin.manong.security.keeper.sso.client.annotation.EnableSecurityInterceptor
  ```java
  @EnableSecurityInterceptor
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
    private SecurityInterceptor securityInterceptor;
  
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityInterceptor).addPathPatterns("/**").
            excludePathPatterns("/login", "/favicon.ico");
    }
  }
  ```
  * application.yml增加配置信息
  ```yaml
  app:
  security:
    sso:
      appId: xxx                            #应用ID
      appSecret: xxxxxx                     #应用秘钥
      serverURL: http://security-keeper/    #单点登录服务地址
  ```

