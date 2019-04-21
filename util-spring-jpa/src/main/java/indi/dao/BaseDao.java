package indi.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseDao<T, ID> extends JpaRepository<T, ID>{
		
	T nativeSingleQuery(String sql);
	
	List<T> nativeListQuery(String sql);

	Query<T, ID> createQuery();
	
	interface Query<T, ID> {
		
		Query<T, ID> with(String p, Object v);
		
		T singleResult();
		
		T first();
		
		List<T> list();
		
		List<T> list(int pageNo, int pageSize);
	}
}
