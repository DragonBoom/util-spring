package indi.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseDao<T, ID> extends JpaRepository<T, ID>{

	WhereQuery<T, ID> createWhereQuery();
	
	interface WhereQuery<T, ID> extends Query<T, ID> {
	    
	    WhereQuery<T, ID> where(String p, Object v);
	}
	   
    PlainSqlQuery<T, ID> createPlainSqlQuery();
    
    interface PlainSqlQuery<T, ID> extends Query<T, ID> {
        
        PlainSqlQuery<T, ID> withSql(String sql);
        
        PlainSqlQuery<T, ID> withParam(String str, Object obj);
    }
    
    interface Query<T, ID> {
        
        T singleResult();
        
        T first();
        
        List<T> list();
        
        List<T> list(int pageNo, int pageSize);
    }
}
