package org.hibernate.bugs;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.bugs.entity.Computer;
import org.hibernate.bugs.entity.Employee;
import org.hibernate.bugs.entity.Person;
import org.hibernate.bugs.entity.Workplace;
import org.hibernate.query.Query;

import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

/**
 * @author Stanislav Alekminskiy
 */
public class QueryGenerationTestCase extends BaseCoreFunctionalTestCase {

	@Before
	public void setupData() {
		Session session = openSession();
		Transaction tx = session.getTransaction();
		tx.begin();

		Workplace workplace = new Workplace( "location one" );
		session.persist( workplace );

		Person person = new Person( "John", "Doe" );
		session.persist( person );

		Employee employee = new Employee( person, workplace );
		session.persist( employee );

		Computer computer = new Computer( "1", workplace );
		session.persist( computer );

		workplace = new Workplace( "location two" );
		session.persist( workplace );

		person = new Person( "Jane", "Doe" );
		session.persist( person );

		employee = new Employee( person, workplace );
		session.persist( employee );

		tx.commit();
		session.close();
	}

	@Test
	public void testSQLGenerationForImplicitJoinInSelectClause() {
		Session session = openSession();

		Query query = session.createQuery(
				"select c.inventoryNumber from Employee e left join Computer c on c.workplace.id = e.workplace.id" );
		List resultList = query.getResultList();
		Assert.assertThat( resultList.size(), is( 2 ) );

		query = session.createQuery(
				"select e.person.firstName, c.inventoryNumber from Employee e left join Computer c on c.workplace.id = e.workplace.id" );
		resultList = query.getResultList();
		Assert.assertThat( resultList.size(), is( 2 ) );

		session.close();
	}

    @Test
    public void testRedundantSqlJoins() {
        Session session = openSession();

        Query query = session.createQuery(
                "select c.inventoryNumber from Employee e left join Computer c on c.workplace = e.workplace" );
        query.getResultList();

        session.close();
    }

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] {
				Person.class,
				Employee.class,
				Workplace.class,
				Computer.class
		};
	}
}
