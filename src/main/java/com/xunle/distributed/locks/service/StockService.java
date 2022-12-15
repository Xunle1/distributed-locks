package com.xunle.distributed.locks.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xunle.distributed.locks.domain.Stock;
import com.xunle.distributed.locks.mapper.StockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xunle
 * @date 2022/12/4 17:10
 */
@Service
public class StockService {

    private ReentrantLock lock = new ReentrantLock();

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void deduct() {
        // redis setnx分布式锁
        UUID uuid = UUID.randomUUID();
        while (!this.redisTemplate.opsForValue().setIfAbsent("lock", uuid.toString(), 60, TimeUnit.SECONDS)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            // 业务逻辑
            String stock = this.redisTemplate.opsForValue().get("stock");
            if (stock != null && Integer.parseInt(stock) > 0) {
                this.redisTemplate.opsForValue().set("stock", String.valueOf(Integer.parseInt(stock) - 1));
            }
        } finally {
            // 释放锁 防误删
            String checkAndDeleteScript = new StringBuilder()
                    .append("if (redis.call('get', 'lock') == ARGV[1]) then redis.call('del', 'lock') return 0 else return 1 end")
                    .toString();
            RedisScript redisScript = new DefaultRedisScript(checkAndDeleteScript, Long.class);
            Long result = (Long)this.redisTemplate.execute(redisScript, new ArrayList<>(), uuid.toString());
            System.out.println(result);
        }
    }

    public void deduct4() {
        // redis 事务乐观锁
        this.redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                while (true) {
                    operations.watch("stock");
                    String stock = (String) operations.opsForValue().get("stock");

                    if (stock != null && Integer.parseInt(stock) > 0) {
                        operations.multi();
                        operations.opsForValue().set("stock", String.valueOf(Integer.parseInt(stock) - 1));
                        List<Object> exec = operations.exec();
                        if (exec.size() > 0) return exec;
                    }
                    if (Integer.parseInt(stock) == 0) {
                        return null;
                    }
                }
            }
        });
    }

    public void deduct3() {
        // 乐观锁
        List<Stock> stocks = this.stockMapper.selectList(new QueryWrapper<Stock>().eq("product_code", "1001"));
        Stock stock = stocks.get(0);
        Integer version = stock.getVersion();
        stock.setVersion(version + 1);
        stock.setCount(stock.getCount() - 1);
        if (this.stockMapper.update(stock, new UpdateWrapper<Stock>()
                .eq("id", stock.getId())
                .eq("version", version)) == 0) {
            this.deduct();
        }
    }

    @Transactional
    public void deduct2() {
        // select for update
        List<Stock> stocks = this.stockMapper.selectForUpdate("1001");
        Stock stock = stocks.get(0);
        if (stock != null && stock.getCount() > 0) {
            stock.setCount(stock.getCount() - 1);
            stockMapper.updateById(stock);
        }
    }

    public Stock getStock(Long id) {
        return stockMapper.selectById(id);
    }
}
