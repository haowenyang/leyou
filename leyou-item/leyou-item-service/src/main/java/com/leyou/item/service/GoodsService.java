package com.leyou.item.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

public interface GoodsService {
    PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows);
    void saveGoods(SpuBo spuBo);
    SpuDetail querySpuDetailBySpuId(Long id);
    List<Sku> querySkusBySpuId(Long id);
    void updateGoods(SpuBo spuBo);
}
