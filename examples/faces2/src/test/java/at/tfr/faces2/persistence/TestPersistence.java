package at.tfr.faces2.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.hibernate.LazyInitializationException;
import org.hibernate.bytecode.enhance.spi.interceptor.LazyAttributeLoadingInterceptor;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

import at.tfr.faces2.model.Child;
import at.tfr.faces2.model.Parent;

@SuppressWarnings("unused")
@RunWith(CdiTestRunner.class)
public class TestPersistence {

	@Inject
	private EntityManager em;

	@ClassRule
	public static TestRule init = new ExternalResource() {
		private EntityManager em;
		private EntityTransaction tx;
		@Override
		public void before() {
			em = new Provider().getEntityManger();
			tx = em.getTransaction();
			Parent parent = new Parent();
			parent.setName("Test");
			tx.begin();
			Assert.assertTrue("PersistenceContext not bound to active TX", em.isJoinedToTransaction());
			em.persist(parent);
			Child child = new Child();
			child.setName("TestChild1");
			child.setParent(parent);
			em.persist(child);
			child = new Child();
			child.setName("TestChild2");
			child.setParent(parent);
			em.persist(child);
			tx.commit();
		}
	};

	/**
	 * plain EntityManger find, traversal during open persistence context
	 *  - no LazyLoadingException
	 *  - but second SELECT to fetch associated collection
	 */
	@Test
	public void testSimpleSelect() throws Exception {
		List<Parent> ps = em.createQuery("select p from Parent p", Parent.class).getResultList();
		List<Child> cs = em.createQuery("select c from Child c", Child.class).getResultList();
		Assert.assertTrue("Insert failed for parents", ps.size() > 0);
		Assert.assertTrue("Insert failed for childred", cs.size() > 0);
		Parent p = em.find(Parent.class, 1L);
		Assert.assertFalse("no children associated", p.getChildren().isEmpty());
		Assert.assertFalse("not all children associated", p.getChildren().size() < 2);
	}

	/**
	 * Empty FetchGraph of Parent w/o additional node "children" -> LazyLoadingException
	 */
	@Test(expected=LazyInitializationException.class)
	public void testParentWithEmptyGraph() throws Exception {
		Parent p = em.find(Parent.class, 1L, Provider.createHints(em, Parent.class));
		Assert.assertNotNull(p);
		em.close();
		Assert.assertFalse("no children associated", p.getChildren().isEmpty());
		Assert.assertFalse("not all children associated", p.getChildren().size() < 2);
	}

	/**
	 * Empty FetchGraph of Parent with additional node "children" - no LazyLoadingException
	 */
	@Test
	public void testParentWithChildrenSelect() throws Exception {
		Parent p = em.find(Parent.class, 1L, Provider.createHints(em, Parent.class, "children"));
		Assert.assertNotNull(p);
		em.close();
		Assert.assertFalse("no children associated", p.getChildren().isEmpty());
		Assert.assertFalse("not all children associated", p.getChildren().size() < 2);
	}

	/**
	 * Default FetchGraph of Parent with additional node "children" - no LazyLoadingException
	 */
	@Test
	public void testParentWithDefaultGraphSelect() throws Exception {
		Parent p = em.find(Parent.class, 1L, Provider.createHints(em, "Parent"));
		em.close();
		Assert.assertFalse("no children associated", p.getChildren().isEmpty());
		Assert.assertFalse("not all children associated", p.getChildren().size() < 2);
	}

	/**
	 * Default FetchGraph of Parent with - here redundant - additional node "children"
	 */
	@Test
	public void testParentWithChildrenNodeSelect() throws Exception {
		Parent p = em.find(Parent.class, 1L, Provider.createHints(em, "Parent", "children"));
		em.close();
		Assert.assertFalse("no children associated", p.getChildren().isEmpty());
		Assert.assertFalse("not all children associated", p.getChildren().size() < 2);
	}

	/**
	 * Hibernate issues second SELECT on parent association - works
	 */
	@Test
	public void testChildWithEmptyGraph() throws Exception {
		Child c = em.find(Child.class, 2L, Provider.createHints(em, Child.class));
		em.close();
		Assert.assertEquals("no parent associated", "Test", c.getParent().getName());
	}

	/**
	 * FetchGraph includes Parent node, no second SELECT
	 */
	@Test
	public void testChildWithDefaultGraph() throws Exception {
		Child c = em.find(Child.class, 2L, Provider.createHints(em, "Child"));
		em.close();
		Assert.assertEquals("no parent associated", "Test", c.getParent().getName());
	}

	private Map<String, Object> createParentHints() {
		Class<Parent> rootType = Parent.class;
		String[] nodes = { "children" };
		return Provider.createHints(em, rootType, nodes);
	}

}
