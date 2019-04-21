package indi.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * 负责管理静态资源的Controller
 * 
 * @author DragonBoom
 *
 */
@Controller
@Slf4j
public class StaticController extends BasicController {
	private static final String PIC_API_PATH = "/pic";
	private static final String ERROR_API_PATH = "/404";

	/**
	 * welcome
	 */
	@GetMapping(PIC_API_PATH)
	public String getPic() {
		return "/pic/welcome.png";
	}
	
	@GetMapping(ERROR_API_PATH)
	public String getErrorPage() {
	    return "/error/404.html";
	}
	
}
