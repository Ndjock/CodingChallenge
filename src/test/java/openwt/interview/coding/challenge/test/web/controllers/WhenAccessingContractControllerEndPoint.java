package openwt.interview.coding.challenge.test.web.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import openwt.interview.coding.challenge.persistence.entities.Contact;
import openwt.interview.coding.challenge.persistence.repos.ContactRepository;
import openwt.interview.coding.challenge.persistence.repos.SkillRepository;
import openwt.interview.coding.challenge.web.controllers.ContactController;

@RunWith(SpringRunner.class)
@WebMvcTest(ContactController.class)
@WithMockUser
@EnableSpringDataWebSupport 
public class WhenAccessingContractControllerEndPoint {

	@Autowired
	private  MockMvc mockMvc;

	@MockBean(name="contactRepository")
	private ContactRepository contactRepository;
	@MockBean(name="skillRepository")
	private SkillRepository skillRepository;
	
	
	@Test
	public void getRequestForContactSearchByIdShouldReturn200IfContactExists() throws Exception {
		Contact contact = new Contact();
		contact.setId(1L);
		Mockito
			.when(contactRepository.findById(1L))
			.thenReturn(Optional.of(contact));
		mockMvc
			.perform(get("/contacts/1").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}
	
	@Test
	public void getRequestForContactSearchByIdShouldReturn404IfContactDoesntExists() throws Exception {
		Mockito
			.when(contactRepository.findById(1L))
			.thenReturn(Optional.ofNullable(null));
		mockMvc
			.perform(get("/contacts/1").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());
	}
	
	@Test
	public void getRequestForContactsListShouldReturn204IfContacListEmpty() throws Exception {
		List<Contact> contacts = Arrays.asList();
		Mockito.when(contactRepository.findAll(Mockito.any(Pageable.class)))
				.thenReturn(new PageImpl<>(contacts));
		mockMvc
			.perform(get("/contacts").accept(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isNoContent());
	}
	
	@Test
	public void getRequestForContactsListShouldReturn200IfContacNotEmpty() throws Exception {
		List<Contact> contacts = Arrays.asList(new Contact());
		Page<Contact> page =  new PageImpl<Contact>(contacts,PageRequest.of(0, 1),1L);
		Mockito
			.when(contactRepository.findAll(Mockito.any(Pageable.class)))
			.thenReturn(page);
		mockMvc
			.perform(get("/contacts").accept(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isOk());
	}
}
