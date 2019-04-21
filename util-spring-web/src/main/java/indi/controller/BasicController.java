package indi.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BasicController{

	@Autowired
	protected HttpServletRequest request;
	@Autowired
	protected HttpServletResponse response;
}
