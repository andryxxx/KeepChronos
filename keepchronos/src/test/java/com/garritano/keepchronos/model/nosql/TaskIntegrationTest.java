package com.garritano.keepchronos.model.nosql;

import static org.junit.Assert.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.transaction.TransactionManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.garritano.keepchronos.model.nosql.Project;
import com.garritano.keepchronos.model.nosql.Task;

public class TaskIntegrationTest {
	private static final String PERSISTENCE_UNIT_NAME = "infinispan-pu";
	private static EntityManagerFactory entityManagerFactory;
	protected EntityManager entityManager;
	private static TransactionManager transactionManager;
	private Query query;

	private Project project_another;
	private Task task1;
	private Task task2;

	@BeforeClass
	public static void setUpClass() {
		entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);

		// accessing JBoss's Transaction can be done differently but this one works
		// nicely
		transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
	}

	@Before
	public void setUp() throws Exception {
		entityManager = entityManagerFactory.createEntityManager();

		// make sure to have the Task table empty
		transactionManager.begin();
		query = entityManager.createQuery("select t from Task t");
		List<Task> allTasks = query.getResultList();
		for (Task element : allTasks) {
			entityManager.remove(element);
		}
		transactionManager.commit();

		transactionManager.begin();
		query = entityManager.createQuery("select p from Task p");
		assertTrue(query.getResultList().size() == 0);
		transactionManager.commit();

		// get a new EM to make sure data is actually retrieved from the store and not
		// Hibernate’s internal cache
		entityManager.close();
		entityManager = entityManagerFactory.createEntityManager();

		project_another = new Project();
		project_another.setTitle("Another project");
		project_another.setDescription("Another exciting project!");

		task1 = new Task();
		task1.setTitle("First task");
		task1.setDescription("This is my first task, hi!");
		task1.setProject(project_another);
		task1.setDuration(30);

		task2 = new Task();
		task2.setTitle("Second task");
		task2.setDescription("This is my second task, wow!");
		task2.setDuration(20);
	}

	@Test
	public void testBasicPersistence() throws Exception {

		transactionManager.begin();
		entityManager.persist(task1);
		transactionManager.commit();

		// Perform a simple query for all the Task entities
		query = entityManager.createQuery("select p from Task p");

		// We should have only one task in the database
		assertTrue(query.getResultList().size() == 1);

		// We should have the same title
		assertTrue(((Task) query.getSingleResult()).getTitle().equals(task1.getTitle()));

		// and the same description
		assertTrue(((Task) query.getSingleResult()).getDescription().equals(task1.getDescription()));

		// and the same id
		assertTrue(((Task) query.getSingleResult()).getId() == (task1.getId()));

		// and the same duration
		assertTrue(((Task) query.getSingleResult()).getDuration() == (task1.getDuration()));

		// and the same associate project
		assertTrue(((Task) query.getSingleResult()).getProject().equals(project_another));
	}

	@Test
	public void testBasicPersistenceWithoutProject() throws Exception {
		transactionManager.begin();
		entityManager.persist(task2);
		transactionManager.commit();

		// Perform a simple query for all the Task entities
		query = entityManager.createQuery("select p from Task p");

		assertEquals(null, ((Task) query.getSingleResult()).getProject());
	}

	@Test
	public void testMultiplePersistence() throws Exception {
		transactionManager.begin();
		entityManager.persist(task1);
		transactionManager.commit();

		transactionManager.begin();
		entityManager.persist(task2);
		transactionManager.commit();

		// Perform a simple query for all the Task entities
		Query query = entityManager.createQuery("select t from Task t");

		// We should have 2 tasks in the database
		assertTrue(query.getResultList().size() == 2);
	}

	@After
	public void tearDown() {
		task1 = null;
		task2 = null;
		entityManager.close();
	}

	@AfterClass
	public static void tearDownClass() {
		entityManagerFactory.close();
	}
}