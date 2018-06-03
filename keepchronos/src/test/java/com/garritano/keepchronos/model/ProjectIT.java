package com.garritano.keepchronos.model;

import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ProjectIT {
	private static final String PERSISTENCE_UNIT_NAME = "mysql-pu";
	private static EntityManagerFactory entityManagerFactory;
	protected EntityManager entityManager;
	
	@BeforeClass
	public static void setUpClass() {
		entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	}
	
	@Before
	public void setUp() throws Exception {
		entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		
		// make sure to drop the Project table for testing
		entityManager.createNativeQuery("delete from Project").executeUpdate();
		entityManager.getTransaction().commit();
		
		entityManager.getTransaction().begin();
	}
	
	@Test
	public void testBasicPersistence() {
		Project project = new Project();
		project.setTitle("First project");
		project.setDescription("This is my first project, hi!");
		
		entityManager.persist(project);
		
		entityManager.getTransaction().commit();
		entityManager.clear();
		
		// Create a fresh, new EntityManager
		entityManager = entityManagerFactory.createEntityManager();
		
		// Perform a simple query for all the Message entities
		Query query = entityManager.createQuery("select p from Project p");

		// We should have only one project in the database
		assertTrue(query.getResultList().size() == 1);
	}
	
	@Test
	public void testMultiplePersistence() {
		Project project1 = new Project();
		project1.setTitle("First project");
		project1.setDescription("This is my first project, hi!");
		
		entityManager.persist(project1);
		
		Project project2 = new Project();
		project2.setTitle("First project");
		project2.setDescription("This is my first project, hi!");
		
		entityManager.persist(project2);
		
		entityManager.getTransaction().commit();
		entityManager.clear();
		
		// Create a fresh, new EntityManager
		entityManager = entityManagerFactory.createEntityManager();
		
		// Perform a simple query for all the Message entities
		Query query = entityManager.createQuery("select p from Project p");

		// We should have 2 projects in the database
		assertTrue(query.getResultList().size() == 2);
	}
	
	@After
	public void tearDown() {
		if ( entityManager.getTransaction().isActive() ) {
			entityManager.getTransaction().rollback();
		}
		entityManager.close();
	}

	@AfterClass
	public static void tearDownClass() {
		entityManagerFactory.close();
	}
}