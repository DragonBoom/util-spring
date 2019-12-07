package indi.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import indi.conf.FileProperties;
import indi.data.Result;
import indi.data.dto.UploadFileDTO;
import indi.service.FileService;

/**
 * 本Controller用于处理上传文件的请求
 * 
 * @author DragonBoom
 *
 */
@Controller
public class UploadController extends BasicController {
    private static final String API_PATH = "/api/file'";

    /**
     * 接收上传请求
     * 
     * @throws IOException
     */
    @PostMapping(API_PATH)
    public Result<?> upload(MultipartFile file) throws IOException {
        UploadFileDTO uploadFileDTO = UploadFileDTO.builder()
        .name(file.getName())
        .content(file.getBytes())
        .contentType(file.getContentType())
        .build();
        
        String uploadPath = fileProperties.getUploadPath();
        uploadFileDTO.setPath(getPath(uploadFileDTO, uploadPath));

        return fileService.upload(uploadFileDTO);
    }
    
    private Path getPath(UploadFileDTO dto, String uploadPath) {
        return Paths.get(uploadPath, dto.getName());
    }
    
    @Autowired
    private FileProperties fileProperties;

    @Autowired
    private FileService fileService;
}
