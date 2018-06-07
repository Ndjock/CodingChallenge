package openwt.interview.coding.challenge.test.web.controllers;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import openwt.interview.coding.challenge.persistence.entities.Contact;
import openwt.interview.coding.challenge.persistence.repos.ContactRepository;
import openwt.interview.coding.challenge.persistence.repos.SkillRepository;
import openwt.interview.coding.challenge.web.controllers.ContactController;
import openwt.interview.coding.challenge.web.error.ElementNotFoundException;

/**
 * Testing the contact controller as pojo with its mocks
 * @author Pc
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WhenUsingContractController {
	
	@Mock
	private ContactRepository contactRepository;
	@Mock
	private SkillRepository skillRepository;
	@InjectMocks
	private ContactController contactController;

	
	@Test(expected=ElementNotFoundException.class)
	public void requestShouldThrowElementNotFoundIfContactNotFound() {
		Long id = 1L;
		Optional<Contact> optContact = Optional.ofNullable(null);
		Mockito.when(contactRepository.findById(id)).thenReturn(optContact);
		contactController.getContactById(id);
	}
	
	@Test
	public void requestShouldReturnNotNullResponseIfContacFound() {
		Long id = 1L;
		Optional<Contact> optContact = Optional.of(new Contact());
		
		Mockito.when(contactRepository.findById(id)).thenReturn(optContact);
		Assertions.assertThat(contactController.getContactById(id)).isNotNull();
	}
	
	
}

