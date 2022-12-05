package com.xunle.distributed.locks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xunle.distributed.locks.domain.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author xunle
 * @date 2022/12/4 20:15
 */
@Repository
public interface StockMapper extends BaseMapper<Stock> {

    @Update("update db_stock set count = count - #{count} where product_code = #{productCode} and count >= 1")
    public void updateStock(@Param("productCode")String productCode, @Param("count")int count);

    @Select("select * from db_stock where product_code = #{productCode} and count >= 1 for update")
    public List<Stock> selectForUpdate(@Param("productCode")String productCode);
}
