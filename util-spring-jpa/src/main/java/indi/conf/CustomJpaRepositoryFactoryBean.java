package indi.conf;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import indi.dao.BaseDaoImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * 将FactoryBean注册为Bean以替换相应的默认自动注册的Bean
 * 
 * @author DragonBoom
 *
 */
@Slf4j
public class CustomJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends JpaRepositoryFactoryBean<T, S, ID> {

	public CustomJpaRepositoryFactoryBean(Class<T> repositoryInterface) {
		super(repositoryInterface);
		log.info("Init CustomJpaRepositoryFactory for {}", repositoryInterface);
	}

	@Override
	protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
		return new CustomJpaRepositoryFactory(entityManager);
	}
	
	private class CustomJpaRepositoryFactory extends JpaRepositoryFactory {

		public CustomJpaRepositoryFactory(EntityManager entityManager) {
			super(entityManager);
		}
		
		@Override
		protected SimpleJpaRepository<?, ?> getTargetRepository(RepositoryInformation information,
				EntityManager entityManager) {
			BaseDaoImpl<?, ?> baseDaoImpl = new BaseDaoImpl<>(information.getDomainType(), entityManager);
			return baseDaoImpl;
		}
		
		/***
		 * important
		 */
		@Override
		protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
			return BaseDaoImpl.class;
		}
		
		
	}

}
