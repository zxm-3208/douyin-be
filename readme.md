# 项目进度
1. 用户模块-登录注册
   1. 手机验证码发送 （问题：云平台短信网站需要备案）（√）
   2. 前后端分离项目，跳转需要跨域                 (√)
   3. 验证码登录注册  （UserDetailsSer`vice默认是使用用户名+密码进行验证，这里对该接口进行了魔改） (√)
   4. Spring Security认证 (目前不考虑用户权限问题，所有用户都是普通用户)`             （√）
   5. 第一次登录完成前后端交互。     （√）
   6. Spring Security 认证过滤链，实现后续登录无需在通过手机号验证码登录。   （√）
   7. 登出功能          (√)
   8. SpringSecurity异常处理   (√)
   9. 刷新Jwt有效时间(有效时间48小时，再次访问时有效时间小于24小时时重置为48小时。长时间不登录，Jwt失效)    (√)
   10. 多种验证方式下，如何配置多个AuthenticationManager
   11. 用户名+密码+图形验证码登录
   12. TODO: 基于Oauth2，调用QQ微信等接口进行认证登录。




# 功能列表
1. 用户模块
    - 注册、登录    （√）
    - 异常登录注册模块
    - 关注
    - 聊天
2. 视频模块（要关注多维度视屏流，编解码，带宽，时延，画质，RTC）
    - 视频获取
    - 视频存储
    - 视频缓存
    - 视频点赞、收藏
    - 视频评论
    - ...
3. 高并发弹幕
4. 红包
5. 商城
6. ...

# 小细节TODO
1. 连接池换成druid
2. 查询分页
3. 错误页面需要跳转到统一页面

# 碰到的问题
1. WebSecurityConfigurerAdapter从 5.7.0-M2 起已经弃用，推荐使用基于组件的 security 配置 (为了组件化)。本项目采用了最新的组件
2. 使用手机号+验证码登录会存在一个问题：手机号和验证码必须存放在UserDetails中，但是验证码存放在数据库中有点浪费。此外，authenticate中存入的原始值，UserDetails中的是加密值
3. 自定义过滤链，在继承OncePerRequestFilter类时，出现了request 自动转换为org.springframework.security.web.header.HeaderWriterFilter$HeaderWriterRequest的BUG。要统一JwtAuthenticationTokenFilter文件和TokenService文件HttpServletRequest的包文件（jakarta.servlet.http.HttpServletRequest和javax.servlet.http.HttpServletRequest;javax.servlet在2018年之后就没有更新了。新版的Spring都用的是jakarta）
4. 与后端认证对应的，在前端需要通过守护路由对页面访问进行限制，且在访问后端接口时，需要在请求头中携带token
5. JWT是无状态的，不需要存储到Redis里面，如果存在了Redis体现不出JWT无状态的特性。但是如果不存储到Redis里面，后续在进行doFilterInternal时需要从mysql中读取用户数据。
6. TODO(检测用户异常): 由于JWT的无状态性，当发现用户异常时需要采取措施。但这里一般是通过客户端的Https协议保障，为了进一步防止该情况的话，可以通过手机验证码进行二次验证。
7. 多种验证方式下，如何配置多个AuthenticationManager？AuthenticationManager的实现ProviderManager管理了众多的AuthenticationProvider，每一个AuthenticationProvider都支持特定类型的Authentication。将不同的认证方式分别交给不同的每一个AuthenticationProvider都支持特定类型的Authentication就可以了。只要用一个AuthenticationProvider认证通过，就算认证通过了。
8. 