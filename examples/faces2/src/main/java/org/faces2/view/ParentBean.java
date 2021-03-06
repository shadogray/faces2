package org.faces2.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
 * Backing bean for Parent entities.
 * <p/>
 * This class provides CRUD functionality for all Parent entities. It focuses
 * purely on Java EE 6 standards (e.g. <tt>&#64;ConversationScoped</tt> for
 * state management, <tt>PersistenceContext</tt> for persistence,
 * <tt>CriteriaBuilder</tt> for searches) rather than introducing a CRUD
 * framework or custom base class.
 */

@Named
@Stateful
@ViewScoped
public class ParentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * Support creating and retrieving Parent entities
	 */

	private Long id;
	private Parent parent;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Parent getParent() {
		return this.parent;
	}

	public void setParent(Parent parent) {
		this.parent = parent;
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
			this.parent = this.example;
		} else {
			findById(id);
		}
	}

	public Parent findById(Long id) {
		Map<String,Object> hints = new HashMap<>();
		hints.put("javax.persistence.fetchgraph", entityManager.getEntityGraph("graph.Parent.children"));
		return entityManager.find(Parent.class, id, hints);
	}

	/*
	 * Support updating and deleting Parent entities
	 */

	public String update() {
//		this.conversation.end();

		try {
			if (this.parent.getId() == null) {
				this.entityManager.persist(this.parent);
				return "search?faces-redirect=true";
			} else {
				this.parent = this.entityManager.merge(this.parent);
				return "view?faces-redirect=true&id=" + this.parent.getId();
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
			Parent deletableEntity = findById(id);
			Iterator<Child> iterChildren = deletableEntity.getChildren()
					.iterator();
			for (; iterChildren.hasNext();) {
				Child nextInChildren = iterChildren.next();
				nextInChildren.setParent(null);
				iterChildren.remove();
				this.entityManager.merge(nextInChildren);
			}
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
	 * Support searching Parent entities with pagination
	 */

	private int page;
	private long count;
	private List<Parent> pageItems;

	private Parent example = new Parent();

	public int getPage() {
		return this.page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPageSize() {
		return 10;
	}

	public Parent getExample() {
		return this.example;
	}

	public void setExample(Parent example) {
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
		Root<Parent> root = countCriteria.from(Parent.class);
		countCriteria = countCriteria.select(builder.count(root)).where(
				getSearchPredicates(root));
		this.count = this.entityManager.createQuery(countCriteria)
				.getSingleResult();

		// Populate this.pageItems

		CriteriaQuery<Parent> criteria = builder.createQuery(Parent.class);
		root = criteria.from(Parent.class);
		TypedQuery<Parent> query = this.entityManager.createQuery(criteria
				.select(root).where(getSearchPredicates(root)));
		query.setFirstResult(this.page * getPageSize()).setMaxResults(
				getPageSize());
		this.pageItems = query.getResultList();
	}

	private Predicate[] getSearchPredicates(Root<Parent> root) {

		CriteriaBuilder builder = this.entityManager.getCriteriaBuilder();
		List<Predicate> predicatesList = new ArrayList<Predicate>();

		String name = this.example.getName();
		if (name != null && !"".equals(name)) {
			predicatesList.add(builder.like(
					builder.lower(root.<String> get("name")),
					'%' + name.toLowerCase() + '%'));
		}

		return predicatesList.toArray(new Predicate[predicatesList.size()]);
	}

	public List<Parent> getPageItems() {
		return this.pageItems;
	}

	public long getCount() {
		return this.count;
	}

	/*
	 * Support listing and POSTing back Parent entities (e.g. from inside an
	 * HtmlSelectOneMenu)
	 */

	public List<Parent> getAll() {

		CriteriaQuery<Parent> criteria = this.entityManager
				.getCriteriaBuilder().createQuery(Parent.class);
		return this.entityManager.createQuery(
				criteria.select(criteria.from(Parent.class))).getResultList();
	}

	/*
	 * Support adding children to bidirectional, one-to-many tables
	 */

	private Parent add = new Parent();

	public Parent getAdd() {
		return this.add;
	}

	public Parent getAdded() {
		Parent added = this.add;
		this.add = new Parent();
		return added;
	}
}
