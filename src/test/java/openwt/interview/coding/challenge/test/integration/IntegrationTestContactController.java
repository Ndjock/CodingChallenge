package openwt.interview.coding.challenge.test.integration;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import liquibase.integration.spring.SpringLiquibase;
import openwt.interview.coding.challenge.persistence.entities.Contact;
import openwt.interview.coding.challenge.persistence.entities.Skill;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@WithMockUser(password="saba@saba.com") // default user password
@Transactional
public class IntegrationTestContactController {
	
	
	// liquibase only loads ddls
	@TestConfiguration
	static class CustomLiquibaseCfg{
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
	private MockMvc mockMvc;
	
	@Autowired
	private TestEntityManager em; 
	
	// keeping track of the autoincremented ids
	static private long currentContactId = 0;
	static private long currentSkilltId = 0;
	
	
	@Before
	public void initContactsAndSkills() {
		// init contact & skills
		em.persist(getValidContact());
		for(int i=0 ; i< 3; i++)
			em.persist(getValidSkill());
	}
	
	@Test
	public void shouldContactSkillListIncreaseWhendAddingSkillReferencesThroughControllerEndpoint() throws Exception {
		// old state: (c1 --> null)
		// adding skills: sk1,sk2 --> c1
		// old state: (c1 --> sk1,sk2)
				
		// adding skills to contacts
		Long cId = currentContactId;
		long skId = currentSkilltId;
		
		mockMvc.perform(put("/contacts/"+cId+"/skills/"+(skId--))
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isCreated());
		mockMvc.perform(put("/contacts/"+cId+"/skills/"+(skId--))
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
		.andExpect(status().isCreated());

		assertThat(em.find(Contact.class, currentContactId).getSkills())
						.hasSize(2)
						.extracting("id").contains(skId+1,skId+2);

		mockMvc.perform(put("/contacts/"+cId+"/skills/"+(skId))
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
		.andExpect(status().isCreated());
		
		assertThat(em.find(Contact.class, currentContactId).getSkills())
						.hasSize(3)
						.extracting("id").contains(skId,skId+1,skId+2);
	}

	@Test
	public void shouldContactSkillListDecreaseWhendRemovingSkillReferencesThroughControllerEndpoint() throws Exception {
		// old state: (c1 --> sk1,sk2,sk3) 
		// removing skills: sk1,sk2 from c1
		// new  state: (c1 --> null)
		
		long cId = currentContactId;
		long skId = currentSkilltId;
		
		Contact contact =	em.find(Contact.class,cId);
		contact.addSkill(em.find(Skill.class,skId--));
		contact.addSkill(em.find(Skill.class,skId--));
		contact.addSkill(em.find(Skill.class,skId));
		em.persist(contact);

		assertThat(em.find(Contact.class,cId).getSkills()).hasSize(3);

		// removing skill references to contacts through rest and asserting the db state
		mockMvc.perform(delete("/contacts/"+cId+"/skills/"+(skId++))
						.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/contacts/"+cId+"/skills/"+(skId++))
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
		.andExpect(status().isOk());

		assertThat(em.find(Contact.class,cId).getSkills()).hasSize(1);
		
		mockMvc.perform(delete("/contacts/"+cId+"/skills/"+((skId)))
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
		.andExpect(status().isOk());

		assertThat(em.find(Contact.class,cId).getSkills()).isEmpty();
	}
	
	
	@Test
	public void shouldContactSkillReferenceBePresentWhenAddedThroughControllerEndPoint() throws Exception {

		long cId = currentContactId;
		long skId = currentSkilltId;
		
		// nothing at the beggining
		mockMvc.perform(get("/contacts/"+cId+"/skills"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(isEmptyOrNullString()));
		
		// adding references through API
		mockMvc.perform(put("/contacts/"+cId+"/skills/"+(skId--)))
			.andExpect(status().isCreated());
		mockMvc.perform(put("/contacts/"+cId+"/skills/"+(skId--)))
			.andExpect(status().isCreated());
		mockMvc.perform(put("/contacts/"+cId+"/skills/"+(skId)))
			.andExpect(status().isCreated());
		
		
		//asserting at the front
		mockMvc.perform(get("/contacts/"+cId+"/skills"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$..skillList.length()", hasItem(greaterThan(0))));
		
		
		// assering at the back
		assertThat(em.find(Contact.class, cId).getSkills())
				.hasSize(3)
				.extracting("id").contains(skId,skId+1,skId+2);
	}

	
	@Test
	public void shouldContactSkillReferenceBeErasedWhenSkillIsDeletedThroughControllerEndPoint() throws Exception {
		// old : (c1 --> sk1,sk2,sk3)
		// deleting sk1,sk2,sk3 using DELETE /skills/2
		// new : (c1 --> null)
		
		
		long cId = currentContactId;
		long skId = currentSkilltId;
		
		// set references
		Contact contact = em.find(Contact.class,cId);
		
		contact.addSkill(em.find(Skill.class,skId--));
		contact.addSkill(em.find(Skill.class,skId--));
		contact.addSkill(em.find(Skill.class,skId));
		em.persistAndFlush(contact);

		// removing all 3 skills from db
		mockMvc.perform(delete("/skills/"+(skId)).with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(status().isOk());
		mockMvc.perform(delete("/skills/"+(skId+1)).with(SecurityMockMvcRequestPostProcessors.csrf()))
			.andExpect(status().isOk());
		mockMvc.perform(delete("/skills/"+(skId+2)).with(SecurityMockMvcRequestPostProcessors.csrf()))
			.andExpect(status().isOk());
		
		// asserting the contact yet no more skill references
		mockMvc.perform(get("/contacts/"+currentContactId+"/skills/"))
			.andExpect(status().isNoContent());
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
