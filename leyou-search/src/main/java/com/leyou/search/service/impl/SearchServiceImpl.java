package com.leyou.search.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.reponsitory.GoodsRepository;
import com.leyou.search.service.SearchService;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    private static final ObjectMapper Mapper = new ObjectMapper();

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    @Override
    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();
        //根据分类Id查询分类名称
        List<String> names = categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
        //根据品牌id去查询品牌
        Brand brandname = brandClient.queryBrandById(spu.getBrandId());
        //根据spu的id查询所有的sku
        List<Sku> skus = goodsClient.querySkuBySpuId(spu.getId());
        //所有sku价格集合
        List<Long> skuPriceList = new ArrayList<>();
        //需要的sku数据
        List<Map<String,Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
          skuPriceList.add(sku.getPrice());
            Map<String,Object> map= new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            skuMapList.add(map);
        });
        // 查询详情
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spu.getId());
        // 查询规格参数
        List<SpecParam> specParams = this.specificationClient.queryParams(null, spu.getCid3(), true, null);
        // 处理规格参数,进行反序列化
        Map<String, Object> genericSpecs = Mapper.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<String, Object>>() {
        });
        Map<String, List<Object>> specialSpecs = Mapper.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<Object>>>() {
        });

        Map<String,Object> specs = new HashMap<>();
        specParams.forEach(specParam -> {
            if (specParam.getSearching()){
                if (specParam.getGeneric()){
                    String value = genericSpecs.get(specParam.getId().toString()).toString();
                    if(specParam.getNumeric()){
                        value = chooseSegment(value, specParam);
                    }
                    specs.put(specParam.getName(), StringUtils.isBlank(value) ? "其它" : value);
                } else {
                    specs.put(specParam.getName(), specialSpecs.get(specParam.getId().toString()));
                }
            }
        });

        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names, " ")+ null);
        //获取spu下所有的sku的价格
        goods.setPrice(skuPriceList);
        goods.setSkus(Mapper.writeValueAsString(skuMapList));
        goods.setSpecs(specs);

        return goods;
    }

    @Override
    public SearchResult searchGoods(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        if (StringUtils.isBlank(key)){
            return null;
        }
        //自定义查询器
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
        //添加查询条件
        //QueryBuilder basicQuery = QueryBuilders.matchQuery("all", key).operator(Operator.AND);
        QueryBuilder boolQueryBuilder = this.buildBooleanQueryBuilder(searchRequest);
        builder.withQuery(boolQueryBuilder);
        //通过sourceFilter设置返回的结果字段,我们只需要spu的id、skus、subTitle
        builder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id", "skus", "subTitle"}, null));
        //分页,页码从0开始
        builder.withPageable(PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize()));
        //添加排序
        if (StringUtils.isNotBlank(searchRequest.getSortBy())) {
            builder.withSort(SortBuilders.fieldSort(searchRequest.getSortBy()).order(searchRequest.getDescending() ? SortOrder.DESC : SortOrder.ASC));
        }
        String categoryAggName = "categories";
        String brandAggName = "brands";
        //添加对分类的聚合
        builder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //添加对品牌的聚合
        builder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        //执行查询
        AggregatedPage<Goods> searchResult = (AggregatedPage<Goods>) goodsRepository.search(builder.build());

        //获取聚合结果并解析
        List<Map<String, Object>> categoryDatas = this.getCategoryAggregation(searchResult.getAggregation(categoryAggName));
        List<Brand> brands = this.getBrandAggregation(searchResult.getAggregation(brandAggName));

        //判断聚合出的分类是否是1个，如果是1个则进行该分类的规格参数聚合
        List<Map<String, Object>> specs = null;
        if (categoryDatas.size() == 1) {
            specs = this.getParamAggresult((Long) categoryDatas.get(0).get("id"),boolQueryBuilder);
        }
        //封装分页数据
        SearchResult searchPageResult = new SearchResult(searchResult.getTotalElements(), searchResult.getTotalPages(), searchResult.getContent(), categoryDatas, brands,specs);

        return searchPageResult;
    }

    @Override
    public void createIndex(Long id) throws IOException {
        //查询spu
        Spu spu = this.goodsClient.querySpuById(id);
        Goods goods = this.buildGoods(spu);
        //保存
        this.goodsRepository.save(goods);
    }

    @Override
    public void deleteIndex(Long id) {
        this.goodsRepository.deleteById(id);
    }

    private QueryBuilder buildBooleanQueryBuilder(SearchRequest searchRequest) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 添加基本查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));
        // 添加过滤条件
       if (CollectionUtils.isEmpty(searchRequest.getFilter())) {
            return boolQueryBuilder;
        }
        Map<String,Object> filter = searchRequest.getFilter();
        for (Map.Entry<String,Object> entry:filter.entrySet()){
            String key = entry.getKey();
            if (StringUtils.equals("品牌",key)){
                key="brandId";
            }else if (StringUtils.equals("分类",key)){
                key="cid3";
            }else {
                key = "specs." + key + ".keyword";
            }
            //添加到过滤中
            boolQueryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));
        }
        return boolQueryBuilder;
    }

    //根据查询条件聚合规格参数
    private List<Map<String, Object>> getParamAggresult(Long cid, QueryBuilder basicQuery) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        List<SpecParam> params = specificationClient.queryParams(null, cid, null, true);
        params.forEach(param -> {
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs." + param.getName()+".keyword"));
        });
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{},null));
        List<Map<String,Object>> specs = new ArrayList<>();
        AggregatedPage<Goods> goodsPage =(AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());
        Map<String,Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        for (Map.Entry<String,Aggregation> entry:aggregationMap.entrySet()){
            Map<String,Object> map = new HashMap<>();
            map.put("k",entry.getKey());
            List<Object> options = new ArrayList<>();
            Terms terms = (Terms)entry.getValue();
            terms.getBuckets().forEach(bucket ->options.add(bucket.getKeyAsString()));
            map.put("options",options);
            specs.add(map);
        }
        return specs;
    }
    // 解析品牌聚合结果
    private List<Brand> getBrandAggregation(Aggregation brandAgg) {
            LongTerms terms = (LongTerms) brandAgg;
            List<Brand> brands = new ArrayList<>();
            for (LongTerms.Bucket bucket : terms.getBuckets()) {
               Brand brand = brandClient.queryBrandById(bucket.getKeyAsNumber().longValue());
               brands.add(brand);
            }
            return brands;
    }

    //聚合出规格参数
    private List<Map<String, Object>> getCategoryAggregation(Aggregation categoryAgg) {
        LongTerms terms = (LongTerms) categoryAgg;
        return terms.getBuckets().stream().map(bucket -> {
            Map<String, Object> map = new HashMap<>();
            Long id = bucket.getKeyAsNumber().longValue();
            List<String> names = categoryClient.queryNamesByIds(Arrays.asList(id));
            map.put("id",id);
            map.put("name",names.get(0));
            return map;
        }).collect(Collectors.toList());
    }
}
