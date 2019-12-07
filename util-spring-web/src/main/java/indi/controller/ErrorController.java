package indi.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import indi.data.Result;
import indi.data.Results;

@Controller
public class ErrorController extends BasicController {

    /**
     * 对于允许返回json（请求头中accept 包含
     * [application/json;charset=UTF-8]）的请求，发生异常时，返回RestResult结构的json格式的字符串
     * 
     * @return
     */
    @GetMapping(path = "/error", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Result<?> handleJson() {
        Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String msg = (String) request.getAttribute("javax.servlet.error.message");
        return Results.error(status, msg);
    }

    /**
     * 对于只允许返回html的请求
     * 
     * <p>
     * 按理说，这种请求一般都是用户明确能感知到的请求（主要是浏览器地址栏跳转），因此统一当作404异常处理
     */
    @GetMapping(path = "/error", produces = "text/html")
    public String handleHtml() throws IOException {
//        Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
//        String msg = (String) request.getAttribute("javax.servlet.error.message");
//        response.sendRedirect("/404");
        return "404";
    }
}
