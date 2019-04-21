package indi.dao;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.util.Pair;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Streams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseDaoImpl<T, ID> extends SimpleJpaRepository<T, ID> implements BaseDao<T, ID> {

	protected EntityManager em;
	protected Class<?> domainClass;

	public BaseDaoImpl(Class<T> domainClass, EntityManager entityManager) {
		super(domainClass, entityManager);
		this.em = entityManager;// 父类没有entityManager的getter To^
		this.domainClass = domainClass;
		log.info("Create BaseDaoImpl for {}", domainClass);
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public T nativeSingleQuery(String sql) {
		// TODO Auto-generated method stub
		return (T) em.createNativeQuery(sql, super.getDomainClass()).getSingleResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> nativeListQuery(String sql) {
		// TODO Auto-generated method stub
		return em.createNativeQuery(sql, super.getDomainClass()).getResultList();
	}

	@Override
	public Query createQuery() {
		return new Query();
	}

	public class Query implements BaseDao.Query<T, ID> {
		private static final String SELECT_ALL_PREFIX = "select * from ";
		
		private List<Pair<String, Object>> params;
		private String sql;
//		private int pageNo;
//		private int pageSize;
		

		protected void init() {
			params = new ArrayList<>();
		}
		
		public Query() {
			init();
		}

		@Override
		public indi.dao.BaseDao.Query<T, ID> with(String p, Object v) {
			if (v == null) {
				return this;
			}
			Pair<String,Object> pair = Pair.of(p, v);
			params.add(pair);
			return this;
		}
		
		/**
		 * 生成Sql语句
		 * 
		 * @return
		 */
		protected String buildSql() {
			// 获取表名
			String tableName = getTableName(domainClass);
			
			StringBuilder sb = new StringBuilder(SELECT_ALL_PREFIX).append(tableName);
			
			if (params.size() > 0) {
				sb.append(" where ");
				// 设置参数 result: a = ?0 and b = ?1;
				/*
				 * 使用到guava提供的工具类的mapWithIndex
				 */
				String wherePart = Streams
						.mapWithIndex(params.stream(), (pair, i) -> {
							String p = pair.getFirst();
							StringBuilder sb2 = new StringBuilder(p).append(" = ").append("?").append(i);
							return sb2.toString();
						})
						.collect(Collectors.joining(" and "));
				
				sb.append(wherePart);
			}
					
			log.debug("Build sql: {}", sb);
			return sb.toString();
		}
		
		/**
		 * 获取实体类对应的表名
		 * <p>表名取注解的name字段，若不存在则取类名（转化为下划线式写法)
		 * 
		 * @param domainClass
		 * @return
		 */
		protected String getTableName(Class<?> domainClass) {
			String name = null;
			Annotation[] annotations = domainClass.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof javax.persistence.Entity) {
					name = ((javax.persistence.Entity) annotation).name();
				}
			}
			if (name == null) {
				// 将类名转化为下划线式写法
				String simpleClassName = domainClass.getSimpleName();
				name = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, simpleClassName);
			}
			return name;
		}

		@Override
		public T singleResult() {
			List<T> list = list();
			int size = list.size();
			if (size > 1) {
				log.error("{}", sql);
				String error = "sql`s result set contains more than 1 result";
				log.error(error);
				throw new RuntimeException(error);
			} else if (size == 1) {
				return list.get(0);
			} else {
				return null;
			}
		}
		
		@Override
		public T first() {
			TypedQuery<T> typedQuery = buildTypedQuery();
			return typedQuery.getSingleResult();
		}
		
		@Override
		public List<T> list() {
			TypedQuery<T> typedQuery = buildTypedQuery();
			return typedQuery.getResultList();
		}

		@Override
		public List<T> list(int pageNo, int pageSize) {
			TypedQuery<T> typedQuery = buildTypedQuery();
			return typedQuery
					.setFirstResult(pageNo * pageSize)
					.setMaxResults(pageSize)
					.getResultList();
		}
		
		protected TypedQuery<T> buildTypedQuery() {
			sql = buildSql();
			TypedQuery<T> query = (TypedQuery<T>) em.createNativeQuery(sql, domainClass);
			for (int i = 0; i < params.size(); i++) {
				Pair<String, Object> pair = params.get(i);
				Object v = pair.getSecond();
				query.setParameter(i, v);
			}
			return query;
		}

	}
}
