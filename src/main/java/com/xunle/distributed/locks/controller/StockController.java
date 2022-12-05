package com.xunle.distributed.locks.controller;

import com.xunle.distributed.locks.domain.Stock;
import com.xunle.distributed.locks.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xunle
 * @date 2022/12/4 17:13
 */
@RestController
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping("stock/{id}")
    public Stock getStock(@PathVariable("id")Long id) {
        Stock stock = stockService.getStock(id);
        return stock;
    }


    @GetMapping("stock/deduct")
    public String deduct() {
        long st = System.currentTimeMillis();
        this.stockService.deduct();
        long et = System.currentTimeMillis();
        System.out.println("request cost: " + (et - st) + "ms");
        return "deduct stock";
    }
}
