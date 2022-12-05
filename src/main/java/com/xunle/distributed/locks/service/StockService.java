package com.xunle.distributed.locks.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xunle.distributed.locks.domain.Stock;
import com.xunle.distributed.locks.mapper.StockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public void deduct() {
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
