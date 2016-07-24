package at.tfr.faces2.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import at.tfr.faces2.model.Parent;
@ApplicationScoped
public class Provider {

	private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("faces2-persistence-unit");
	
	@Produces
	public EntityManager getEntityManger() {
		EntityManager em = emf.createEntityManager();
		return em;
	}

	public static Map<String, Object> createHints(EntityManager em, @SuppressWarnings("rawtypes") Class rootType, String... nodes) {
		@SuppressWarnings("unchecked")
		EntityGraph<Parent> graph = em.createEntityGraph(rootType);
		Stream.of(nodes).forEach((node) -> graph.addAttributeNodes(node));
		return createHints(em, graph);
	}

	public static Map<String, Object> createHints(EntityManager em, String graphName, String... nodes) {
		@SuppressWarnings("rawtypes")
		EntityGraph graph = em.createEntityGraph(graphName);
		Stream.of(nodes).forEach((node) -> graph.addAttributeNodes(node));
		return createHints(em, graph);
	}

	public static Map<String, Object> createHints(EntityManager em, @SuppressWarnings("rawtypes") EntityGraph graph) {
		Map<String, Object> hints = new HashMap<>();
		hints.put("javax.persistence.fetchgraph", graph);
		return hints;
	}
}
