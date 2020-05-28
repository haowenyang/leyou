package com.leyou.service.impl;


import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.item.pojo.*;
import com.leyou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;

    @Override
    public Map<String, Object> loadData(Long spuId) {
        Map<String, Object> model = new HashMap<>();
        // 查询spu
        Spu spu = this.goodsClient.querySpuById(spuId);
        // 查询spu详情
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spuId);
        // 查询sku
        List<Sku> skus = this.goodsClient.querySkuBySpuId(spuId);
        //查询分类
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = categoryClient.queryNamesByIds(cids);

        List<Map<String,Object>> categories = new ArrayList<>();
        for (int i = 0; i <cids.size() ; i++) {
            Map<String,Object> map = new HashMap<>();
            map.put("id",cids.get(i));
            map.put("name",names.get(i));
            categories.add(map);
        }
        // 查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());
        // 查询组内参数
        List<SpecGroup> specGroups = this.specificationClient.querySpecsByCid(spu.getCid3());
        // 查询所有特有规格参数
        List<SpecParam> specParams = this.specificationClient.queryParams(null, spu.getCid3(), null, false);
        // 处理规格参数
        Map<Long, String> paramMap = new HashMap<>();
        specParams.forEach(param->{
            paramMap.put(param.getId(), param.getName());
        });
        model.put("spu", spu);
        model.put("spuDetail", spuDetail);
        model.put("skus", skus);
        model.put("brand", brand);
        model.put("categories", categories);
        model.put("groups", specGroups);
        model.put("params", paramMap);

        return model;
    }
}
