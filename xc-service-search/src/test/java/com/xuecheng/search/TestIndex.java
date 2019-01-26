package com.xuecheng.search;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestIndex {
	@Autowired
	RestHighLevelClient restClient;

	@Autowired
	RestClient client;

	/**
	 * 创建索引库,并添加映射。
	 * @throws Exception
	 */
	@Test
	public void testIndexCreate() throws Exception{
		// 创建索引请求对象
		CreateIndexRequest createIndexRequest = new CreateIndexRequest("xc_");
		// 设置索引参数,分页树1，副本数0.
		createIndexRequest.settings(Settings.builder().put("number_of_shards", 1).put("number_of_replicas", 0));
		// 添加映射
		createIndexRequest.mapping("doc"," {\n" +
				" \t\"properties\": {\n" +
				"            \"studymodel\":{\n" +
				"             \"type\":\"keyword\"\n" +
				"           },\n" +
				"            \"name\":{\n" +
				"             \"type\":\"keyword\"\n" +
				"           },\n" +
				"           \"description\": {\n" +
				"              \"type\": \"text\",\n" +
				"              \"analyzer\":\"ik_max_word\",\n" +
				"              \"search_analyzer\":\"ik_smart\"\n" +
				"           },\n" +
				"           \"pic\":{\n" +
				"             \"type\":\"text\",\n" +
				"             \"index\":false\n" +
				"           }\n" +
				" \t}\n" +
				"}", XContentType.JSON);
		// 客户端使用创建索引对象进行请求
		CreateIndexResponse createIndexResponse = restClient.indices().create(createIndexRequest);
		// 创建是否成功
		boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
		System.out.println(shardsAcknowledged);
	}

	/**
	 * 删除索引库
	 * @throws Exception
	 */
	@Test
	public void testIndexDelete() throws Exception{
		// 删除索引请求对象
		DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("xc_course");
		// 使用删除请求，可以获取到响应
		DeleteIndexResponse deleteIndexResponse = restClient.indices().delete(deleteIndexRequest);
		// 查看是否删除成功
		boolean acknowledged = deleteIndexResponse.isAcknowledged();
		System.out.println(acknowledged);
	}
}
