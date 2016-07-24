package org.faces2.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.Conversation;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import at.tfr.faces2.model.Child;
import at.tfr.faces2.model.Parent;

/**
 * Backing bean for Child entities.
 * <p/>
 * This class provides CRUD functionality for all Child entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ViewScoped
public class ChildBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Child entities
	 */

	private Long id;
	private Child child;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Child getChild() {
		return this.child;
	}

	public void setChild(final Child child) {
		this.child = child;
	}

//	@Inject
//	private Conversation conversation;

	//@PersistenceContext(unitName = "faces2-persistence-unit", type = PersistenceContextType.EXTENDED)
	@Inject
	private EntityManager entityManager;

	public String create() {

//		this.conversation.begin();
//		this.conversation.setTimeout(1800000L);
		return "create?faces-redirect=true";
	}

	public void retrieve() {

		if (FacesContext.getCurrentInstance().isPostback()) {
			return;
		}

//		if (this.conversation.isTransient()) {
//			this.conversation.begin();
//			this.conversation.setTimeout(1800000L);
//		}

		if (this.id == null) {
			this.child = this.example;
		} else {
			this.child = findById(id);
		}
	}

	public Child findById(Long id) {

		return this.entityManager.find(Child.class, id);
	}

	/*
	 * Support updating and deleting Child entities
	 */

	public String update() {
//		this.conversation.end();

		try {
			if (this.child.getId() == null) {
				this.entityManager.persist(this.child);
				return "search?faces-redirect=true";
			} else {
				this.child = this.entityManager.merge(this.child);
				return "view?faces-redirect=true&id=" + this.child.getId();
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	public String delete() {
//		this.conversation.end();

		try {
			Child deletableEntity = findById(id);
			Parent parent = deletableEntity.getParent();
			parent.getChildren().remove(deletableEntity);
			deletableEntity.setParent(null);
			this.entityManager.merge(parent);
			this.entityManager.remove(deletableEntity);
			this.entityManager.flush();
			return "search?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(e.getMessage()));
			return null;
		}
	}

	/*
	 * Support searching Child entities with pagination
	 */

	private int page;
	private long count;
	private List<Child> pageItems;

	private Child example = new Child();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Child getExample() {
		return this.example;
	}

	public void setExample(Child example) {
		this.example = example;
	}

	public String search() {
		this.page = 0;
		return null;
	}

	public void paginate() {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();

		// Populate this.count

		CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
		Root<Child> root = countCriteria.from(Child.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Child> criteria = builder.createQuery(Child.class);
		root = criteria.from(Child.class);
		TypedQuery<Child> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Child> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		Parent parent = this.example.getParent();
		if (parent != null) {
			predicatesList.add(builder.equal(root.get("parent"), parent));
		}
		String name = this.example.getName();
		if (name != null && !"".equals(name)) {
			predicatesList.add(builder.like(
					builder.lower(root.<String> get("name")),
					'%' + name.toLowerCase() + '%'));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Child> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Child entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Child> getAll() {

		CriteriaQuery<Child> criteria = this.entityManager.getCriteriaBuilder()
				.createQuery(Child.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Child.class))).getResultList();
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Child add = new Child();

	public Child getAdd() {
		return this.add;
	}

	public Child getAdded() {
		Child added = this.add;
		this.add = new Child();
		return added;
	}
}
