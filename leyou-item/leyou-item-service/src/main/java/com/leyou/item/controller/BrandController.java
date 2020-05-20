package com.leyou.item.controller;


import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Controller
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @GetMapping("/page")
    public ResponseEntity<PageResult<Brand>> queryBrandsByPage
            (@RequestParam(name = "key", required = false) String key,
             @RequestParam(name = "page", defaultValue = "1") Integer page,
             @RequestParam(name = "rows", defaultValue = "5") Integer rows,
             @RequestParam(name = "sortBy", required = false) String sortBy,
             @RequestParam(name = "desc", required = false) Boolean desc
            ) {

        PageResult<Brand> pageResult = brandService.queryBrandsByPage(key, page, rows, sortBy, desc);
        return ResponseEntity.ok(pageResult);
    }
   @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam(name = "cids") List<Long> cids){
        brandService.saveBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
   }
   @GetMapping("/cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandsByCid(@PathVariable("cid") Long cid){
       List<Brand> brands = brandService.queryBrandsByCid(cid);
       if (CollectionUtils.isEmpty(brands)){
           return ResponseEntity.notFound().build();
       }
       return ResponseEntity.ok(brands);
   }
}
