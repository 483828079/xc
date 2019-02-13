package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.controller.MediaUploadController;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {
    private final static Logger LOGGER = LoggerFactory.getLogger(MediaUploadController.class);

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("routingkey_media_video")
    String routingkey_media_video;
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
     * @return 分块文件存在并且和要上传分块大小相同返回fileExist为true(进行上传)。
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
            // fileExist为false 进行上传
           return new CheckChunkResult(CommonCode.SUCCESS,false);
        }
    }

    /**
     * 分块文件上传
     * @param file 要上传的分块文件
     * @param fileMd5 文件的md5
     * @param chunk 当前块的索引(文件名)
     * @return 上传成功返回成功状态，上传失败返回失败状态
     */
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        if(file == null){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_ISNULL);
        }

        // 创建块文件目录
        boolean fileFold = createChunkFileFolder(fileMd5);
        // 块文件
        File chunkfile = new File(getChunkFileFolderPath(fileMd5) + chunk);
        InputStream is = null;
        OutputStream os = null;
        try {
            is = file.getInputStream();
            os = new FileOutputStream(chunkfile);
            // 写入快文件到磁盘
            IOUtils.copy(is, os);
        } catch (Exception e) {
            LOGGER.error("upload chunk file fail:{}",e.getMessage());
            ExceptionCast.cast(MediaCode.CHUNK_FILE_UPLOAD_FAIL);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 上传成功，返回成功状态
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 上传成功后合并上传的块文件，将文件信息实例化到mongodb
     * @param fileMd5 上传文件的md5
     * @param fileName 上传文件的名称
     * @param fileSize 上传文件的大小
     * @param mimetype 上传文件的mime类型
     * @param fileExt 上传文件的拓展名
     * @return 合并成功返回成功状态，合并失败返回失败状态。
     */
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //获取块文件的路径
        String chunkfileFolderPath = getChunkFileFolderPath(fileMd5);
        File chunkfileFolder = new File(chunkfileFolderPath);

        // 如果块文件目录不存在进行创建
        if(!chunkfileFolder.exists()){
            chunkfileFolder.mkdirs();
        }

        // 合并文件
        File mergeFile = new File(getFilePath(fileMd5,fileExt));

        // 创建合并文件
        // 合并文件存在先删除再创建
        if(mergeFile.exists()){
            mergeFile.delete();
        }

        boolean newFile = false;

        try {
            // 创建合并的文件
            newFile = mergeFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("mergechunks..create mergeFile fail:{}",e.getMessage());
        }

        // 如果创建合并文件失败返回错误状态
        if(!newFile){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CREATEFAIL);
        }

        // 获取块文件，此列表是已经排好序的列表
        List<File> chunkFiles = getChunkFiles(chunkfileFolder);

        // 合并文件
        mergeFile = mergeFile(mergeFile, chunkFiles);
        // 合并后的文件为null，返回合并失败状态
        if(mergeFile == null){
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        // 校验文件,合并后的文件是否和要上传的文件md5相同
        // 如果相同说明是同一个文件，表明合并成功。否则合并失败。
        boolean checkResult = this.checkFileMd5(mergeFile, fileMd5);

        // 合并失败返回合并失败状态
        if(!checkResult){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        // 保存上传文件信息到mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        mediaFile.setFileOriginalName(fileName);
        // 文件路径保存相对路径
        mediaFile.setFilePath(getFileFolderRelativePath(fileMd5,fileExt));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);

        //状态为上传成功
        mediaFile.setFileStatus("301002");
        MediaFile save = mediaFileRepository.save(mediaFile);

        // 上传成功后发送消息到队列
        String mediaId = mediaFile.getFileId();
        //向MQ发送视频处理消息
        sendProcessVideoMsg(mediaId);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 发送mediaId到消息队列
     * @param mediaId
     */
    private ResponseResult sendProcessVideoMsg(String mediaId) {
        // 判断mediaId对应的集合是否存在
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(mediaId);
        if (!mediaFileOptional.isPresent()) {
            return new ResponseResult(CommonCode.FAIL);
        }

        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("mediaId", mediaId);
        // 要发送的消息
        String msgStr = JSON.toJSONString(msgMap);

        // 发送消息到消息队列
        try {
            // 发送消息到交换机，指定routeKey，交换机会将消息发送到routeKey对应的队列
            this.rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkey_media_video, msgStr);
            LOGGER.info("send media process task msg:{}",msgStr);
        }catch (Exception e){
            e.printStackTrace();
            LOGGER.info("send media process task error,msg is:{},error:{}",msgStr,e.getMessage());
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 获取文件所在相对路径(除去磁盘位置)
     * @param fileMd5 文件的md5
     * @param fileExt 文件的拓展名
     * @return 文件的相对路径
     */
    private String getFileFolderRelativePath(String fileMd5, String fileExt) {
        String filePath = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
        return filePath;
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

    /**
     * 如果块文件夹不存在进行创建
     * @param fileMd5 文件md5
     * @return 创建失败返回false
     */
    private boolean createChunkFileFolder(String fileMd5) {
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()) {
            boolean isMkdirs = chunkFileFolder.mkdirs();
            return isMkdirs;
        }
        return true;
    }

    /**
     * 获取文件夹下的所有文件，并且将文件按照名称升序排序。
     * @param chunkfileFolder 文件夹的文件对象
     * @return 排序好的所有文件的集合
     */
    private List<File> getChunkFiles(File chunkfileFolder) {
        List<File> chunkFileList = Arrays.asList(chunkfileFolder.listFiles());
        Collections.sort(chunkFileList, Comparator.comparingInt(o -> Integer.parseInt(o.getName())));
        return chunkFileList;
    }

    /**
     * 合并chunkFiles集合中的所有文件到mergeFile
     * @param mergeFile 合并文件
     * @param chunkFiles 要合并的所有文件
     * @return 合并成功后的文件
     */
    private File mergeFile(File mergeFile, List<File> chunkFiles) {
        try {
            //创建写文件对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
            //遍历分块文件开始合并
            //读取文件缓冲区
            byte[] b = new byte[1024];
            for(File chunkFile:chunkFiles){
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                //读取分块文件
                while((len = raf_read.read(b))!=-1){
                    //向合并文件中写数据
                    raf_write.write(b,0,len);
                }
                raf_read.close();
            }
            raf_write.close();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("merge file error:{}",e.getMessage());
            return null;
        }
        return mergeFile;
    }

    /**
     * 判断mergeFile的md5是否等于fileMd5
     * @param mergeFile 合并文件
     * @param fileMd5 要上传文件的md5
     * @return 如果md5相同返回true，反之为false。
     */
    private boolean checkFileMd5(File mergeFile, String fileMd5) {
        if(mergeFile == null || StringUtils.isEmpty(fileMd5)){
            return false;
        }

        //进行md5校验
        FileInputStream mergeFileInputStream = null;

        try {
            // 获取文件的md5
            mergeFileInputStream = new FileInputStream(mergeFile);
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileInputStream);
            // 比较md5
            return mergeFileMd5.equalsIgnoreCase(fileMd5);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("checkFileMd5 error,file is:{},md5 is:{}",mergeFile.getAbsoluteFile(), fileMd5);
        } finally {
            if (! Objects.isNull(mergeFileInputStream)) {
                try {
                    mergeFileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
