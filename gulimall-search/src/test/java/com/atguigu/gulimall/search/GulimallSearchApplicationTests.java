package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

	@Autowired
	RestHighLevelClient client;

/* 从es中查询数据
GET bank/_search
{
  "query": {
    "match": {
      "address": "mill"
    }
  },
  "aggs": {
    "ageAgg": {
      "terms": {
        "field": "age",
        "size": 10
      }
    },
    "ageAvg": {
      "avg": {
        "field": "age"
      }
    },
    "balanceAvg": {
      "avg": {
        "field": "balance"
      }
    }
  },
  "size": 0
}
 */
	@Test
	public void searchData() throws IOException {
		//1、创建检索请求
		SearchRequest searchRequest = new SearchRequest();
		//指定索引
		searchRequest.indices("bank");

		//指定DSL，检索条件
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		//sourceBuilder.query();
		//sourceBuilder.from();
		//sourceBuilder.size();
		//sourceBuilder.aggregation()
		sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));

		TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
		sourceBuilder.aggregation(ageAgg);
		AvgAggregationBuilder ageAvg = AggregationBuilders.avg("ageAvg").field("age");
		//嵌套聚合
		//ageAvg.subAggregation(AggregationBuilders.avg().field())
		sourceBuilder.aggregation(ageAvg);
		AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
		sourceBuilder.aggregation(balanceAvg);

		//sourceBuilder.size(0);
		System.out.println(sourceBuilder);

		searchRequest.source(sourceBuilder);

		//2、执行检索
		SearchResponse searchResponse = client.search(searchRequest,GulimallElasticSearchConfig.COMMON_OPTIONS);

		//3、分析结果
		System.out.println(searchResponse);
		//3.1、获取符合条件的实体类
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		/*
	    {"_index" : "bank",
        "_type" : "account",
        "_id" : "472",
        "_score" : 5.4032025,
        "_source" : {}}
		 */
		for (SearchHit hit : searchHits) {
			String string = hit.getSourceAsString();
			Account account = JSON.parseObject(string, Account.class);
			System.out.println("account:"+account);
		}
		//3.2、获取这次检索的分析信息
		Aggregations aggregations = searchResponse.getAggregations();
		Terms ageAgg1 = aggregations.get("ageAgg");
		for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
			String keyAsString = bucket.getKeyAsString();
			System.out.println("年龄："+keyAsString+"==>"+bucket.getDocCount());
		}
		Avg balanceAvg1 = aggregations.get("balanceAvg");
		System.out.println("平均薪资："+balanceAvg1.getValue());
	}

	@ToString
	@Data
	static class Account{
			private int account_number;
			private int balance;
			private String firstname;
			private String lastname;
			private int age;
			private String gender;
			private String address;
			private String employer;
			private String email;
			private String city;
			private String state;
	}

	/**
	 * 存储或更新数据到es
	 * @throws IOException
	 */
	@Test
	public void indexData() throws IOException {
		IndexRequest indexRequest = new IndexRequest("users");
		//数据的id
		indexRequest.id("1");

		//indexRequest.source("userName","zhangsan","age",18,"gender","男");
		User user = new User();
		String jsonString = JSON.toJSONString(user);
		//要保存的内容
		indexRequest.source(jsonString, XContentType.JSON);

		//执行操作
		IndexResponse indexResponse = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

		//提取有用的响应信息
		System.out.println(indexResponse);
	}

	@Data
	class User{
		private String userName;
		private String gender;
		private Integer age;
	}


	@Test
	public void contextLoads() {
		System.out.println(client);
	}
}
