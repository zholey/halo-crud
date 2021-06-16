package org.gridsofts.halo.crud.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.gridsofts.halo.SuperDAO;
import org.gridsofts.halo.annotation.Table;
import org.gridsofts.halo.bean.Condition;
import org.gridsofts.halo.crud.IEntity;
import org.gridsofts.halo.crud.IEntityService;
import org.gridsofts.halo.crud.IGenericType;
import org.gridsofts.halo.crud.SrvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实体信息“增删改查”服务的抽象基类
 * 
 * @author lei
 */
public abstract class AbstractCRUDService<T extends IEntity<K>, K> implements IEntityService<T, K>, IGenericType<T> {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private SuperDAO superDAO;
	private Class<T> beanClass;

	public SuperDAO getSuperDAO() {
		return superDAO;
	}

	public void setSuperDAO(SuperDAO superDAO) {
		this.superDAO = superDAO;
	}

	public void setBeanClass(Class<T> beanClass) {
		this.beanClass = beanClass;
	}

	/**
	 * 子类通过实现这个方法来设置DAO
	 */
	public abstract void setSuperDAO();

	@PostConstruct
	public void initialize() {
		setSuperDAO();
		setBeanClass(getGenericClass(0));
	}

	@Override
	public boolean create(T bean) throws SrvException {
		return superDAO.save(beanClass, bean) != null;
	}

	@Override
	public boolean update(T bean) throws SrvException {
		return superDAO.update(bean) == 1;
	}

	@Override
	public T find(K pkid) throws SrvException {
		return superDAO.find(beanClass, pkid);
	}

	@Override
	public List<T> findAll(List<K> pkids) throws SrvException {

		if (pkids == null) {
			return null;
		}

		return pkids.stream().map(pkid -> {
			return superDAO.find(beanClass, pkid);
		}).collect(Collectors.toList());
	}

	@Override
	public List<T> list() throws SrvException {
		return superDAO.list(beanClass);
	}

	@Override
	public boolean remove(List<K> pkids) throws SrvException {

		if (pkids != null) {
			int result = 0;

			for (K pkid : pkids) {
				try {
					result += superDAO.delete(find(pkid));
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}

			return result == pkids.size();
		}
		return false;
	}

	@Override
	public List<T> query(Condition condition) {
		Table tableAnnotation = beanClass.getAnnotation(Table.class);
		if (tableAnnotation == null) {
			throw new NullPointerException("tableAnnotation is null");
		}

		StringBuffer conditionSql = new StringBuffer("SELECT T.* FROM " + tableAnnotation.value() + " T");
		List<Object> queryParams = new ArrayList<>();

		// 拼接查询条件
		if (condition != null) {
			conditionSql.append(getConditionSQL(beanClass, condition, queryParams));
		}

		return superDAO.executeQuery(beanClass, conditionSql.toString(), queryParams);
	}
}
