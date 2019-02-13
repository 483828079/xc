package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.controller.MediaUploadController;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaUploadService {
    private final static Logger LOGGER = LoggerFactory.getLogger(MediaUploadController.class);

    @Autowired
    MediaFileRepository mediaFileRepository;

    //上传文件根目录
    @Value("${xc-service-manage-media.upload-location}")
    String uploadPath;

    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        return null;
    }

    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        return null;
    }

    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        return null;
    }

    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        return null;
    }
}
