# 项目进度
1. 登录注册模块（认证模块douyin_duth）
   1. 手机验证码发送 （问题：云平台短信网站需要备案）（√）
   2. 前后端分离项目，跳转需要跨域                 (√)
   3. 验证码登录注册  （UserDetailsSer`vice默认是使用用户名+密码进行验证，这里对该接口进行了魔改） (√)
   4. Spring Security认证 (目前不考虑用户权限问题，所有用户都是普通用户)`             （√）
   5. 第一次登录完成前后端交互。     （√）
   6. Spring Security 认证过滤链，实现后续登录无需在通过手机号验证码登录。   （√）
   7. 登出功能          (√)
   8. SpringSecurity异常处理   (√)
   9. 刷新Jwt有效时间(有效时间48小时，再次访问时有效时间小于24小时时重置为48小时。长时间不登录，Jwt失效)    (√)
   10. 多种验证方式下，如何配置多个AuthenticationManager (√)
   11. 用户名+密码+图形验证码登录  (√)
   13. TODO: 基于Oauth2，调用QQ微信等接口进行认证登录。 (问题：网站要备案,且需要部署到云服务器上（有IP地址）)。
   14. gateway鉴权（白名单，校验jwt合法性）  (√)
2. 将项目由单体项目转换为微服务架构
   1. douyin_common: 服务注册与发现 (√)
   2. douyin_gateway: 网关路由 (√)
   3. douyin_auth: 用户登录授权服务 (√)
   4. Nacos配置 (√)
   5. gateway配置 (√)
   6. 新增了一台云服务器，将Redis,minIO集群化。减少nacos的内存占用设置，集群化
3. 视频发布模块 
   1. 将文件写入到minIO (√)
   2. 完善文件属性，写入到数据库中 (√)
   3. 将文件ID由MD5转换为雪花算法 (√)
   4. 文件切片上传 (√)
   5. 根据MD5码判断文件是否已经上传过（秒传）（√）
   6. 服务器的响应 (√)
   7. redis缓存记录 (√)
   8. 文件的合并 (√)
   9. mysql记录 (√)
   10. 补充每个步骤的日志 (√)
   11. 考虑数据上传IO成功，但未保存到数据库的情况。
   12. 视频并发下载 
   13. 消息队列（预下载）
   14. 分布式文件系统




# 功能列表

1. 认证模块（douyin_auth）
   - 手机号注册/登录 (√)
   - 用户名+密码+验证码验证登录 (√)
   - TODO: 第三方接口登录 (等网站上线后在做)
