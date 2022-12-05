package com.xunle.distributed.locks.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author xunle
 * @date 2022/12/4 17:09
 */
@TableName("db_stock")
@Data
public class Stock {

    private Long id;
    private String productCode;
    private String warehouse;
    private Integer count;
    private Integer version;
}
