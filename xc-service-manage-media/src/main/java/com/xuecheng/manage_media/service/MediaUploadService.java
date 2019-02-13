package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.controller.MediaUploadController;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Optional;

@Service
public class MediaUploadService {
    private final static Logger LOGGER = LoggerFactory.getLogger(MediaUploadController.class);

    @Autowired
    MediaFileRepository mediaFileRepository;

    //上传文件根目录
    @Value("${xc-service-manage-media.upload-location}")
    String uploadPath;

    /**
     * 上传文件前进行调用，判断文件是否存在，创建文件目录。
     * @param fileMd5 上传文件的md5
     * @param fileName 上传文件的文件名称
     * @param fileSize 上传文件的大小
     * @param mimetype 上传文件的mimeType
     * @param fileExt 上传文件的拓展名
     * @return 检查文件是否存在，存在抛出异常，不存在创建文件目录返回正确状态
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        // 判断文件是否存在

        // 文件是否存在磁盘上

        // 获取当前文件的路径
        String filePath = getFilePath(fileMd5, fileExt);
        // 创建文件对象
        File file = new File(filePath);

        // 文件信息是否存在于mongodb中(上传文件成功后会将文件信息存储在mongodb)
        // 文件md5作为id
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(fileMd5);

        // 如果文件存在并且文件信息存在mongodb中抛出异常
        if (file.exists() && mediaFileOptional.isPresent()) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }

        // 创建文件目录
        boolean fileFold = createFileFolder(fileMd5);

        //上传文件目录创建失败
        if(!fileFold){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_CREATEFOLDER_FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 检查要上传的分块文件是否已经存在(如果已经存在该分块可以不用再上传)
     * @param fileMd5 文件的md5
     * @param chunk 分块的索引
     * @param chunkSize 分块的大小
     * @return 如果存在返回失败信息，如果不存在返回成功信息
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        // 获取文件对应的分块所在的目录
        String chunkfileFolderPath = getChunkFileFolderPath(fileMd5);
        // 分块文件的路径(分块所在目录 + 分块的索引(分块的名称))
        String chunkFilePath = chunkfileFolderPath + chunk;
        // 判断分块文件是否存在
        // 如果要上传该块需要fileExist为false。 不存在或者大小不相等才进行上传。
        File chunkFile = new File(chunkFilePath);
        if (chunkFile.exists() && chunkSize.equals(chunkFile.length())) {
            // 文件存在并且大小相同不进行上传
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,true);
        } else {
           return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK,true);
        }
    }

    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        return null;
    }

    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        return null;
    }

    /**
     * 根据文件md5判断文件目录是否存在，不存在创建。
     * @param fileMd5 文件的md5
     * @return 执行完毕返回true，创建失败返回false
     */
    private boolean createFileFolder(String fileMd5) {
        String fileFolderPath = getFileFolderPath(fileMd5);
        File fileFolder = new File(fileFolderPath);
        // 文件夹不存在创建目录
        if (!fileFolder.exists()) {
            //创建文件夹
            boolean mkdirs = fileFolder.mkdirs();
            return mkdirs;
        }
        return true;
    }

    /**
     * 根据文件md5和文件拓展名获取文件目录
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @param fileMd5
     * @param fileExt
     * @return 文件目录
     */
    private String getFilePath(String fileMd5, String fileExt) {
        String filePath = uploadPath+fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + "." + fileExt;
        return filePath;
    }

    /**
     * 根据文件的md5得到文件所在文件夹
     * @param fileMd5
     * @return
     */
    private String getFileFolderPath(String fileMd5){
        String fileFolderPath = uploadPath+ fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" ;
        return fileFolderPath;
    }

    /**
     * 获取文件的分块目录(文件所在目录+chunks)
     * @param fileMd5 文件的md5
     * @return 文件分块所在目录
     */
    private String getChunkFileFolderPath(String fileMd5) {
        String fileChunkFolderPath = getFileFolderPath(fileMd5) +"/" + "chunks" + "/";
        return fileChunkFolderPath;
    }
}
