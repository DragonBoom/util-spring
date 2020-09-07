package indi.service;

import java.io.IOException;
import java.nio.file.Path;

import javax.servlet.http.HttpServletResponse;

import indi.data.Result;
import indi.data.dto.UploadFileDTO;
import indi.data.entity.FileDO;

public interface FileService  extends BasicService<FileDO, Long> {

    /**
     * 持久化磁盘文件
     * 
     * @param path
     * @return
     */
    Result<FileDO> persist(Path path);
 
    /**
     * 以标准格式上传文件
     * 
     * @param dto
     * @return
     */
    Result<FileDO> upload(UploadFileDTO dto);
    
    /**
     * 分块地返回文件
     * 
     * @return
     * @throws IOException 
     */
    Result<?> responseFilePart(Path path, Long partSize, Integer partIndex, HttpServletResponse response) throws IOException;
    
    /**
     * 分块下载，支持继续下载
     * 
     * @param url 分块传输下载url
     * @param partSize 分块大小，建议网络环境越差该值越小；但值过小时可能会导致分块过多，进一步导致分块信息超过上限...
     * @param targetPath 下载保存路径。若文件已存在，将覆盖
     * @return
     * @throws Exception 
     */
    Result<?> downloadFilePart(String url, Long partSize, Path targetPath) throws Exception;
}
