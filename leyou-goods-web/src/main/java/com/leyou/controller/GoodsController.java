package com.leyou.controller;

import com.leyou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;


@Controller

public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    /**
     * 跳转到商品详情页
     */
    @GetMapping("item/{id}.html")
    public String toItemPage(Model model, @PathVariable("id")Long id){
        Map<String, Object> map = goodsService.loadData(id);
        model.addAllAttributes(map);
        return "item";
    }
}
