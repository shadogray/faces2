package org.faces2.view;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class Provider {

	//@PersistenceContext(unitName = "faces2-persistence-unit", type = PersistenceContextType.EXTENDED)
	@Produces
	@PersistenceContext(unitName = "faces2-persistence-unit")
	private EntityManager entityManager;

}
