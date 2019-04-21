package indi.controller;

import java.io.IOException;

import javax.servlet.ServletInputStream;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import com.google.common.io.ByteStreams;

import lombok.extern.slf4j.Slf4j;

/**
 * 本Controller用于处理上传文件的请求
 * 
 * @author DragonBoom
 *
 */
@Controller
@Slf4j
public class UploadController extends BasicController {
    private static final String API_PATH = "/api/file";

    /**
     * 接收上传请求 POST 限定 TODO 待完成
     * @throws IOException 
     */
    @PostMapping(API_PATH)
    public void upload() throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        byte[] bytes = ByteStreams.toByteArray(inputStream);
        response.getOutputStream().flush();// return empty
    }
}
