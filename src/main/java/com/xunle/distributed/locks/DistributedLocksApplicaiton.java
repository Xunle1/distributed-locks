package com.xunle.distributed.locks;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author xunle
 * @date 2022/12/4 17:07
 */
@SpringBootApplication
@MapperScan("com.xunle.distributed.locks.mapper")
public class DistributedLocksApplicaiton {
    public static void main(String[] args) {
        SpringApplication.run(DistributedLocksApplicaiton.class,args);
    }
}
