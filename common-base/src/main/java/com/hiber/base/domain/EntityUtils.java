package com.hiber.base.domain;

import java.util.Optional;
import org.hibernate.proxy.HibernateProxy;

/**
 * Entity objects utils.
 */
public abstract class EntityUtils {
	/**
	 * Gets identifier from hibernate object. If object is hibernate proxy than this object is not fetch
	 * (id is present in proxy so we can get it without database fetch select).
	 *
	 * @param entity Entity with identifier.
	 * @param <T> Object id class.
	 *
	 * @return Identifier from entity.
	 */
	public static <T> T getId(Identifiable<T> entity) {
		if (entity instanceof HibernateProxy) {
			Optional<T> id = findId((HibernateProxy) entity);
			return id.orElseThrow(() -> new RuntimeException("Proxied object has no identifier field set"));
		}
		return Optional.ofNullable(entity.getId())
				.orElseThrow(() -> new RuntimeException("Object has no identifier field set"));
	}

	/**
	 * Retrieves id from object without database fetch select.
	 *
	 * @param hibernateProxyEntity Hibernate proxy entity.
	 * @param <T> Object id class.
	 *
	 * @return Id of that object.
	 */
	@SuppressWarnings("unchecked")
	private static <T> Optional<T> findId(HibernateProxy hibernateProxyEntity) {
		return Optional.ofNullable((T) hibernateProxyEntity.getHibernateLazyInitializer().getIdentifier());
	}
}
