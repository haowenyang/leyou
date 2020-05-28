package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface SpecificationService {
    List<SpecGroup> querySpecGroupsByCid(Long cid);

    List<SpecParam> querySpecParamsByGid(Long gid,Long cid,Boolean generic,Boolean searching);

    List<SpecGroup> querySpecsByCid(Long cid);
}
