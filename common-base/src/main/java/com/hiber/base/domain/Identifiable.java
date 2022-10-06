package com.hiber.base.domain;

/**
 * Points entities which have identifier.
 *
 * @param <T> Identifier class.
 */
public interface Identifiable<T> {
	/**
	 * @return Identifier of that object.
	 */
	T getId();
}
