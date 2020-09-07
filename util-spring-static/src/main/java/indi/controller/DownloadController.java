package indi.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import indi.conf.FileProperties;
import indi.data.Result;
import indi.data.Results;
import indi.data.dto.UploadFileDTO;
import indi.service.FileService;

/**
 * 本Controller用于处理文件下载的请求。将上传、请求进行拆分，确保在实现了较多接口后不至于混乱
 * 
 * @author DragonBoom
 *
 */
@Controller
public class DownloadController extends BasicController {
    private static final String API_PATH = "/api/file/download";

    /**
     * 按特殊协议分块下载
     * 
     * @throws IOException
     */
    @GetMapping(API_PATH + "/part")
    public Result<?> downloadPart(MultipartFile file) throws IOException {
        // TODO:
        return Results.error("尚未实现");
    }
    
    private Path getPath(UploadFileDTO dto, String uploadPath) {
        return Paths.get(uploadPath, dto.getName());
    }
    
    @Autowired
    private FileProperties fileProperties;

    @Autowired
    private FileService fileService;
}
