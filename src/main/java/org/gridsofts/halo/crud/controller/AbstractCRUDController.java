package org.gridsofts.halo.crud.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.gridsofts.halo.crud.IEntity;
import org.gridsofts.halo.crud.IEntityService;
import org.gridsofts.halo.crud.IGenericType;
import org.gridsofts.halo.crud.SrvException;
import org.gridsofts.halo.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 实体信息“增删改查”控制器的抽象基类
 * 
 * @author lei
 */
public abstract class AbstractCRUDController<T extends IEntity<K>, K> implements IGenericType<K> {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private IEntityService<T, K> crudService;

	public IEntityService<T, K> getCrudService() {
		return crudService;
	}

	public void setCrudService(IEntityService<T, K> crudService) {
		this.crudService = crudService;
	}

	/**
	 * 子类通过实现这个方法来设置服务类
	 */
	public abstract void setCrudService();
	
	@PostConstruct
	public void initialize() {
		setCrudService();
	}

	/**
	 * 获取所有的实体信息
	 * 
	 * @return
	 */
	@ApiResponses(@ApiResponse(code = 200, message = "实体信息列表；JSON数组"))
	@ApiOperation("获取所有的实体信息")

	@ResponseBody
	@RequestMapping(value = "", method = RequestMethod.GET)
	public List<T> list() {

		try {
			return crudService.list();
		} catch (SrvException e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * 查找实体信息
	 * 
	 * @param pkid
	 * @return
	 */
	@ApiResponses(@ApiResponse(code = 200, message = "实体信息；JSON对象"))
	@ApiOperation("查找实体信息")

	@ResponseBody
	@RequestMapping(value = "/{pkid:.+}", method = RequestMethod.GET)
	public T find(@ApiParam(value = "路径变量；实体ID；", required = true) @PathVariable String pkid) {

		try {
			Class<K> ktype = getGenericClass(1);

			if (ktype != null && !StringUtil.isNull(pkid)) {
				return crudService.find(ktype.cast(ktype));
			}
		} catch (SrvException e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * 新建 实体信息
	 * 
	 * @param bean
	 * @return
	 */
	@ApiResponses(@ApiResponse(code = 200, message = "OK - 创建成功；FAIL - 创建失败；其它异常信息"))
	@ApiOperation("新增")

	@ResponseBody
	@RequestMapping(value = "", method = RequestMethod.POST)
	public String create(T bean) {

		try {
			if (bean == null) {
				return ("bean is null");
			}

			return crudService.create(bean) ? ("OK") : ("FAIL");
		} catch (SrvException e) {
			logger.error(e.getMessage(), e);

			return e.getMessage();
		}
	}

	/**
	 * 修改 实体信息
	 * 
	 * @param bean
	 * @return
	 */
	@ApiResponses(@ApiResponse(code = 200, message = "OK - 修改成功；FAIL - 修改失败；其它异常信息"))
	@ApiOperation("修改")

	@ResponseBody
	@RequestMapping(value = "", method = RequestMethod.PUT)
	public String update(T bean) {

		try {
			if (bean == null) {
				return ("bean is null");
			}

			// 先查找要修改的实体
			T target = crudService.find(((IEntity<K>) bean).getPK());
			if (target == null) {
				return ("bean not found");
			}

			return crudService.update(target) ? ("OK") : ("FAIL");
		} catch (SrvException e) {
			logger.error(e.getMessage(), e);

			return e.getMessage();
		}
	}

	/**
	 * 批量删除实体信息
	 * 
	 * @param ids 多个实体ID以“,”分隔
	 * @return
	 */
	@ApiResponses(@ApiResponse(code = 200, message = "OK - 删除成功；FAIL - 删除失败；其它异常信息"))
	@ApiOperation("批量删除")

	@ResponseBody
	@RequestMapping(value = "/{ids:.+}", method = RequestMethod.DELETE)
	public String delete(@ApiParam(value = "路径变量；实体ID；多个ID以“,”分隔", required = true) @PathVariable String ids) {

		try {
			Class<K> ktype = getGenericClass(1);

			if (!StringUtil.isNull(ids) && ktype != null) {
				List<K> idList = Arrays.stream(ids.split("\\s*\\,+\\s*")).map(ktype::cast).collect(Collectors.toList());
				return crudService.remove(idList) ? ("OK") : ("FAIL");
			}

			return ("FAIL");
		} catch (SrvException e) {
			logger.error(e.getMessage(), e);

			return e.getMessage();
		}
	}
}
