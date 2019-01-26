package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDocument {
	@Autowired
	RestHighLevelClient restClient;

	@Autowired
	RestClient client;

	/**
	 * 创建Document
	 * @throws Exception
	 */
	@Test
	public void testDocumentCreate() throws Exception{
		// 创建Document的请求对象，指定索引库名称，类型。
		IndexRequest indexRequest = new IndexRequest("xc_course", "doc");

		// 初始化要添加到索引库的Document数据。
		// 使用Map集合。key为field，value为filed的值。
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("name", "spring cloud实战");
		jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud 基础入门 3.实战Spring Boot 4.注册中心eureka。");
		jsonMap.put("studymodel", "201001");
		SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		jsonMap.put("timestamp", dateFormat.format(new Date()));
		jsonMap.put("price", 5.6f);

		// 设置Document内容
		indexRequest.source(jsonMap);

		// 通过client进行http请求
		IndexResponse indexResponse = restClient.index(indexRequest);

		// 获取响应结果内容,  CREATED：创建成功
		DocWriteResponse.Result result = indexResponse.getResult();
		System.out.println(result);
	}

	/**
	 * 通过id查询Document
	 * @throws Exception
	 */
	@Test
	public void testDocumentQuery() throws Exception{
		// 查询Document的请求对象
		GetRequest getRequest = new GetRequest("xc_course", "doc", "dKzmhGgBSM93s6OpZsiW");

		// 调用查询请求对象获取响应
		GetResponse getResponse = restClient.get(getRequest);

		// 是否查询到内容
		boolean exists = getResponse.isExists();

		// filed filedValue 作为 Map的 key value
		Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
		System.out.println(sourceAsMap);
	}

	/**
	 * 通过Document的id更新Document(被指定的Field的value会被替换)
	 * @throws Exception
	 */
	@Test
	public void testDocumentUpdate() throws Exception{
		// 更新Document的请求对象, 通过id指定要更新的document
		UpdateRequest updateRequest = new UpdateRequest("xc_course", "doc", "dKzmhGgBSM93s6OpZsiW");

		// 设置要更新的filed
		Map<String, Object> map = new HashMap();
		map.put("name", "spring cloud实战");
		updateRequest.doc(map);

		// 调用更新请求,获取响应对象
		UpdateResponse updateResponse = restClient.update(updateRequest);

		// 获取更新响应状态
		RestStatus status = updateResponse.status();

		// OK, 更新执行成功
		System.out.println(status);
	}

	/**
	 * 按照id删除Document
	 * (只能用id，要想使用别的field来删除
	 * 可以先进行查询再通过查询结果Document的id删除。)
	 * @throws Exception
	 */
	@Test
	public void testDocumentDelete() throws Exception{
		// 删除Document的请求对象
		DeleteRequest deleteRequest = new DeleteRequest("xc_course", "doc", "dKzmhGgBSM93s6OpZsiW");

		// 调用删除的请求
		DeleteResponse deleteResponse = restClient.delete(deleteRequest);

		// 获取响应结果内容
		DocWriteResponse.Result result = deleteResponse.getResult();

		// DELETED 删除成功
		System.out.println(result);
	}
}