2. 视频发布模块 (douyin_publish)`
   - 提供视频上传功能 (√)
   - 视频存储 （IO完成，数据库TODO）
   - 将视频发送至vedio消息队列，用于后续处理
   - 熔断降级
3. 视频点赞模块 (douyin_like)
   - 点赞，取消点赞
   - 喜欢列表
   - 消息队列
   - 推荐算法
4. 视频评论模块 (douyin_comment)
   - 添加评论
   - 删除评论
   - 评论列表
   - 评论数量
   - 推荐系统
   - 控评
5. 视频流模块 (douyin_feed)
   - 视频下载（ftp）
   - 视频缓存
   - 推荐算法
6. 相关用户模块（douyin_relation）    
   - 关注和取消关注的动作，消息队列
   - 关注列表 
   - 粉丝列表 
   - 附近的人 
   - 用户统计 (UV用HyperLogLog)
7. 用户聊天模块(douyin_message)（高可用）
   - 发送、接收消息
   - 聊天记录
   - 消息队列
8. 视频模块（要关注多维度视屏流，编解码，带宽，时延，画质，RTC）
    - 视频发布
    - 视频获取（Feed流）
    - 视频存储
    - 视频缓存
    - 视频点赞、收藏
    - 视频评论
    - ...
9. 高并发弹幕
10. 红包
11. 商城
12. AI
13. ...

# 小细节TODO
1. 连接池换成druid
2. 查询分页
3. 错误页面需要跳转到统一页面

# 碰到的问题
1. 认证模块
   1. WebSecurityConfigurerAdapter从 5.7.0-M2 起已经弃用，推荐使用基于组件的 security 配置 (为了组件化)。本项目采用了最新的组件
   2. 使用手机号+验证码登录会存在一个问题：手机号和验证码必须存放在UserDetails中，但是验证码存放在数据库中有点浪费。此外，authenticate中存入的原始值，UserDetails中的是加密值
   3. 自定义过滤链，在继承OncePerRequestFilter类时，出现了request 自动转换为org.springframework.security.web.header.HeaderWriterFilter$HeaderWriterRequest的BUG。要统一JwtAuthenticationTokenFilter文件和TokenService文件HttpServletRequest的包文件（jakarta.servlet.http.HttpServletRequest和javax.servlet.http.HttpServletRequest;javax.servlet在2018年之后就没有更新了。新版的Spring都用的是jakarta）
   4. 与后端认证对应的，在前端需要通过守护路由对页面访问进行限制，且在访问后端接口时，需要在请求头中携带token
   5. JWT是无状态的，不需要存储到Redis里面，如果存在了Redis体现不出JWT无状态的特性。但是如果不存储到Redis里面，后续在进行doFilterInternal时需要从mysql中读取用户数据。
   6. TODO(检测用户异常): 由于JWT的无状态性，当发现用户异常时需要采取措施。但这里一般是通过客户端的Https协议保障，为了进一步防止该情况的话，可以通过手机验证码进行二次验证。
   7. 多种验证方式下，如何配置多个AuthenticationManager？AuthenticationManager的实现ProviderManager管理了众多的AuthenticationProvider，每一个AuthenticationProvider都支持特定类型的Authentication。将不同的认证方式分别交给不同的每一个AuthenticationProvider都支持特定类型的Authentication就可以了。只要用一个AuthenticationProvider认证通过，就算认证通过了。
2. 微服务架构
   1. 微服务间的数据库如何设计。
   2. nacos2.x 新增了gRPC 的通信方式(用于客户端向服务端发起连接请求)，新增了两个端口。这两个端口在nacos原先的端口上(默认8848)，进行一定偏移量自动生成
   3. 转换成微服务架构后，前端无法正常调用接口了。 排查：
      - 服务命名(uri解析)不支持下划线. 
      - 要想使用gateway进行请求转发，所有的微服务实例与gateway服务实例在nacos中必须是同一个namesapce和同一个group. 
      - pring Cloud 2020版本以后，默认移除了对Netflix的依赖，其中就包括Ribbon. 因此需要额外导入spring-cloud-loadbalancer依赖。
      - Postman可以正常调用后端接口，但是前端由于出现多个’Access-Control-Allow-Origin’ CORS头，会导致前端无法正常访问后端接口。解决方案：https://blog.csdn.net/youanyyou/article/details/127543821
3. 视频发布模块
   1. 不同服务间的数据库要独立，外键采用逻辑外键。
   2. 非事务方法调用同类的一个事务方法，事务无法控制：在使用@Transacational的时候，Spring Framework会默认使用AOP代理。因此，代码运行时会生成一个代理对象，且是通过代理对象调用目标方法.方法：1. @Autowired直接注入service，通过cglib动态代理实现.(Spring Boot2.6以后已禁用循环引用) 2. 通过Spring AOP的方式调用，但需要配置exposeProxy=true。
   3. RequestParam一般用于name-valueString类型的请求域，RequestPart用于复杂的请求域.使用@RequestBody接收对象，所对应的content-type:application/json。后端接收multipart/form-data时，可以用VO接收，且不带@RequestXXX注解
   4. Redis作为缓存，要在分片数据合并并上传minio后，才能将value设置为1。分片数量齐了，但是还没有合并也是0.
   5. 将视频写入数据库操作时导致nacos失效。通过检查服务器docker日志，发现是内存溢出。
   6. 为了确保一致性，要先保存mysql,在保存redis。

4. 