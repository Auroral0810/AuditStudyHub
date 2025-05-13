package com.auditStudyHub.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.auditStudyHub.config.OSSConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 *
 * @author AuditStudyHub
 */
@Slf4j
@Component
public class OSSUtils {

    private final OSS ossClient;
    private final OSSConfig ossConfig;

    @Autowired
    public OSSUtils(OSS ossClient, OSSConfig ossConfig) {
        this.ossClient = ossClient;
        this.ossConfig = ossConfig;
    }

    /**
     * 上传文件到OSS
     *
     * @param file 文件
     * @param folder 文件夹，例如：images/，如果不传则放在根目录
     * @return 文件访问URL
     * @throws IOException IO异常
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String objectName = generateObjectName(folder, fileExtension);
        
        try (InputStream inputStream = file.getInputStream()) {
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(fileExtension));
            metadata.setContentLength(file.getSize());
            
            // 上传文件
            ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream, metadata);
            
            // 返回访问URL
            return ossConfig.getUrlPrefix() + "/" + objectName;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw e;
        }
    }

    /**
     * 生成OSS对象名称
     *
     * @param folder 文件夹
     * @param fileExtension 文件扩展名
     * @return 对象名称
     */
    private String generateObjectName(String folder, String fileExtension) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String objectName = uuid + "." + fileExtension;
        
        if (folder != null && !folder.isEmpty()) {
            if (!folder.endsWith("/")) {
                folder += "/";
            }
            return folder + dateStr + "/" + objectName;
        }
        
        return dateStr + "/" + objectName;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 根据文件扩展名获取内容类型
     *
     * @param fileExtension 文件扩展名
     * @return 内容类型
     */
    private String getContentType(String fileExtension) {
        if (fileExtension == null) {
            return "application/octet-stream";
        }
        
        switch (fileExtension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 删除OSS上的文件
     *
     * @param objectName 对象名称
     * @return 是否成功
     */
    public boolean deleteFile(String objectName) {
        try {
            // 如果传入的是完整的URL，提取对象名称
            if (objectName.startsWith(ossConfig.getUrlPrefix())) {
                objectName = objectName.substring(ossConfig.getUrlPrefix().length() + 1);
            }
            
            ossClient.deleteObject(ossConfig.getBucketName(), objectName);
            return true;
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return false;
        }
    }

    /**
     * 生成文件下载地址（带有效期）
     *
     * @param objectName 对象名称
     * @param expireInSeconds 有效期（秒）
     * @return 下载地址
     */
    public String generateDownloadUrl(String objectName, int expireInSeconds) {
        try {
            // 如果传入的是完整的URL，提取对象名称
            if (objectName.startsWith(ossConfig.getUrlPrefix())) {
                objectName = objectName.substring(ossConfig.getUrlPrefix().length() + 1);
            }
            
            Date expiration = new Date(System.currentTimeMillis() + expireInSeconds * 1000L);
            URL url = ossClient.generatePresignedUrl(ossConfig.getBucketName(), objectName, expiration);
            return url.toString();
        } catch (Exception e) {
            log.error("生成下载链接失败", e);
            return null;
        }
    }
} 