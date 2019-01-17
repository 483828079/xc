package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GridFsTest {
	@Autowired
	private GridFsTemplate gridFsTemplate;

	@Autowired
	private GridFSBucket gridFSBucket;

	/**
	 * 保存文件到mongodb
	 * mongodb提供了存储的方案 gridFs
	 * 有两张表，fs.files fs.chunks。
	 * fs.files用于存储文件的元数据信息（文件名称、块大小、上传时间等信息）。
	 * fs.chunks用于存储文件的二进制数据
	 * 在GridFS存储文件是将文件分块存储，文件会按照256KB的大小分割成多个块进行存储
	 *
	 * 存储成功后会获取一个id，此id就是fs.files的id。
	 * 也是fs.chunks的filed files_id的值。
	 * @throws Exception
	 */
	@Test
	public void testStore() throws Exception{
		InputStream inputStream = new FileInputStream("D:/index_banner.ftl");
		ObjectId id = gridFsTemplate.store(inputStream, "index_banner");
		System.out.println(id);
	}

	/**
	 * 获取mongodb中存储的文件(通过fs.files的id)
	 * @throws Exception
	 */
	@Test
	public void queryFile() throws Exception{
		String fileId = "5c3ee71f0af39539b8c90f62";
		// 根据id获取文件
		GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
		// 打开下载流
		GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
		//创建gridFsResource，用于获取流对象
		GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
		//获取流中的数据(也就是同过这种方式获取mongodb中存储的模板。)
		String s = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
		System.out.println(s);
	}

	//删除文件
	@Test
	public void testDelFile() throws IOException {
		//根据文件id删除fs.files和fs.chunks中的记录
		gridFsTemplate.delete(Query.query(Criteria.where("_id").is("5b32480ed3a022164c4d2f92")));
	}

	/***
	 * cms_page中的每一个page对应着一个templateId和一个dataUrl
	 * 生成模板需要data和template
	 * data：请求dataUrl获取
	 * template: 就是templateId对应的cms_template中的filed templateFileId.
	 * 	可以通templateFileId从 fs.files fs.chunks中获取模板.
	 * 	然后生成对应页面。
	 */

}
