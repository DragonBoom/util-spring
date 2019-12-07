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

import indi.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseDaoImpl<T, ID> extends SimpleJpaRepository<T, ID> implements BaseDao<T, ID> {

	protected EntityManager em;
	protected Class<T> domainClass;

	public BaseDaoImpl(Class<T> domainClass, EntityManager entityManager) {
		super(domainClass, entityManager);
		this.em = entityManager;// 父类没有entityManager的getter To^
		this.domainClass = domainClass;
		log.info("Create BaseDaoImpl for {}", domainClass);
	}

	@Override
	public WhereQuery<T, ID> createWhereQuery() {
		return new WhereQueryImpl<T, ID>(domainClass, em);
	}
	
	public static class WhereQueryImpl<T, ID> implements WhereQuery<T, ID> {
	    
	    private static final String SELECT_ALL_PREFIX = "select * from ";
        
        private List<Pair<String, Object>> params;
        private String sql;
        protected Class<?> domainClass;
        protected EntityManager em;

        protected void init() {
            params = new ArrayList<>();
        }
        
        public WhereQueryImpl(Class<T> domainClass, EntityManager em) {
            this.domainClass = domainClass;
            this.em = em;
            init();
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
                            // 将字段名转化为小写驼峰式写法
                            p = StringUtils.toLowerUnderscore(p);
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
        public WhereQueryImpl<T, ID> where(String p, Object v) {
            if (v == null) {
                return this;
            }
            Pair<String,Object> pair = Pair.of(p, v);
            params.add(pair);
            return this;
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

    @Override
    public PlainSqlQuery<T, ID> createPlainSqlQuery() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static class PlainSqlQueryImple<T, ID> implements PlainSqlQuery<T, ID> {
        protected Class<?> domainClass;
        protected EntityManager em;
        TypedQuery<T> nativeTypeQuery;

        public PlainSqlQueryImple(Class<?> domainClass, EntityManager em) {
            this.domainClass = domainClass;
            this.em = em;
        }

        @Override
        public PlainSqlQuery<T, ID> withSql(String sql) {
            nativeTypeQuery = (TypedQuery<T>) em.createNativeQuery(sql, domainClass);
            return this;
        }

        @Override
        public PlainSqlQuery<T, ID> withParam(String str, Object obj) {
            nativeTypeQuery.setParameter(str, obj);
            return this;
        }

        @Override
        public T singleResult() {
            return nativeTypeQuery.getSingleResult();
        }

        @Override
        public T first() {
            return nativeTypeQuery.getSingleResult();
        }

        @Override
        public List<T> list() {
            return nativeTypeQuery.getResultList();
        }

        @Override
        public List<T> list(int pageNo, int pageSize) {
            return nativeTypeQuery
                    .setFirstResult(pageNo * pageSize)
                    .setMaxResults(pageSize)
                    .getResultList();
        }
    }

    @Override
    public <S extends T> S save(S entity) {
        log.debug("Save: {}", entity);
        return super.save(entity);
    }
}
