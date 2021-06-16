package org.gridsofts.halo.crud;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface IGenericType<T> {

	/**
	 * 获取泛型类
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default Class<T> getGenericClass(int index) {
		Type type = getClass().getGenericSuperclass();

		if (type instanceof ParameterizedType) {
			ParameterizedType ptype = (ParameterizedType) type;
			Type[] typeArgs = ptype.getActualTypeArguments();

			if (typeArgs != null && typeArgs.length > index) {
				String typeName = typeArgs[index].getTypeName();

				try {
					return (Class<T>) Class.forName(typeName);
				} catch (ClassNotFoundException e) {
				}
			}
		}

		return null;
	}
}
