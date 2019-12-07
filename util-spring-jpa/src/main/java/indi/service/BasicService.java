package indi.service;

import indi.dao.BaseDao;
import indi.data.Result;

public interface BasicService<T, ID> {
    
    Result<T> get(ID id);
    
    BaseDao<T, ID> getDao();

}
