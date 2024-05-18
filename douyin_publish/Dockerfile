# 基础镜像
FROM openjdk:17-jdk-alpine
# 制定工作目录
WORKDIR /app
# 将jar包添加到工作目录
ADD ./target/douyin_publish-0.0.1-SNAPSHOT.jar .
# 暴露端口
EXPOSE 8040
# 启动命令
CMD ["java", "-jar", "/app/douyin_publish-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]