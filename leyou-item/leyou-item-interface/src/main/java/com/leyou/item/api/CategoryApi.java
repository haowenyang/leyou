package com.leyou.item.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("category")
public interface CategoryApi {

    /**
     * 根据id查询分类的名称(集合)
     */
    @GetMapping("/names")
    List<String> queryNamesByIds(@RequestParam("ids") List<Long> ids);
}