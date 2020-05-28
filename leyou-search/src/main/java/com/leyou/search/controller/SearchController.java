package com.leyou.search.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@Controller
public class SearchController {
    @Autowired
    private SearchService searchService;

    //查询搜索的分页结果集
    @PostMapping("/page")
    public ResponseEntity<SearchResult> searchGoods(@RequestBody SearchRequest searchRequest) {
        SearchResult searchResult = searchService.searchGoods(searchRequest);
        if (searchResult==null|| CollectionUtils.isEmpty(searchResult.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(searchResult);
    }
}
