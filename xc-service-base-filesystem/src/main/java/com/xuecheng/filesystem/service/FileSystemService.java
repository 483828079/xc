package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileSystemService {
	@Autowired
	FileSystemRepository fileSystemRepository;

	@Value("${xuecheng.fastdfs.tracker_servers}")
	String tracker_servers;
	@Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
	int connect_timeout_in_seconds;
	@Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
	int network_timeout_in_seconds;
	@Value("${xuecheng.fastdfs.charset}")
	String charset;

	/**
	 * 上传文件到fastDFS，保存文件信息到数据库。
	 * @param file 文件
	 * @param filetag 文件标签，哪个系统的文件
	 * @param businesskey 业务key
	 * @param metadata 元数据
	 * @return
	 */
	public UploadFileResult upload(MultipartFile file,
								   String filetag,
								   String businesskey,
								   String metadata){
		// 上传文件到fastDFS

		// 如果文件不存在
		if(file == null){
			ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
		}

		// 上传文件到fastDFS，获取上传文件的fileId。
		String fileId = fdfs_upload(file);

		// 如果id不存在
		if (StringUtils.isEmpty(fileId)) {
			ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
		}

		// 封装上传的文件信息
		FileSystem fileSystem = new FileSystem();
		fileSystem.setFileId(fileId);
		fileSystem.setFiletag(filetag);
		fileSystem.setBusinesskey(businesskey);

		// metadata为json格式，需要转换为Map.
		if (StringUtils.isNotEmpty(metadata)) {
			try {
				fileSystem.setMetadata(JSON.parseObject(metadata, Map.class));
			} catch (ClassCastException e) {
				ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_METAERROR);
			}
		}

		// 实例化fileSystem
		fileSystemRepository.save(fileSystem);

		// 响应信息
		return new UploadFileResult(CommonCode.SUCCESS, fileSystem);
	}

	/**
	 * 上传文件到fastDFS服务器。
	 * @param file
	 * @return
	 */
	private String fdfs_upload(MultipartFile file) {
		try {
			// 文件的数据
			byte[] fileBytes = file.getBytes();
			// 文件名称
			String fileName = file.getOriginalFilename();
			// 文件拓展名
			String ext = fileName.substring(fileName.lastIndexOf(".") + 1);

			// 上传文件

			// 初始化配置
			initFdfsConfig();
			TrackerClient trackerClient = new TrackerClient();
			// 连接tracker
			TrackerServer trackerServer = trackerClient.getConnection();
			// 获取storageServer
			StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
			// 创建storageClient
			StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);

			return storageClient1.upload_appender_file1(fileBytes, ext, null);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 加载fastDFS的配置
	 */
	private void initFdfsConfig(){
		try {
			ClientGlobal.initByTrackers(tracker_servers);
			ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
			ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
			ClientGlobal.setG_charset(charset);
		} catch (Exception e) {
			e.printStackTrace();
			//初始化文件系统出错
			ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
		}
	}
}
