package com.example.douyin_chat.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author : zxm
 * @date: 2024/5/31 - 22:55
 * @Description: IM节点的POJO类
 * @version: 1.0
 */
@Data
public class ImNode implements Comparable<ImNode>, Serializable {
    @Serial
    private static final long serialVersionUID = -5057956142938020649L;

    // worker 的ID (由zookeeper生成)
    private long id;

    // Netty 服务的连接数
    private Integer balance = 0;

    // TODO: 这些配置放到nacos上
    // Netty 服务IP
    private String host = "127.0.0.1";

    // Netty 服务端口
    private Integer port = 8081;

    public ImNode(){

    }

    public ImNode(String host, Integer port){
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString()
    {
        return "ImNode{" +
                "id='" + id + '\'' +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ",balance=" + balance +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImNode node = (ImNode) o;
        return Objects.equals(host, node.host) &&
                Objects.equals(port, node.port);
    }

    public int hasCode(){
        return Objects.hash(id, host, port);
    }

    @Override
    public int compareTo(ImNode o) {
        int weight1 = this.balance;
        int weight2 = o.balance;
        if(weight1 > weight2){
            return 1;
        }else if(weight1 < weight2){
            return -1;
        }
        return 0;
    }

    public void incrementBalance(){
        balance++;
    }

    public void decrementBalance(){
        balance--;
    }

}
