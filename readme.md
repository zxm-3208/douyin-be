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
   15. 使用ThreadLocal保存用户信息
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
   11. 考虑数据上传IO成功，但未保存到数据库的情况 (√)
   12. 视频分片多线程下载 (√)   (TODO: 断点续传，先做个缓存，然后再接入消息队列)
   13. 通过后端生成外链实现下载 (√)
   14. 通过javacv实现上传视频后，自动保存封面缩略图 (√)  (TODO: 后续要把这个功能放到视频处理模块)
   15. 发布信息编辑与保存 (√)
   16. TODO: 视频Tag,作者,状态等零散的数据更新到数据库
   17. 获取视频列表  (√)
   18. 发布视频的时候，用推模式在Redis中进行缓存,视频列表读取缓存 (√)
   19. 点击视频列表文件后跳转到播放 (√)
   20. 视屏发布草稿箱
   21. 视频整体多线程下载 
   22. 消息队列（预下载）
   23. 分布式文件系统
4. 视频流模块
   1. 根据(数据库中所有)mediaId一次性(从Redis中(如果没有就缓存到Redis中))获取全部media_url    (√)
   2. 在视频推荐栏中，先用推模式获取视频流demo   (根据视频ID查)        (√)
   3. 根据视频列表点击数据查询 (按照用户视频列表查询，时间排序)         (√)
   4. 用户视频列表,分页 (由于前端没做分页拉取，因此后端还是一次性全读)  (√)
   5. 用户视频播放列表，传入mediaList，分页,前端滑动到边界时，查询新的分页。     (√) 
   6. 首页默认播放列表    (√)
   7. 根据视频状态获取feed流 (√)
   8. 预加载 (主要利用CDN，相关产品有腾讯云的edgeOne, 由于收费，暂时不使用)
   9. 获取下一个分页    (前端还未实现)  
   10. 向前端传递视频参数  (√)
   11. 关注 (推拉等模式)    (关注列表，投喂，消息队列)
   12. 推荐
5. 用户模块
   1. 点赞，取消点赞  (√)
   2. 用户点赞列表    (√)
   3. 关注功能        (√)
   4. 个人信息编辑     (√)
   5. 关注者列表 (√)
   6. 粉丝列表 (√)
   7. 互关判定  (√)
   8. 用户界面访问，以及关注 (√)
   9. Redis缓存更新时清空，第一次查询时从mysql中获取  (√)
6. 即时通讯模块
   1. 基于Google Protobuf 对结构化数据进行序列化 (√)
   2. 使用单例模式创建SessionManger (√)
   3. 绑定user、session、channel, 使用原子变量 (√)
   4. 使用Redis对Session和User进行缓存    (√)
   5. 创建client以及实现节点常用的curd   (√)
   6. 使用单例模式基于ZK对ImNode的数据的进行curd (√)
   7. 长连接心跳机制 (√)
   8. 使用ZooKeeper对节点进行管理和路由 (√)
   9. 分布式计数器 (√)
   10. 单例模式都改成双重检查加锁以保证线程安全且高效
   11. 节点通知 (√)
   12. 客户端session (√)
   13. 建立一个chat网关来统一处理IM业务
7. 容器化
   1. 开发环境和生产环境配置文件区分
   2. 编排




# 功能列表
1. 认证模块（douyin_auth） (核心：验证)
   - 手机号注册/登录 (√)
   - 用户名+密码+验证码验证登录 (√)
   - TODO: 第三方接口登录 (等网站上线后在做)
2. 视频发布模块 (douyin_publish)` (核心：上传和下载)
   - 提供视频上传功能 (√)
   - 视频存储   (√)
   - 编码
   - 将视频发送至vedio消息队列，用于后续处理
   - 熔断降级
3. 视频流模块 (douyin_feed) (核心：Feed流)
   - 作品列表  (√)
   - 默认视频列表 (√)
   - 草稿箱
   - 推荐算法
4. 视频点赞模块 (douyin_like)
   - 点赞，取消点赞  (√)
   - 喜欢列表   (√)
   - 消息队列
   - 推荐算法
5. 视频评论模块 (douyin_comment)
   - 添加评论
   - 删除评论
   - 评论列表
   - 评论数量
   - 推荐系统
   - 控评
6. 相关用户模块（douyin_relation）    
   - 关注和取消关注的动作，消息队列
   - 关注列表  (√)
   - 粉丝列表  (√)
   - 附近的人 
   - 用户统计 (UV用HyperLogLog)
7. 用户聊天模块(douyin_message)（高可用）
   - 发送、接收消息
   - 聊天记录
   - 消息队列
8. 高并发弹幕
9. 红包
10. 商城
11. AI
12. ...

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
   8. Spring cloud 如何保证使用Thredlocal保存用户信息，不会产生内存泄漏。(gateway库与MVC库冲突，无法使用拦截器)(不同微服务之间由于线程不同，每个微服务有自己的线程池)
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
   7. new Thread不在spring容器中,因此Runnable不受Spring容器管理，需要手动注入bean. TODO:(Bug)上下文工具类写再common服务中，同样无法再Runnable中注入，猜测可能是执行顺序问题。可以将工具类写在当前服务中。
   8. TODO: 多线程并发的八股不够熟悉，需要深化
4. 视频播放模块
   1. TODO: 判断播放列表中应该包含哪些短视频是一大问题。
   2. TODO: 播放列表太长，应该分页
   3. TODO: 什么时候加载下一页播放列表
   4. 获取一个用户发布的视频列表，可以用Redis存储最新视频，如果当需要访问的视频不在Redis中时，在从Mysql中读取。
   5. 用户作品列表中的数据用Redis存储有效期是否需要设置为永久。
   5. BUG: minio上有，数据库上没有的时候不会获取封面。
5. 用户模块
   1. 一个服务模块如何连接多个数据库。 需要对不同数据源进行配置
   2. TODO: 部分功能需要联结多个数据库进行查询，后续需要进行优化
   3. mybatis xml文件中代表不同意义的字段名不能重复，否则会出现混乱
   4. 关注时要特判不能关注自己
   5. 关注、取关动作在用户页面跳转后的关注、粉丝列表中需要灵活变化，需要分清楚本地用户id，目标用户id，目标用户所关注/被关注的用户Id这三者的逻辑关系
6. 即时通讯模块
   1. 用到设计模式：单例模式，建造者模式，代理模式，工厂模式
   2. zookeeper 安全认证
   3. zookeeper 与 etcd对比， etcd优于zookeeper,吸取了zookeeper的经验，更有前瞻性。但是相关教程较少，先用经典的zookeeper，后续用etcd重构。
   4. 为什么ImWorker使用单例模式。