package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class EsCourseService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EsCourseService.class);
	@Value("${xuecheng.elasticsearch.course.index}")
	private String es_index;
	@Value("${xuecheng.elasticsearch.course.type}")
	private String es_type;
	@Value("${xuecheng.elasticsearch.course.source_field}")
	private String source_field;

	@Autowired
	RestHighLevelClient client;

	/**
	 * 按条件分页查询CoursePub列表
	 * @param page
	 * @param size
	 * @param courseSearchParam
	 * @return
	 */
	public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
		if (Objects.isNull(courseSearchParam)) {
			courseSearchParam = new CourseSearchParam();
		}

		// 查询请求对象
		SearchRequest searchRequest = new SearchRequest(es_index);
		// 设置document的type
		searchRequest.types(es_type);
		// source源
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// source源字段过虑
		String[] source_fields = source_field.split(",");
		// 只查询source_fields中的字段
		searchSourceBuilder.fetchSource(source_fields, new String[]{});
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		// 按照关键字查询
		String keyword = courseSearchParam.getKeyword();
		if (StringUtils.isNotEmpty(keyword)) {
			// multiMatchQuery 复合查询，同时匹配多个field，只要有一个匹配成功就算查询成功。
			// field 提升权重，能够让权重高的显示在上面
			// minimumShouldMatch 分词之后匹配到的最小数量。如果分词为3个，70%起码匹配到2个才算成功。
			boolQueryBuilder.must(QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(),
					"name", "teachplan","description")
					.minimumShouldMatch("70%")
					.field("name", 10));
		}

		// 按照分类和难度等级查询
		// 这里使用filter，filter比查询要快。并且支持term和range过滤。
		// 这里全部使用term，不需要分词。
		if(StringUtils.isNotEmpty(courseSearchParam.getMt())){
			boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
		}
		if(StringUtils.isNotEmpty(courseSearchParam.getSt())){
			boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
		}
		if(StringUtils.isNotEmpty(courseSearchParam.getGrade())){
			boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
		}
		boolQueryBuilder.mustNot(QueryBuilders.termQuery("_id", "%{id}"));
		// 分页
		// 如果当前页小于等于0，默认为1.
		if(page <= 0){
			page = 1;
		}

		// 如果当前页显示数量小于等于0，默认为20
		if(size<=0){
			size = 20;
		}

		int start = (page-1)*size;
		searchSourceBuilder.from(start);
		searchSourceBuilder.size(size);

		// 使用boolean查询
		searchSourceBuilder.query(boolQueryBuilder);

		// 高亮设置
		// 将匹配到的term前后分别加上preTags 和 postTags
		searchSourceBuilder.highlighter(new HighlightBuilder()
				.preTags("<font class='eslight'>")
				.postTags("</font>")
				.field("name"));

		// 设置source
		searchRequest.source(searchSourceBuilder);

		// 调用请求
		SearchResponse searchResponse = null;
		try {
			searchResponse = client.search(searchRequest);
		} catch (IOException e) {
			LOGGER.error("xuecheng search error..{}",e.getMessage());
			return new QueryResponseResult(CommonCode.FAIL,new QueryResult<CoursePub>());
		}
		// 结果集处理
		SearchHits hits = searchResponse.getHits();
		// 总记录数
		long totalHits = hits.getTotalHits();
		// 用来记录查询到的CoursePub
		List<CoursePub> coursePubList = new ArrayList<>();
		// 匹配度高的记录数
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit searchHit : searchHits) {
			// 每一个coursePub
			CoursePub coursePub = new CoursePub();

			// 当前查询到的Document信息
			Map<String, Object> courseMap = searchHit.getSourceAsMap();
			// name
			String name = (String) courseMap.get("name");
			// 取出高亮中的name，如果存在就替换掉没有高亮的name。
			Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
			if (! Objects.isNull(highlightFields)) {
				HighlightField highlightField = highlightFields.get("name");
				if (! Objects.isNull(highlightField)) {
					Text[] fragments = highlightField.fragments();
					StringBuilder nameStr = new StringBuilder();
					for (Text fragment : fragments) {
						nameStr.append(fragment);
					}
					// 替换掉没有高亮效果的name
					name = nameStr.toString();
				}
			}
			coursePub.setName(name);
			// pic
			coursePub.setPic((String) courseMap.get("pic"));
			// price
			coursePub.setPrice((Double) courseMap.get("price"));
			// price_old
			coursePub.setPrice_old((Double) courseMap.get("price_old"));

			// 添加coursePub到集合中。
			coursePubList.add(coursePub);
		}

		// 封装分页信息
		QueryResult queryResult = new QueryResult();
		queryResult.setTotal(totalHits);
		queryResult.setList(coursePubList);

		return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
	}
}
