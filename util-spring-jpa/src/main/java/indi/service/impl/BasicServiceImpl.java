package indi.service.impl;


import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

import indi.dao.BaseDao;
import indi.data.Result;
import indi.data.Results;
import indi.service.BasicService;

public abstract class BasicServiceImpl<T, ID> implements BasicService<T, ID> {
	
	public Result<T> get(ID id) {
	    return Results.fromOptional(getDao().findById(id), "根据ID找不到实体");
	}
	
	public abstract BaseDao<T, ID> getDao();

	@Autowired
	protected EntityManager em;
}
