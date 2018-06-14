package openwt.interview.coding.challenge.test.web.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
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
	
	private HttpMessageConverter mappingJackson2HttpMessageConverter;
	
	@Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
            .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
            .findAny()
            .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }
	
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

	@Test
	public void getRequestForContactsListShouldReturnExpectedPagination() throws Exception {
		
		// defining init params
		int contactCount = 10;
		int pageLength = 4;
		List<Contact> contacts = new ArrayList<>();
		// adding contactCount amount of contacts
		for (int i = 0; i < contactCount ;i++)
			contacts.add(new Contact());

		//mocking contactPage as the Nth. page of contacts of size x
		int currentPage = 1; 
		Page<Contact> contactPage =  new PageImpl<Contact>(contacts.subList(pageLength-1, pageLength),
										PageRequest.of(currentPage, pageLength),contactCount);
		Mockito
			.when(contactRepository.findAll(Mockito.any(Pageable.class)))
			.thenReturn(contactPage);
		
		// assert pagination status 
		mockMvc
			.perform(get("/contacts").accept(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$..page.number").value(currentPage)) // second page
			.andExpect(jsonPath("$..page.size").value(pageLength)) // page.size
			.andExpect(jsonPath("$..page.totalPages")
						.value((contactCount/pageLength) + (contactCount%pageLength!=0? 1: 0)));// total pages;
	}
	@Test
	public void getRequestForContactListShouldReturnExceptedContactCount() throws Exception {
		// defining init params
		int contactCount = 10;
		int pageLength = 4;
		List<Contact> contacts = new ArrayList<>();
		// adding contactCount amount of contacts
		for (int i = 0; i < contactCount ;i++)
			contacts.add(new Contact());
		
		Page<Contact> contactPage =  new PageImpl<Contact>(contacts.subList(0, pageLength),
				PageRequest.of(0, pageLength),contactCount);
		Mockito
			.when(contactRepository.findAll(Mockito.any(Pageable.class)))
			.thenReturn(contactPage);
		mockMvc.perform(get("/contacts").contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$..contactList.length()").value(pageLength));
	}

	@Test
	public void postRequestForAddingContactShouldReturn200IfContactIsValid() throws Exception {
		Contact contact = getValidContact();
		mockMvc.perform(post("/contacts/")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(contact)))
				.andExpect(status().isCreated());
		Mockito.verify(contactRepository).save(Mockito.any(Contact.class));

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
		return contact;
	}

	
	private Contact getInvalidContact() {
		// invalid contact;
		Contact contact = new Contact();
		contact.setFirstname(null);
		contact.setLastname(null);
		contact.setFullname(null);
		contact.setEmail(null);
		contact.setPhoneNumber(null);
		contact.setAddressLine(null);
		return contact;
	}

	
	@Test
	public void postRequestForAddingContactShouldReturn400IfFirstnameIsNullOrInvalid() throws Exception {
		// Valid contact;
		Contact contact = new Contact();
		contact.setFirstname(null);
		contact.setLastname("sadina");
		contact.setFullname("sadina, sadina");
		contact.setEmail("saba@saba.com");
		contact.setPhoneNumber("6219036402");
		contact.setAddressLine("uipstrasse 0, 91524 fmgqjcat");
 
		mockMvc.perform(post("/contacts/")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(contact)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
				.andExpect(jsonPath("$..details").value(hasItem(containsString("firstname"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
		
		contact.setFirstname("asdadh2");
		
		mockMvc.perform(post("/contacts/")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(contact)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
		.andExpect(jsonPath("$..details").value(hasItem(containsString("firstname"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
		
		contact.setFirstname("  ");
		
		mockMvc.perform(post("/contacts/")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(contact)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
		.andExpect(jsonPath("$..details").value(hasItem(containsString("firstname"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
	
	}
	
	@Test
	public void postRequestForAddingContactShouldReturn400IfLastnameIsNullOrInvalid() throws Exception {
		// Valid contact;
		Contact contact = new Contact();
		contact.setFirstname("sadina");
		contact.setLastname(null);
		contact.setFullname("sadina, sadina");
		contact.setEmail("saba@saba.com");
		contact.setPhoneNumber("6219036402");
		contact.setAddressLine("uipstrasse 0, 91524 fmgqjcat");

		mockMvc.perform(post("/contacts/")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(contact)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
				.andExpect(jsonPath("$..details").value(hasItem(containsString("lastname"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
		
		contact.setLastname("asdadh2");
		
		mockMvc.perform(post("/contacts/")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(contact)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
		.andExpect(jsonPath("$..details").value(hasItem(containsString("lastname"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
		
		contact.setLastname("  ");
		
		mockMvc.perform(post("/contacts/")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(contact)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
		.andExpect(jsonPath("$..details").value(hasItem(containsString("lastname"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
	
	}

	@Test
	public void postRequestForAddingContactShouldReturn400IfFullnameIsNullOrInvalid() throws Exception {
		
		Contact contact = new Contact();
		contact.setFirstname("sadina");
		contact.setLastname("sadina");
		contact.setFullname(null);
		contact.setEmail("saba@saba.com");
		contact.setPhoneNumber("6219036402");
		contact.setAddressLine("uipstrasse 0, 91524 fmgqjcat");

		mockMvc.perform(post("/contacts/")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(contact)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
				.andExpect(jsonPath("$..details").value(hasItem(containsString("fullname"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
		
		contact.setFullname("asdadh2");
		
		mockMvc.perform(post("/contacts/")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(contact)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
		.andExpect(jsonPath("$..details").value(hasItem(containsString("fullname"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
		
		contact.setFirstname("  ");
		
		mockMvc.perform(post("/contacts/")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(contact)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
		.andExpect(jsonPath("$..details").value(hasItem(containsString("fullname"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
		
	}
	
	@Test
	public void postRequestForAddingContactShouldReturn400IfEmailIsNullOrInvalid() throws Exception {
		
		Contact contact = new Contact();
		contact.setFirstname("sadina");
		contact.setLastname("sadina");
		contact.setFullname("sadina, sadina");
		contact.setEmail(null);
		contact.setPhoneNumber("6219036402");
		contact.setAddressLine("uipstrasse 0, 91524 fmgqjcat");

		mockMvc.perform(post("/contacts/")
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(contact)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
				.andExpect(jsonPath("$..details").value(hasItem(containsString("email"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
		
		contact.setEmail("asdadh2");
		
		mockMvc.perform(post("/contacts/")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(contact)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
		.andExpect(jsonPath("$..details").value(hasItem(containsString("email"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
		
		contact.setFirstname("  ");
		
		mockMvc.perform(post("/contacts/")
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(contact)))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$..message").value(hasItem("Validation Error")))
		.andExpect(jsonPath("$..details").value(hasItem(containsString("email"))));
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
		
	}
	
	@Test
	public void putRequestForUpdatingContactShouldReturn403UserNotSameAsContact() throws Exception {
		// user id and contact id are not the same id;
		Long contactId= 1000000L; 
		Long userId= 1L;
	
		Contact contact = new Contact();
		contact.setId(contactId);
		Optional<Contact> optContact =  Optional.of(contact);
		
		Mockito.when(contactRepository.findByEmail(Mockito.any(String.class))).thenReturn(optContact);
		
		mockMvc.perform(put("/contacts/"+userId)
						.with(SecurityMockMvcRequestPostProcessors.csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(json(getValidContact())))
				.andExpect(status().isForbidden());	
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));

	}
	
	@Test
	public void putRequestForUpdatingContactShouldReturn404IfUserNotFoundByEmail() throws Exception {
		Long contactId = 1L;
		
		Optional<Contact> optContact =  Optional.ofNullable(null);
		Mockito.when(contactRepository.findByEmail(Mockito.any(String.class))).thenReturn(optContact);
		mockMvc.perform(put("/contacts/"+contactId)
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(getValidContact())))
		.andExpect(status().isForbidden());
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
		
	}

	@Test
	public void putRequestForUpdatingContactShouldReturn400IfContactNotValid() throws Exception {
		Long contactId = 1L;
		mockMvc.perform(put("/contacts/"+contactId)
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(getInvalidContact())))
		.andExpect(status().isBadRequest());
		Mockito.verify(contactRepository,Mockito.never()).save(Mockito.any(Contact.class));
	}

	@Test
	public void putRequestForUpdatingContactShouldReturn200IfContactValid() throws Exception {
		// user id and contact id have the same id;
		Long contactId= 1L; 
		Long userId= 1L;
		
		// return a contact with user id
		Contact contact = new Contact();
		contact.setId(userId);
		Mockito.when(contactRepository.findByEmail(Mockito.any(String.class))).thenReturn(Optional.of(contact));
		Mockito.when(contactRepository.findById(contactId)).thenReturn(Optional.of(new Contact()));

		mockMvc.perform(put("/contacts/"+contactId)
				.with(SecurityMockMvcRequestPostProcessors.csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json(getValidContact())))
		.andExpect(status().isOk());
		
		Mockito.verify(contactRepository, Mockito.times(1)).save(Mockito.any(Contact.class));
	}
	
	@Test
	public void deleteRequestForRemovingContactShouldReturn403IfUserNotExists() throws Exception {
		Long userId= 1L;
	
		Mockito.when(contactRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.ofNullable(null));
		
		mockMvc.perform(delete("/contacts/"+userId)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
			.andExpect(status().isForbidden());
		Mockito.verify(contactRepository,Mockito.never()).delete(Mockito.any());

	}
	
	@Test
	public void deleteRequestForRemovingContactShouldReturn403IfUserNotSameAsContact() throws Exception {
		// user id and contact id don't have the same id;
		Long userId= 1L;
		Long contactId= 2L;
		
		Contact contact = new Contact();
		contact.setId(userId);
		
		Mockito.when(contactRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(contact));
		
		mockMvc.perform(delete("/contacts/"+contactId)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
			.andExpect(status().isForbidden());
		Mockito.verify(contactRepository,Mockito.never()).delete(Mockito.any());
	}
	
	@Test
	public void deleteRequestForRemovingContactShouldReturn200IfUserSameAsContact() throws Exception {
		// user id and contact id don't have the same id;
		Long userId= 1L;
		Long contactId= 1L;
		
		Contact contact = new Contact();
		contact.setId(userId);
		
		Mockito.when(contactRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(contact));
		Mockito.when(contactRepository.findById(contactId)).thenReturn(Optional.of(new Contact()));
		
		mockMvc.perform(delete("/contacts/"+contactId)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
			.andExpect(status().isOk());
		Mockito.verify(contactRepository,Mockito.times(1)).delete(Mockito.any());
	}
	

	
	private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
	
	
	@Test
	public void testJsonToContact() throws IOException, JSONException {
		JSONAssert.assertEquals("{id:123}", "{id:123,name:\"john\"}", JSONCompareMode.LENIENT);
		JSONAssert.assertNotEquals("{id:123}", "{id:123,name:\"john\"}", JSONCompareMode.STRICT);

	}
	
}
