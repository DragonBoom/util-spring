package indi.service;

import java.nio.file.Path;

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
    
    Result<FileDO> upload(UploadFileDTO dto);
}
