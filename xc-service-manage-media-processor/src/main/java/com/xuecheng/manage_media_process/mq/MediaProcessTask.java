package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class MediaProcessTask {
	private static final Logger LOGGER = LoggerFactory.getLogger(MediaProcessTask.class);

	//ffmpeg绝对路径
	@Value("${xc-service-manage-media.ffmpeg-path}")
	String ffmpeg_path;

	//上传文件根目录
	@Value("${xc-service-manage-media.video-location}")
	String serverPath;

	@Autowired
	MediaFileRepository mediaFileRepository;

	/**
	 * 监听消息队列，对消息队列中mediaId对应的文件格式进行处理
	 * @param msg 消息队列中的消息
	 * @throws IOException
	 */
	@RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}")
	public void receiveMediaProcessTask(String msg) throws IOException {
		Map<String, String> msgMap = JSON.parseObject(msg, Map.class);
		// 消息队列中的mediaId
		String mediaId = msgMap.get("mediaId");
		// 消息队列中的mediaId为null, 结束该方法。
		if (Objects.isNull(mediaId)) {
			return;
		}

		// 判断该mediaId对应的集合是否存在
		Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(mediaId);
		if (! mediaFileOptional.isPresent()) {
			return;
		}

		// 媒资文件对象
		MediaFile mediaFile = mediaFileOptional.get();

		// 对文件处理(理论上可以处理大部分格式的视频，但是这里只做avi视频的处理)

		// 文件的格式(拓展名)
		String fileType = mediaFile.getFileType();

		// 设置文件的状态
		if (fileType == null || !fileType.equals("avi")) {
			mediaFile.setProcessStatus("303004");//处理状态为无需处理
			mediaFileRepository.save(mediaFile);
			return ;
		} else {
			// 如果是avi格式的文件，对其进行处理，处理前先将状态设置为未处理。
			// 如果期间处理失败就是未处理状态了(只有开始处理该状态才存在，否在就是为null)。
			mediaFile.setProcessStatus("303001");//处理状态为未处理
			mediaFileRepository.save(mediaFile);
		}

		// 开始对文件进行处理
		// 生成mp4

		// 文件的路径 = 文件所在磁盘路径 + 文件相对路径 + 文件名称
		String video_path = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();
		// 转换的文件名称 = md5+后缀(avi格式的文件也是md5的名称)
		String mp4_name = mediaFile.getFileId()+".mp4";
		// 转换文件存放的路径
		String mp4folder_path = serverPath + mediaFile.getFilePath();
		// 使用工具类对文件进行转换，将avi转换为mp4并存放于同级目录下。
		Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4folder_path);
		// 工具类，如果转换成功返回success，否则返回错误信息(调用ffmp给的错误信息)
		// 工具类判断是否为success的原理是，判断转换后的文件是否和转化前的文件视频时间相同。
		String result = videoUtil.generateMp4();

		// 如果转换失败写入错误信息设置处理状态并结束处理方法
		if(result == null || !result.equals("success")){
			//操作失败写入处理日志
			mediaFile.setProcessStatus("303003");//处理状态为处理失败
			MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
			mediaFileProcess_m3u8.setErrormsg(result);
			mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
			mediaFileRepository.save(mediaFile);
			return ;
		}

		// 生成m3u8
		// 生成的mp4文件地址
		video_path = serverPath + mediaFile.getFilePath() + mp4_name;
		// m3u8文件名称 md5 + .m3u8
		String m3u8_name = mediaFile.getFileId()+".m3u8";
		// m3u8的文件路径
		String m3u8folder_path = serverPath + mediaFile.getFilePath()+"hls/";
		HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path,video_path,m3u8_name,m3u8folder_path);
		// 生成m3u8文件
		// 如果生成成功返回success，失败返回错误信息。
		// 判断生成成功的原理：视频长度是否相同，m3u8中的列表是否正确
		result = hlsVideoUtil.generateM3u8();
		if(result == null || !result.equals("success")){
			//操作失败写入处理日志
			mediaFile.setProcessStatus("303003");//处理状态为处理失败
			MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
			mediaFileProcess_m3u8.setErrormsg(result);
			mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
			mediaFileRepository.save(mediaFile);
			return ;
		}

		// 成功改变mongodb中的文件信息
		//获取m3u8列表
		List<String> ts_list = hlsVideoUtil.get_ts_list();
		//更新处理状态为成功
		mediaFile.setProcessStatus("303002");//处理状态为处理成功
		MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
		mediaFileProcess_m3u8.setTslist(ts_list);
		mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
		//m3u8文件url
		mediaFile.setFileUrl(mediaFile.getFilePath()+"hls/"+m3u8_name);
		mediaFileRepository.save(mediaFile);

	}
}
