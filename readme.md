# 项目进度
1. 用户模块-登录注册
   1. 手机验证码发送 （问题：云平台短信网站需要备案）（√）
   2. 前后端分离项目，跳转需要跨域                 (√)
   3. 验证码登录注册  （UserDetailsSer`vice默认是使用用户名+密码进行验证，这里对该接口进行了魔改） (√)
   4. Spring Security (目前不考虑用户权限问题，所有用户都是普通用户)`             （√）
   5. 第一次登录完成前后端交互。     （√）
   6. Spring Security 认证过滤链，实现后续登录无需在通过手机号验证码登录。   （√）
   7. 登出功能           (√)
   8. SpringSecurity异常处理   (√)



# 功能列表
1. 用户模块
    - 注册、登录
    - 异常登录注册模块
    - 关注
    - 聊天
2. 视频模块
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
4. TODO：Token在到达有效期前无法有效撤销。通过Redis作为撤销证书列表，将有异常行为的token写入Redis撤销证书列表（比如设置30分钟，表示30分钟内禁止该用户继续访问）