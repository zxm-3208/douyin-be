package com.example.douyin_gateway.config;


import com.example.douyin_commons.utils.FastJsonJsonRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author : zxm
 * @date: 2024/3/24 - 10:24
 * @Description: com.example.be.common.config
 * @version: 1.0
 */
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<Object, Object> template = new RedisTemplate<>();

        //配置连接工厂
        template.setConnectionFactory(connectionFactory);

        //redis中的key值,使用StringRedisSerializer来序列化和反序列化
        template.setKeySerializer(new StringRedisSerializer());

        //设置Hash类型的value设置序列化器
        template.setHashValueSerializer(new StringRedisSerializer());


        //使用FastJson2JsonRedisSerializer 来序列化和反序列化redis的value值
        FastJsonJsonRedisSerializer serializer = new FastJsonJsonRedisSerializer(Object.class);

        ObjectMapper mapper = new ObjectMapper();

        //指定要序列化的域: field,get和set,以及修饰符范围,ANY表示包括private和public
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        //指定序列化输入的类型,类必须是非final修饰的, final修饰的类会报异常.
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);

        //redis中存储的value值,采用json序列化
        template.setValueSerializer(serializer);

        //设置Hash类型的key设置序列化器
        template.setHashKeySerializer(serializer);

        return template;
    }
}