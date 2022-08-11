package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.AttrRespTo;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.BrandEntity;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 无名氏
 * @date 2022/7/24
 * @Description:
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;
    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult result = null;

        //准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            result = buildSearchResponse(searchParam,response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }



    /**
     * 构建检索请求
     * 模糊匹配，过滤(按照属性，分类，品牌，价格区间，库存)，排序，分页，高亮，聚合分析
     * @return
     * @param searchParam
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //模糊匹配，过滤
        BoolQueryBuilder boolQueryBuilder = boolSearch(searchParam);
        sourceBuilder.query(boolQueryBuilder);

        //排序
        //sort=saleCount_asc/desc
        //"sort": [{"skuPrice": {"order": "desc"}}]
        String sort = searchParam.getSort();
        if (StringUtils.hasText(sort)){
            String[] s = sort.split("_");
            if (s.length==2 && !sort.startsWith("_")){
                SortOrder sortOrder = "asc".equalsIgnoreCase(s[1]) ? SortOrder.ASC:SortOrder.DESC;
                //SortOrder sortOrder = SortOrder.fromString(s[1]);
                sourceBuilder.sort(s[0],sortOrder);
            }
        }

        //分页
        //"from": 0,"size": 5,
        Integer pageNum = searchParam.getPageNum();
        if (pageNum==null || pageNum<=0){
            pageNum = 1;
        }
        int from = (pageNum-1) * EsConstant.PRODUCT_PAGE_SIZE;
        sourceBuilder.from(from).size(EsConstant.PRODUCT_PAGE_SIZE);

        //高亮
        //"highlight": {"fields": {"skuTitle": {}},"pre_tags": "<b style='color:red'>","post_tags": "</b>"},
        if (StringUtils.hasText(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        //聚合分析

        //品牌聚合
        //"aggs": {"brand_agg": {"terms": {"field": "brandId","size": 10},
        //                         "aggs": {"brand_name_agg": {"terms": {"field": "brandName","size": 1}},
        //                                  "brand_img_agg":{"terms": {"field": "brandImg","size": 1}}}}}
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(10);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brandAgg);

        //分类聚合
        //"aggs": {"catalog_agg": {"terms": {"field": "catalogId","size": 10},
        //                          "aggs": {"catalog_name_agg": {"terms": {"field": "catalogName","size": 1}}}},}
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg");
        catalogAgg.field("catalogId").size(10);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalogAgg);

        //属性聚合
        //"aggs": {"attr_agg":{"nested": {"path": "attrs"},
        //         "aggs": {"attr_id_agg":{"terms": {"field": "attrs.attrId","size": 10},
        //            "aggs": {"attr_name_agg": {"terms": {"field": "attrs.attrName","size": 1}},
        //                   "attr_value_agg":{"terms": {"field": "attrs.attrValue","size": 10}}}}}}}
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg","attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg");
        attrIdAgg.field("attrs.attrId").size(10);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10));
        attrAgg.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(attrAgg);

        System.out.println(sourceBuilder.toString());
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX},sourceBuilder);
    }


    /**
     *"bool": {
     *       "must": [{"match": {"skuTitle": "华为"}}],
     *       "filter": [{"term": {"catalogId": "225"}},{"terms": {"brandId": ["1","2","9"]}},
     *         {"nested": {"path": "attrs","query": {"bool": {"must": [{"term": {"attrs.attrId": {"value": "1"}}},
     *           {"terms": {"attrs.attrValue": ["LIO-A00","A2100"]}}]}}}},
     *         {"term": {"hasStock": {"value": "true"}}},{"range": {"skuPrice": {"gte": 0,"lte": 6000}}}]
     * }
     * @param searchParam
     * @return
     */
    private BoolQueryBuilder boolSearch(SearchParam searchParam) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.hasText(searchParam.getKeyword())){
            //sku标题
            //"must": [{"match": {"skuTitle": "华为"}}],
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle",searchParam.getKeyword()));
        }
        //分类id
        //"filter": [{"term": {"catalogId": "225"}}]
        if (searchParam.getCatalog3Id()!=null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId",searchParam.getCatalog3Id()));
        }
        //品牌id
        //"filter": [{"terms": {"brandId": ["1","2","9"]}}]
        if (!CollectionUtils.isEmpty(searchParam.getBrandId())){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",searchParam.getBrandId()));
        }
        //按照属性进行查询
        //"filter": [{"nested": {"path": "attrs","query": {"bool": {"must": [{"term": {"attrs.attrId": {"value": "1"}}},
        //   {"terms": {"attrs.attrValue": ["LIO-A00","A2100"]}}]}}}},{"nested","path":.......}]
        if (!CollectionUtils.isEmpty(searchParam.getAttrs())){
            List<String> attrs = searchParam.getAttrs();
            //attrs=1_安卓:其他&attrs=2_5寸:6寸
            for (String attr : attrs) {
                //各属性是或的关系（一个attrId不可能即使1也是2），
                //nested放循环里是或的关系。放在外面就是与的关系
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                //attrs=1_安卓:其他
                //s[0]=1   s[1]=安卓:其他
                String[] s = attr.split("_");
                // "1_安卓:其他" ==> [1,安卓:其他]  length=2
                // "_安卓:其他"  ==>  [,安卓:其他]  length=2
                if (s.length==2 && !attr.startsWith("_")){
                    String attrId = s[0];
                    //安卓:其他
                    String[] attrValue = s[1].split(":");
                    //防止s[1] = :其他
                    if (StringUtils.hasText(attrValue[0])){
                        nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                        nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValue));
                    }
                }
                //ScoreMode.None 不参与评分
                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs",nestedBoolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }
        //是否有库存
        //"filter": [{"term": {"hasStock": {"value": "true"}}}]
        if (searchParam.getHasStock()!=null) {
            boolean hasStock =  searchParam.getHasStock() == 1;
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", hasStock));
        }
        //按照价格区间查询
        //"filter": [{"range": {"skuPrice": {"gte": 0,"lte": 6000}}}]
        if (StringUtils.hasText(searchParam.getSkuPrice())){
            RangeQueryBuilder skuPriceRange = QueryBuilders.rangeQuery("skuPrice");
            String skuPrice = searchParam.getSkuPrice();
            String[] s = skuPrice.split("_");
            //价格在1~500之间  skuPrice=1_500
            // "1_500" ==> [1, 500]  length=2
            if (s.length==2 && !skuPrice.startsWith("_")){
                skuPriceRange = skuPriceRange.gte(s[0]).lte(s[1]);
            }
            //价格不高于500    skuPrice=_500
            // "_500"  ==>  [, 500]  length=2
            else if (s.length==2 && skuPrice.startsWith("_")){
                skuPriceRange = skuPriceRange.lte(s[1]);
            }
            //价格不低于1      skuPrice=1_
            // "1_"    ==>  [1]      length=1
            else if (s.length==1 && skuPrice.endsWith("_")){
                skuPriceRange = skuPriceRange.gte(s[0]);
            }
            boolQueryBuilder.filter(skuPriceRange);
        }
        return boolQueryBuilder;
    }

    /**
     * 根据查询到的数据，构建返回结果
     *
     * @param searchParam
     * @param response
     * @return
     */
    private SearchResult buildSearchResponse(SearchParam searchParam, SearchResponse response) {
        SearchHits searchHits = response.getHits();
        SearchResult searchResult = new SearchResult();
        //1、返回的所有查询到的商品
        SearchHit[] hits = searchHits.getHits();
        List<SkuEsModel> skuEsModels = null;
        if (hits !=null && hits.length>0){
            skuEsModels = new ArrayList<>();
            for (SearchHit hit : hits) {
                String s = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(s, SkuEsModel.class);
                if (StringUtils.hasText(searchParam.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    if (skuTitle != null) {
                        Text[] fragments = skuTitle.getFragments();
                        if (fragments != null && fragments.length > 0) {
                            skuEsModel.setSkuTitle(fragments[0].string());
                        }
                    }
                }
                skuEsModels.add(skuEsModel);
            }
        }
        searchResult.setProducts(skuEsModels);

        Aggregations aggregations = response.getAggregations();
        ////2、当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = aggregations.get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket attrBucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            Long attrId = attrBucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);
            ParsedStringTerms attrNameAgg = attrBucket.getAggregations().get("attr_name_agg");
            List<? extends Terms.Bucket> attrNameBuckets = attrNameAgg.getBuckets();
            if (!CollectionUtils.isEmpty(attrNameBuckets)){
                String attrName = attrNameBuckets.get(0).getKeyAsString();
                attrVo.setAttrName(attrName);
            }

            ParsedStringTerms attrValueAgg = attrBucket.getAggregations().get("attr_value_agg");
            List<? extends Terms.Bucket> attrValueBucket = attrValueAgg.getBuckets();
            if (!CollectionUtils.isEmpty(attrValueBucket)){
                List<String> attrValues = attrValueBucket.stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
                attrVo.setAttrValue(attrValues);
            }
            attrVos.add(attrVo);
        }
        searchResult.setAttrs(attrVos);
        //3、当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brandAgg = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> brandBuckets = brandAgg.getBuckets();
        if (!CollectionUtils.isEmpty(brandBuckets)){
            for (Terms.Bucket brandBucket : brandBuckets) {
                SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
                Long brandId = brandBucket.getKeyAsNumber().longValue();
                brandVo.setBrandId(brandId);

                ParsedStringTerms brandImgAgg = brandBucket.getAggregations().get("brand_img_agg");
                List<? extends Terms.Bucket> brandImgBuckets = brandImgAgg.getBuckets();
                if (!CollectionUtils.isEmpty(brandImgBuckets)){
                    String brandImg = brandImgBuckets.get(0).getKeyAsString();
                    brandVo.setBrandImg(brandImg);
                }

                ParsedStringTerms brandNameAgg = brandBucket.getAggregations().get("brand_name_agg");
                List<? extends Terms.Bucket> brandNameBuckets = brandNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(brandNameBuckets)){
                    String brandName = brandNameBuckets.get(0).getKeyAsString();
                    brandVo.setBrandName(brandName);
                }
                brandVos.add(brandVo);
            }
        }
        searchResult.setBrands(brandVos);
        //4、当前所有商品涉及到的所有分类信息
        ParsedLongTerms catalogAgg = aggregations.get("catalog_agg");
        List<? extends Terms.Bucket> catalogBuckets = catalogAgg.getBuckets();
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(catalogBuckets)){
            for (Terms.Bucket catalogBucket : catalogBuckets) {
                SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
                long catalogId = catalogBucket.getKeyAsNumber().longValue();
                catalogVo.setCatalogId(catalogId);
                ParsedStringTerms catalogNameAgg = catalogBucket.getAggregations().get("catalog_name_agg");
                List<? extends Terms.Bucket> catalogNameBucket = catalogNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(catalogNameBucket)){
                    String catalogName = catalogNameBucket.get(0).getKeyAsString();
                    catalogVo.setCatalogName(catalogName);
                }
                catalogVos.add(catalogVo);
            }
        }
        searchResult.setCatalogs(catalogVos);
        //5、分页信息-页码
        Integer pageNum = searchParam.getPageNum();
        if (pageNum ==null|| pageNum<=0){
            pageNum = 1;
        }
        searchResult.setPageNum(pageNum);
        //6、分页信息-总记录树
        long total = searchHits.getTotalHits().value;
        searchResult.setTotal(total);
        //7、分页信息-总页码
        long totalPage = (long) Math.ceil((total/(double)EsConstant.PRODUCT_PAGE_SIZE));
        //long totalPage = (total-1)%EsConstant.PRODUCT_PAGE_SIZE +1;
        if (totalPage> Integer.MAX_VALUE){
            totalPage = Integer.MAX_VALUE;
        }
        searchResult.setTotalPages((int)totalPage);

        //面包屑
        List<SearchResult.NavVo> navs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(searchParam.getAttrs())) {
            navs = searchParam.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //attrs=1_安卓:其他
                //s[0]=1   s[1]=安卓:其他
                String[] s = attr.split("_");
                // "1_安卓:其他" ==> [1,安卓:其他]  length=2
                // "_安卓:其他"  ==>  [,_安卓:其他]  length=2
                if (s.length==2 && !attr.startsWith("_")){
                    String attrId = s[0];
                    searchResult.getAttrIds().add(Long.parseLong(attrId));
                    //如果远程服务调用失败，就用id作为属性值
                    String name = attrId;
                    try {
                        R r = productFeignService.attrInfo(Long.parseLong(attrId));
                        if (r.getCode()==0){
                            Object attrVo = r.get("attr");
                            String attrString = JSON.toJSONString(attrVo);
                            AttrRespTo attrRespTo = JSON.parseObject(attrString, AttrRespTo.class);
                            name = attrRespTo.getAttrName();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    navVo.setNavName(name);
                    //设置属性值
                    navVo.setNavValue(s[1]);

                    //取消这个导航栏需要跳转到的url
                    String queryString = searchParam.getQueryString();
                    String value = UriUtils.encode(attr,"UTF-8");
                    String replace = replaceQueryString(queryString,"attrs", value);
                    if (StringUtils.hasText(replace)){
                        navVo.setLink(EsConstant.searchURI + "?" + replace);
                    }else {
                        navVo.setLink(EsConstant.searchURI);
                    }

                }
                return navVo;
            }).collect(Collectors.toList());
        }


        //品牌、分类
        if (!CollectionUtils.isEmpty(searchParam.getBrandId())) {
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            //远程查询所有品牌
            R r = productFeignService.branInfo(searchParam.getBrandId());
            if (r.getCode() == 0) {
                StringBuffer stringBuffer = new StringBuffer();
                Object brand = r.get("brand");
                String brandListString = JSON.toJSONString(brand);
                List<BrandEntity> brandEntities = JSON.parseArray(brandListString, BrandEntity.class);
                String replace = "";
                for (BrandEntity brandEntity : brandEntities) {
                    stringBuffer.append(brandEntity.getName()+";");
                    replace = replaceQueryString(searchParam.getQueryString(),"brandId",brandEntity.getBrandId()+"");
                }
                navVo.setNavValue(stringBuffer.toString());
                if (StringUtils.hasText(replace)){
                    navVo.setLink(EsConstant.searchURI + "?" + replace);
                }else {
                    navVo.setLink(EsConstant.searchURI);
                }
            }
            navs.add(navVo);
        }
        //TODO 分类不需要导航取消
        searchResult.setNavs(navs);

        return searchResult;
    }

    private String replaceQueryString(String queryString,String key,String value) {
        String param = key + "=" + value;
        String replace;
        int attrIndex = queryString.indexOf(param);
        if (queryString.startsWith(param)) {
            //判断该参数后面还有没有参数
            if (queryString.indexOf("&",attrIndex+1) >=0) {
                //该属性是第一个参数，且不是最后一个参数
                //http://search.gulimall.com/list.html?attrs=1_A2100&sort=saleCount_asc
                replace = queryString.replace(key +"=" + value +"&", "");
            }else {
                //该参数是第一个参数，也是最后一个参数
                //http://search.gulimall.com/list.html?attrs=1_A2100
                replace = queryString.replace(key+"=" + value, "");
            }
        }else {
            //该属性不是第一个参数
            //http://search.gulimall.com/list.html?hasStock=1&attrs=1_A2100
            replace = queryString.replace("&"+key+"=" + value, "");
        }
        return replace;
    }


}
