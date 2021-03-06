package com.xuecheng.manage_media.controller;

import com.xuecheng.api.media.MediaUploadControllerApi;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/media/upload")
public class MediaUploadController implements MediaUploadControllerApi {
    @Autowired
    MediaUploadService mediaUploadService;

    /**
     * 上传文件前进行调用，判断文件是否存在，创建文件目录。
     * @param fileMd5 上传文件的md5
     * @param fileName 上传文件的文件名称
     * @param fileSize 上传文件的大小
     * @param mimetype 上传文件的mimeType
     * @param fileExt 上传文件的拓展名
     * @return 检查文件是否存在，存在抛出异常，不存在创建文件目录返回正确状态
     */
    @PostMapping("/register")
    public ResponseResult register(@RequestParam("fileMd5") String fileMd5, @RequestParam("fileName") String fileName, @RequestParam("fileSize") Long fileSize, @RequestParam("mimetype") String mimetype, @RequestParam("fileExt") String fileExt) {
        return mediaUploadService.register(fileMd5,fileName,fileSize,mimetype,fileExt);
    }

    /**
     * 检查要上传的分块文件是否已经存在(如果已经存在该分块可以不用再上传)
     * @param fileMd5 文件的md5
     * @param chunk 分块的索引
     * @param chunkSize 分块的大小
     * @return 分块文件存在并且和要上传分块大小相同返回fileExist为true(进行上传)。
     */
    @PostMapping("/checkchunk")
    public CheckChunkResult checkchunk(@RequestParam("fileMd5") String fileMd5, @RequestParam("chunk") Integer chunk, @RequestParam("chunkSize") Integer chunkSize) {
        return mediaUploadService.checkchunk(fileMd5,chunk,chunkSize);
    }

    /**
     * 分块文件上传
     * @param file 要上传的分块文件
     * @param fileMd5 文件的md5
     * @param chunk 当前块的索引(文件名)
     * @return 上传成功返回成功状态，上传失败返回失败状态
     */
    @PostMapping("/uploadchunk")
    public ResponseResult uploadchunk(@RequestParam("file") MultipartFile file, @RequestParam("chunk") Integer chunk, @RequestParam("fileMd5") String fileMd5) {
        return mediaUploadService.uploadchunk(file,fileMd5,chunk);
    }

    /**
     * 上传成功后合并上传的块文件
     * @param fileMd5 上传文件的md5
     * @param fileName 上传文件的名称
     * @param fileSize 上传文件的大小
     * @param mimetype 上传文件的mime类型
     * @param fileExt 上传文件的拓展名
     * @return 合并成功返回成功状态，合并失败返回失败状态。
     */
    @PostMapping("/mergechunks")
    public ResponseResult mergechunks(@RequestParam("fileMd5") String fileMd5, @RequestParam("fileName") String fileName, @RequestParam("fileSize") Long fileSize, @RequestParam("mimetype") String mimetype, @RequestParam("fileExt") String fileExt) {
        return mediaUploadService.mergechunks(fileMd5,fileName,fileSize,mimetype,fileExt);
    }
}