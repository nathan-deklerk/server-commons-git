package com.hiber.test.utils;

public abstract class CastUtils {
	/**
	 * Save cast to generic iterable.
	 *
	 * @param object Object to cast.
	 * @param aClass Expected class.
	 * @param <T> Expected class.
	 *
	 * @return Cast iterable with expected class.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> iterableCast(Object object, Class<T> aClass) {
		if (!(object instanceof Iterable))
			throw new RuntimeException("Object is not an iterable");
		Iterable iterable = (Iterable) object;
		iterable.forEach(i -> {
			if (!aClass.equals(i.getClass()))
				throw new RuntimeException("Iterable contains not valid class: " + i.getClass());
		});
		return (Iterable<T>) object;
	}
}