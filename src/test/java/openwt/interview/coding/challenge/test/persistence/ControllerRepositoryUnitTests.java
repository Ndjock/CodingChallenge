package openwt.interview.coding.challenge.test.persistence;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import liquibase.integration.spring.SpringLiquibase;
import openwt.interview.coding.challenge.persistence.entities.Contact;
import openwt.interview.coding.challenge.persistence.entities.Skill;
import openwt.interview.coding.challenge.persistence.repos.ContactRepository;
import openwt.interview.coding.challenge.persistence.repos.SkillRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
@Transactional
public class ControllerRepositoryUnitTests {

	// liquibase only loads ddls
	@TestConfiguration
	static class CustomLiquibaseCfg {
		@Autowired
		private DataSource ds;

		@Bean("liquibase")
		public SpringLiquibase getSpringLiquibase() {
			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setDataSource(this.ds);
			liquibase.setChangeLog("classpath:db/changelogs/ddl/changelog-ddls.xml");
			return liquibase;
		}
	}

	@Autowired
	private TestEntityManager em;
	@Autowired
	private SkillRepository skillRepository;

	@Autowired
	private ContactRepository contactRepository;

	static private long currentSkilltId;
	static private long currentContactId;

	@Before
	public void setUp() {
		// em.persistAndFlush(getValidContact());
		// for (int i = 0; i < 3; i++)
		// em.persistAndFlush(getValidSkill());

		contactRepository.save(getValidContact());
		for (int i = 0; i < 3; i++)
			skillRepository.save(getValidSkill());

	}

	@Test
	public void testOne() {

		Contact contact = contactRepository.findById(currentContactId).get();
		contact.addSkill(skillRepository.findById(currentSkilltId).get());
		contact.addSkill(skillRepository.findById(currentSkilltId-1).get());
		contact.addSkill(skillRepository.findById(currentSkilltId-2).get());
		
		Skill skill = skillRepository.findById(currentSkilltId).get();

		skillRepository.delete(skill);
	}

	private Skill getValidSkill() {
		Skill skill = new Skill();
		skill.setLevel("Anylevel");
		skill.setName("AnySkill");
		currentSkilltId++;
		return skill;
	}

	private Contact getValidContact() {
		// Valid contact;
		Contact contact = new Contact();
		contact.setFirstname("sadina");
		contact.setLastname("sadina");
		contact.setFullname("sadina, sadina");
		contact.setEmail("saba@saba.com");
		contact.setPhoneNumber("6219036402");
		contact.setAddressLine("uipstrasse 0, 91524 fmgqjcat");
		currentContactId++;
		return contact;
	}

}
