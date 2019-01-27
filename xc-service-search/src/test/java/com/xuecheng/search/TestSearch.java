package com.xuecheng.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestSearch {
	@Autowired
	RestHighLevelClient client;
	@Autowired
	RestClient restClient;

	/**
	 * 查询所有
	 * @throws Exception
	 */
	@Test
	public void testSearchAll() throws Exception{
		// 查询请求的对象，指定索引库
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 指定类型
		searchRequest.types("doc");
		// 搜索源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 匹配所有Document
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		// 过滤Field(第一个参数，只显示数组中的field。 第二个参数排除数组中的field显示。)
		searchSourceBuilder.fetchSource(new String[] {"name", "studymodel"},new String[] {});
		// 给查询请求设置源
		searchRequest.source(searchSourceBuilder);
		// 调用查询请求
		SearchResponse searchResponse = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = searchResponse.getHits();
		// 获取查询总条数
		long totalHits = hits.getTotalHits();
		// hits.hits, 匹配度较高的前N个文档
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit searchHit : searchHits) {
			// 当前Document的id
			String id = searchHit.getId();
			// 当前Document所属索引库名称
			searchHit.getIndex();
			// 当前Document匹配度分数
			searchHit.getScore();
			// source, 记录着(field和filed value的Map)
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			System.out.println(sourceAsMap);
		}
	}

	/**
	 * 分页查询
	 */
	@Test
	public void testSearchPage() throws Exception{
		// 查询请求的对象
	    SearchRequest searchRequest = new SearchRequest("xc_course");
	    // 类型
	    searchRequest.types("doc");
	    // 查询源对象
	    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
	    // 全部匹配
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		// 分页

		// 页码,最低第一页。
		int page = 1;
		// 每页显示个数
		int size = 1;
		int from = (page - 1) / size;
		// 起始位置 从0开始
		searchSourceBuilder.from(from);
		// 显示个数
		searchSourceBuilder.size(size);

		// 设置查询源
		searchRequest.source(searchSourceBuilder);
		// 调用请求
		SearchResponse search = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = search.getHits();
		// 查询结果总数，这里是总共能够匹配到的个数。
		long totalHits = hits.getTotalHits();
		SearchHit[] searchHits = hits.getHits();
		// 这个是当前页的记录个数
		int length = searchHits.length;
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			System.out.println(sourceAsMap);
		}
	}

	/**
	 * 精准匹配(根据Term进行查询)
	 * 也就是查询条件已经是term了不用再分词。
	 * 对已经分词的field匹配其term
	 * @throws Exception
	 */
	@Test
	public void testTermQuery() throws Exception{
		// 查询请求的对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 类型
		searchRequest.types("doc");
		// 查询源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 根据term进行匹配
		searchSourceBuilder.query(QueryBuilders.termQuery("name", "spring"));
		// 设置查询源
		searchRequest.source(searchSourceBuilder);
		// 调用请求
		SearchResponse search = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = search.getHits();
		// 匹配度高的记录
		SearchHit[] searchHits = hits.getHits();
		// 每一条代表一个Document
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			System.out.println(sourceAsMap);
		}
	}

	/**
	 * 根据多个term进行精准匹配(按照term匹配)
	 * 符合其中一个条件成立
	 */
	@Test
	public void testTermsSearch() throws Exception{
		// 查询请求的对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 类型
		searchRequest.types("doc");
		// 查询源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 根据多个id进行匹配
		searchSourceBuilder.query(QueryBuilders.termsQuery("_id", new ArrayList<>(Arrays.asList(new String[]{"1", "2"}))));
		// 设置查询源
		searchRequest.source(searchSourceBuilder);
		// 调用请求
		SearchResponse search = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = search.getHits();
		System.out.println(hits.totalHits);
		// 匹配度高的记录
		SearchHit[] searchHits = hits.getHits();
		System.out.println(searchHits.length);
		// 每一条代表一个Document
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			System.out.println(sourceAsMap);
		}
	}

	/**
	 * 全文检索
	 * 可以指定某个field，对字符串的field value进行分词去匹配field
	 * 匹配到field就可以知道field对应的Document
	 * @throws Exception
	 */
	@Test
	public void testMatchQuery() throws Exception{
		// 查询请求的对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 类型
		searchRequest.types("doc");
		// 查询源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// matchQuery匹配
		// 对String类型的查询条件分词然后去匹配field
		// operator 可以设置AND 或者 OR。
		// AND 每一个分词匹配成功才算匹配成功。
		// OR 有一个分词匹配成功就算匹配成功。
		searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发")
									.operator(Operator.OR));
		// 设置查询源
		searchRequest.source(searchSourceBuilder);
		// 调用请求
		SearchResponse search = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = search.getHits();
		System.out.println(hits.totalHits);
		// 匹配度高的记录
		SearchHit[] searchHits = hits.getHits();
		System.out.println(searchHits.length);
		// 每一条代表一个Document
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			System.out.println(sourceAsMap);
		}
	}

	/**
	 * 全文检索的 Minimum_should_match
	 * 指定至少匹配到term的占比。
	 * 如果一个匹配条件被分词为三个词，
	 * "minimum_should_match": "80%"表示，三个词在文档的匹配占比为80%，
	 * 即3*0.8=2.4，向上取整得2，表示至少有两个词在文档中要匹配成功。
	 * @throws Exception
	 */
	@Test
	public void testMatchQueryMinimum_should_match() throws Exception{
		// 查询请求的对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 类型
		searchRequest.types("doc");
		// 查询源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// matchQuery匹配
		// 对String类型的查询条件分词然后去匹配field
		// minimumShouldMatch 匹配条件分词之后必须在文档中匹配到的占比。
		// spring开发如果被分为3个词，80%也就是 0.8*3=2.4 取整 2. 也就是文档中匹配到2个才算匹配成功。
		searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发")
				.minimumShouldMatch("80%"));
		// 设置查询源
		searchRequest.source(searchSourceBuilder);
		// 调用请求
		SearchResponse search = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = search.getHits();
		System.out.println(hits.totalHits);
		// 匹配度高的记录
		SearchHit[] searchHits = hits.getHits();
		System.out.println(searchHits.length);
		// 每一条代表一个Document
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			System.out.println(sourceAsMap);
		}
	}

	/**
	 * 多个Field的匹配
	 * 可以设置多个Field,
	 * 相当于keyword，复合field. 只要有一个Field匹配成功
	 * 这个Document就算匹配成功。
	 * @throws Exception
	 */
	@Test
	public void testMultiMatchQuery() throws Exception{
		// 查询请求的对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 类型
		searchRequest.types("doc");
		// 查询源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// MultiMatch匹配
		searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring开发", new String[]{"name","description"})
				.minimumShouldMatch("80%"));
		// 设置查询源
		searchRequest.source(searchSourceBuilder);
		// 调用请求
		SearchResponse search = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = search.getHits();
		System.out.println(hits.totalHits);
		// 匹配度高的记录
		SearchHit[] searchHits = hits.getHits();
		System.out.println(searchHits.length);
		// 每一条代表一个Document
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			System.out.println(sourceAsMap);
		}
	}

	/**
	 * 多个字段查询，提升字段权重。
	 * 多个字段进行查询的时候可以提升某个字段的权重，
	 * 权重高的字段匹配到的Document会显示在前面。
	 *
	 * 比如，name为名称，description为内容。
	 * 名称里都有肯定是匹配结果，内容中有匹配权重可以低一些。
	 * @throws Exception
	 */
	@Test
	public void testMultiMatchQueryBootsUp() throws Exception{
		// 查询请求的对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 类型
		searchRequest.types("doc");
		// 查询源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// MultiMatch匹配
		// field 提升权重 10倍权重
		searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring开发", new String[]{"name","description"})
				.minimumShouldMatch("80%")
		.field("name", 10));
		// 设置查询源
		searchRequest.source(searchSourceBuilder);
		// 调用请求
		SearchResponse search = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = search.getHits();
		System.out.println(hits.totalHits);
		// 匹配度高的记录
		SearchHit[] searchHits = hits.getHits();
		System.out.println(searchHits.length);
		// 每一条代表一个Document
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			System.out.println(sourceAsMap);
		}
	}

	/**
	 * 布尔查询
	 * 将多个查询条件结合起来。
	 * must: 表示必须，多个查询条件必须都满足。(通常使用must)
	 * should: 表示或者，多个查询条件只有一个满足就可以。
	 * must_not: 多个查询条件都不满足
	 * @throws Exception
	 */
	@Test
	public void testBooleanQuery() throws Exception{
		// 查询请求的对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 类型
		searchRequest.types("doc");
		// 查询源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// multiQuery
		MultiMatchQueryBuilder multiQueryBuilder = QueryBuilders.multiMatchQuery("spring开发", new String[]{"name", "description"})
				.minimumShouldMatch("80%")
				.field("name", 10);
		// termQuery
		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");
		// booleanQuery
		searchSourceBuilder.query(QueryBuilders.boolQuery()
								.must(multiQueryBuilder)
								.must(termQueryBuilder));
		// 设置查询源
		searchRequest.source(searchSourceBuilder);
		// 调用请求
		SearchResponse search = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = search.getHits();
		System.out.println(hits.totalHits);
		// 匹配度高的记录
		SearchHit[] searchHits = hits.getHits();
		System.out.println(searchHits.length);
		// 每一条代表一个Document
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			System.out.println(sourceAsMap);
		}
	}

	/**
	 * 过滤器
	 *  对搜索的结果进行过滤，
	 *  只关注文档是否匹配，不去计算和判断文档的匹配度得分.
	 *  所以过滤器性能比查询要高，一般查询和过滤器一起使用。
	 *
	 *  "filter":[
	 *        {"term":{"studymodel":"201001"}},
	 *    {"range":{"price":{"gte":60,"lte":100}}
	 * ]
	 *  
	 *  filter的参数
	 *  	range 范围过滤,保留大于等于60 并且小于等于100的记录。
	 *  	term 项匹配过虑,保留studymodel等于"201001"的记录。
	 *  filter和查询
	 * @throws Exception
	 */
	@Test
	public void testFilter() throws Exception{
		// 查询请求的对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 类型
		searchRequest.types("doc");
		// 查询源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// multiQuery
		MultiMatchQueryBuilder multiQueryBuilder = QueryBuilders.multiMatchQuery("spring开发", new String[]{"name", "description"})
				.minimumShouldMatch("80%")
				.field("name", 10);
		// booleanQuery
		// filter 对查询的结果进行过滤。
		searchSourceBuilder.query(QueryBuilders.boolQuery()
				.must(multiQueryBuilder)
				.filter(QueryBuilders.termQuery("studymodel", "201001"))
				.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100)));

		// 设置查询源
		searchRequest.source(searchSourceBuilder);
		// 调用请求
		SearchResponse search = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = search.getHits();
		System.out.println(hits.totalHits);
		// 匹配度高的记录
		SearchHit[] searchHits = hits.getHits();
		System.out.println(searchHits.length);
		// 每一条代表一个Document
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			System.out.println(sourceAsMap);
		}
	}

	/**
	 * 排序，支持keyword，data，float等类型加上多个排序。
	 * 可以有多个field加上排序，会按照先设置的排序，如果相同按照下一个排序。
	 * @throws Exception
	 */
	@Test
	public void testSort() throws Exception{
		// 查询请求的对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 类型
		searchRequest.types("doc");
		// 查询源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// booleanQuery
		// filter 对查询的结果进行过滤。
		// 这里直接写的过滤，将所有查询结果进行过滤。
		searchSourceBuilder.query(QueryBuilders.boolQuery()
				.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100)));

		// order
		// 将查询结果按照studymodel升序排序
		// 然后再按照price降序排序
		searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC))
							.sort(new FieldSortBuilder("price").order(SortOrder.ASC));
		// 设置查询源
		searchRequest.source(searchSourceBuilder);
		// 调用请求
		SearchResponse search = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = search.getHits();
		System.out.println(hits.totalHits);
		// 匹配度高的记录
		SearchHit[] searchHits = hits.getHits();
		System.out.println(searchHits.length);
		// 每一条代表一个Document
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			System.out.println(sourceAsMap);
		}
	}

	/**
	 * 高亮
	 * 设置field的字段:查询匹配到的部分高亮显示
	 * @throws Exception
	 */
	@Test
	public void testHighlight() throws Exception{
		// 查询请求的对象
		SearchRequest searchRequest = new SearchRequest("xc_course");
		// 类型
		searchRequest.types("doc");
		// 查询源对象
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("description", "Spring开发")
															.operator(Operator.OR);
		// booleanQuery
		// filter 对查询的结果进行过滤。
		// 这里直接写的过滤，将所有查询结果进行过滤。
		searchSourceBuilder.query(QueryBuilders.boolQuery()
									.must(matchQueryBuilder)
									.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100)));

		// order
		// 将查询结果按照studymodel升序排序
		// 然后再按照price降序排序
		searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.DESC))
				.sort(new FieldSortBuilder("price").order(SortOrder.ASC));
		// 设置高亮
		searchSourceBuilder.highlighter(new HighlightBuilder()
										.preTags("<tag>")
										.postTags("</tag>")
										.field("name")
										.field("description"));
		// 设置查询源
		searchRequest.source(searchSourceBuilder);
		// 调用请求
		SearchResponse search = client.search(searchRequest);
		// 命中的记录
		SearchHits hits = search.getHits();
		System.out.println(hits.totalHits);
		// 匹配度高的记录
		SearchHit[] searchHits = hits.getHits();
		System.out.println(searchHits.length);
		// 每一条代表一个Document
		for (SearchHit searchHit : searchHits) {
			Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
			Object name = sourceAsMap.get("description");
			System.out.println("未高亮的description： ");
			System.out.println(name);
			Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
			if (highlightFields != null) {
				HighlightField highlightField = highlightFields.get("description");
				if (highlightField!=null) {
					Text[] fragments = highlightField.getFragments();
					System.out.println("高亮的description： ");
					System.out.println(fragments[0]);
				}
			}
		}
	}
}
