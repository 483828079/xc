package com.xuecheng.test.fastdfs;

import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {
	@Test
	public void testUpload() throws Exception{
		ClientGlobal.initByProperties("config/fastdfs-client.properties");
		// 创建tracker
		TrackerClient trackerClient = new TrackerClient();
		// 连接tracker
		TrackerServer trackerServer = trackerClient.getConnection();
		// 获取storageServer
		StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
		// 创建storageClient
		StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
		// 上传文件, 获取fileId路径和文件名称
		String id = storageClient1.upload_appender_file1("E:/bg.jpg", "jpg", null);
		System.out.println(id);
	}

	@Test
	public void testDowenLoad() throws Exception{
		ClientGlobal.initByProperties("config/fastdfs-client.properties");
		// 创建tracker
		TrackerClient trackerClient = new TrackerClient();
		// 连接tracker
		TrackerServer trackerServer = trackerClient.getConnection();
		// 创建storageClient
		StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
		byte[] bytes = storageClient1.download_file1("group1/M00/00/01/wKgZmVxFpMqEZTsSAAAAAKi8wpM346.jpg");
		FileOutputStream fileOutputStream = new FileOutputStream(new File("E:/a.jpg"));
		fileOutputStream.write(bytes);
	}
}
