package indi.service.impl;


import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

public class BasicServiceImpl {
	
	public <T> T getOne() {
		return null;
	}

	@Autowired
	protected EntityManager entityManager;
}
