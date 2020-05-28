package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;

    @GetMapping("/groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroupsByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> specGroups = specificationService.querySpecGroupsByCid(cid);
       if (CollectionUtils.isEmpty(specGroups)){
           return ResponseEntity.notFound().build();
       }
       return ResponseEntity.ok(specGroups);
    }
    @GetMapping("/params")
    public ResponseEntity<List<SpecParam>> querySpecParamsByGid(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "generic", required = false) Boolean generic,
            @RequestParam(value = "searching", required = false) Boolean searching
    ){
        List<SpecParam> specParams = specificationService.querySpecParamsByGid(gid,cid,generic,searching);
        if (CollectionUtils.isEmpty(specParams)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(specParams);
    }
    @GetMapping("group/param/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecsByCid(@PathVariable Long cid) {
        List<SpecGroup> specGroups = this.specificationService.querySpecsByCid(cid);
        if (CollectionUtils.isEmpty(specGroups)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(specGroups);
    }
}
