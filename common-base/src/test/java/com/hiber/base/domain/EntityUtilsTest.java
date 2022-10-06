package com.hiber.base.domain;

import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.proxy.AbstractLazyInitializer;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;

class EntityUtilsTest {

	@Test
	void getId_shouldGetIdFromObject() {
		UUID uuid = UUID.randomUUID();
		UUID id = EntityUtils.getId(new ObjectWithId(uuid));
		assertThat(id, sameInstance(uuid));
	}

	@Test
	void getId_shouldGetIdFromProxy() {
		UUID uuid = UUID.randomUUID();
		UUID proxyUuid = UUID.randomUUID();
		UUID id = EntityUtils.getId(new ProxiedObjectWithId(uuid, proxyUuid));
		assertThat(id, sameInstance(proxyUuid));
	}

	@Data
	static class ObjectWithId implements Identifiable<UUID> {
		private final UUID id;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	private static class ProxiedObjectWithId extends ObjectWithId implements HibernateProxy {
		private final LazyInitializer hibernateLazyInitializer;

		ProxiedObjectWithId(UUID id, UUID proxyId) {
			super(id);
			this.hibernateLazyInitializer = new AbstractLazyInitializer(ObjectWithId.class.getSimpleName(), proxyId, null) {
				@Override
				public Class getPersistentClass() {
					return ObjectWithId.class;
				}
			};
		}

		@Override
		public Object writeReplace() {
			return null;
		}
	}
}