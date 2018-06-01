package openwt.interview.coding.challenge.web.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import openwt.interview.coding.challenge.persistence.entities.Contact;

@RunWith(SpringRunner.class)
@WebMvcTest(ContactController.class)
public class ContactControllerTest {

	@Autowired
	private MockMvc mvc;
	
	@Test
	public void postingContactWithMissingFirstnameShouldBeBadRequest() throws Exception {
		mvc.perform(post("/contacts/")
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(createContact()))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void postingContactWithPresentFirstnameShouldBeValidRequest() throws Exception {
		mvc.perform(post("/contacts/")
					.contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(createContact()))
			.andExpect(status().is2xxSuccessful());
	}
	
	private String createContact() throws JsonProcessingException {
		Contact contactDTO = new Contact();
		contactDTO.setFirstname("firstname");
		contactDTO.setEmail("aasda@yahoo.de");
		contactDTO.setLastname("lastname");
		contactDTO.setPhoneNumber("0002222345987");
		return new ObjectMapper().writeValueAsString(contactDTO);
	}
}
