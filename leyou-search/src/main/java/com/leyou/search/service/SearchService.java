package com.leyou.search.service;



import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Spu;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import org.elasticsearch.search.aggregations.Aggregation;


import java.io.IOException;



public interface SearchService {
    Goods buildGoods(Spu spu) throws IOException;
    SearchResult searchGoods(SearchRequest searchRequest);
    void createIndex(Long id) throws IOException ;
    void deleteIndex(Long id);
}
