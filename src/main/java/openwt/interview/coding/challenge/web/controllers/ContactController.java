package openwt.interview.coding.challenge.web.controllers;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import openwt.interview.coding.challenge.persistence.entities.Contact;
import openwt.interview.coding.challenge.persistence.entities.Skill;
import openwt.interview.coding.challenge.persistence.repos.ContactRepository;
import openwt.interview.coding.challenge.persistence.repos.SkillRepository;
import openwt.interview.coding.challenge.web.dto.ResultResponse;
import openwt.interview.coding.challenge.web.error.ElementNotFoundException;
import springfox.documentation.annotations.ApiIgnore;

@Api(value="Contact Manager with simple CRUDing functions on contacts and their skills")
@SwaggerDefinition()
@RestController
@RequestMapping("/contacts")
public class ContactController {

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private SkillRepository skillRepository;

	@ApiOperation(value = "${ContactController.getContactById.value}")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Contact retrieved successfully"),
	        @ApiResponse(code = 404, message = "The contact you are trying to access is not found")
	})
	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResultResponse<Contact>> getContactById(
			@ApiParam(name="id",value="the id of the contact to be fetched", required=true)
			@PathVariable("id") long id) {
		Contact contact = contactRepository.findById(id)
				.orElseThrow(() -> new ElementNotFoundException("couldn't find contact with id: " + id));
		return new ResponseEntity<ResultResponse<Contact>>(new ResultResponse<Contact>(contact), HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "View a list of existing contacts")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Contact list retrieved successfully"),
	        @ApiResponse(code = 204, message = "No element was found, the contact list is empty")
	})
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
	            value = "Results page you want to retrieve (0..N)"),
	    @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
	            value = "Number of records per page."),
	    @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
	            value = "Sorting criteria in the format: property(,asc|desc). " +
	                    "Default sort order is ascending. " +
	                    "Multiple sort criteria are supported.")
	})
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PagedResources<Resource<Contact>>> getContactList(
			Pageable pageable,PagedResourcesAssembler<Contact> assembler) {
		ResponseEntity<PagedResources<Resource<Contact>>> responseEntity = null;
		Page<Contact> contactPage = contactRepository.findAll(pageable);
		if (contactPage.hasContent())
			responseEntity = new ResponseEntity<PagedResources<Resource<Contact>>>(assembler.toResource(contactPage),HttpStatus.OK);
		else
			responseEntity = new ResponseEntity<PagedResources<Resource<Contact>>>(HttpStatus.NO_CONTENT);
		return responseEntity;
	}

	@ApiOperation(value = "Create a new Contact")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Contact was successfully created")
	})
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> createContact(
			@ApiParam(name="contact", value="the serialized contact entity to be sent to the url to be created")
			@Valid @RequestBody Contact contact,
			UriComponentsBuilder ucBuilder) {
		contact.setId(null);
		contactRepository.save(contact);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/user/{id}").buildAndExpand(contact.getId()).toUri());
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}
	

	@ApiOperation(value = "update an existing contact")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Contact was successfully updated"),
	        @ApiResponse(code = 404, message = "The contact you are trying to update was not found"),
			@ApiResponse(code = 403, message = "The authenticated contact can't update another contact")
	})
	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Contact> updateContact(
			@ApiParam(name="id",value="the id of the contact to be updated", required=true)
			@PathVariable("id") Long id,
			@ApiParam(name="contact", value="the serialized contact entity to be sent to the url to be updated")			
			@Valid @RequestBody Contact contact,
			@ApiIgnore
			Authentication auth) {
	 	
		requireUserBeSameAsContact(auth, id, getContactChangeAuthMsg(id, auth.getName()));
		
		if(!isUserBeingSameContact(auth,id)) 
			throw new AccessDeniedException("update of contact with id "+id+" forbiden for User "+auth.getName());

		contactRepository.findById(id).orElseThrow(
				() -> new ElementNotFoundException("couldn't find contact (with id: " + id + ") to update"));
		contact.setId(id);
		contactRepository.save(contact);
		return new ResponseEntity<Contact>(contact, HttpStatus.OK);
	}

	private boolean isUserBeingSameContact(Authentication auth, Long id) {
		// with authentication being done by Email
		String password = auth.getCredentials().toString();
		Contact contact = contactRepository
							.findByEmail(password)
							.orElseThrow(() ->  new AccessDeniedException("could not find contact with email "+password));
		return contact.getId().equals(id);
	}


	@ApiOperation(value = "delete an existing contact")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Contact was successfully deleted"),
	        @ApiResponse(code = 404, message = "The contact you are trying to delete  was not found")
	})
	@DeleteMapping(value = "{id}")
	public ResponseEntity<Void> deleteContact(
			@ApiParam(name="id",value="the id of the contact to be deleted", required=true)
			@PathVariable("id") Long id,
			@ApiIgnore
			Authentication auth) {
		
		requireUserBeSameAsContact(auth, id, getContactChangeAuthMsg(id, auth.getName()));
		
		Contact contact = contactRepository.findById(id).orElseThrow(
				() -> new ElementNotFoundException("couldn't find contact (with id: " + id + ") to delete"));
		contact.removeAllSkills();
		contactRepository.delete(contact);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	@ApiOperation(value = "adding a skill to a contact")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Skill was successfully added to the contact"),
	        @ApiResponse(code = 404, message = "The contact/skill looked for is not present in the datastore"),
			@ApiResponse(code = 403, message = "The authenticated contact can't change skills of another contact")
	})
	@PutMapping(value = "/{id}/skills/{skillId}")
	public ResponseEntity<Void> addNewSkill(
			@ApiParam(name="id",value="the id of the contact", required=true)
			@PathVariable("id") Long id,
			@ApiParam(name="skillId",value="the id of the skill to be added", required=true)
			@PathVariable("skillId") Long skillId,
			UriComponentsBuilder ucBuilder,
			@ApiIgnore
			Authentication auth) {
		
		requireUserBeSameAsContact(auth,id,getSkillChangeAuthMsg(id,auth.getName()));
		
		Contact contact = contactRepository.findById(id).orElseThrow(
				() -> new ElementNotFoundException("couldn't find contact (with id: " + id + ") to skill addition"));

		Skill skill = skillRepository.findById(skillId).orElseThrow(
				() -> new ElementNotFoundException("couldn't find skill (with id: " + id + ") to add to a contact"));

		contact.addSkill(skill);
		contactRepository.save(contact);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/user/{id}").buildAndExpand(contact.getId()).toUri());
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	@ApiOperation(value = "removing a skill to a contact")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Skill was successfully removed to the contact"),
	        @ApiResponse(code = 404, message = "The contact/skill looked for is not present in the datastore"),
			@ApiResponse(code = 403, message = "The authenticated contact can't change skills of another contact")
	})
	@DeleteMapping(value = "/{id}/skills/{skillId}")
	public ResponseEntity<Void> deleteAddedSkill(
			@ApiParam(name="id",value="the id of the contact", required=true)
			@PathVariable("id") Long id,
			@ApiParam(name="skillId",value="the id of the skill to be removed", required=true)			
			@PathVariable("skillId") Long skillId,
			UriComponentsBuilder ucBuilder,
			@ApiIgnore
			Authentication auth) {

		requireUserBeSameAsContact(auth,id,getSkillChangeAuthMsg(id,auth.getName()));
		

		Contact contact = contactRepository.findById(id).orElseThrow(
				() -> new ElementNotFoundException("couldn't find contact (with id: " + id + ") to delete"));

		Skill skill = skillRepository.findById(skillId).orElseThrow(
				() -> new ElementNotFoundException("couldn't find skill (with id: " + id + ") to delete from contact"));

		contact.removeSkill(skill);
		contactRepository.save(contact);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/user/{id}").buildAndExpand(contact.getId()).toUri());
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	
	private void requireUserBeSameAsContact(Authentication auth, Long id, String msg) {
		if(!isUserBeingSameContact(auth,id)) 
			throw new AccessDeniedException(msg.toString());
	}


	private String getSkillChangeAuthMsg(Long contactId, String username) {
		return 	new StringBuilder()
					.append("change on skills of contact with id ")
					.append(contactId)	
					.append(" forbiden for User ")
					.append(username).toString();
	}
	
	private String getContactChangeAuthMsg(Long contactId, String username) {
		return 	new StringBuilder()
					.append("update of contact with id ")
					.append(contactId)	
					.append(" forbiden for User ")
					.append(username).toString();
	}


	@ApiOperation(value = "View list of skills beloging to a contact")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Contact skill list is successful fetched"),
			@ApiResponse(code = 204, message = "there are no skills associated with this contact"),
			@ApiResponse(code = 404, message = "The contact looked for is not present in the datastore")
	})
	@ApiImplicitParams({
	    @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
	            value = "Results page you want to retrieve (0..N)"),
	    @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
	            value = "Number of records per page."),
	    @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
	            value = "Sorting criteria in the format: property(,asc|desc). " +
	                    "Default sort order is ascending. " +
	                    "Multiple sort criteria are supported.")
	})
	@GetMapping(value = "/{id}/skills", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PagedResources<Resource<Skill>>> getContactSkills(
			@ApiParam(name="id",value="the id of the contact", required=true)
			@PathVariable("id") Long id, Pageable pageable, PagedResourcesAssembler<Skill> assembler,
			@ApiIgnore("authentication Object from Spring")
			Authentication auth) {
		Contact contact = contactRepository.findById(id).orElseThrow(
				() -> new ElementNotFoundException("couldn't find contact (with id: " + id + ") to fetch skill from"));

		List<Long> skillIds = contact.getSkills().stream().map(Skill::getId).collect(Collectors.toList());

		if (skillIds.isEmpty())
			return new ResponseEntity<PagedResources<Resource<Skill>>>(HttpStatus.NO_CONTENT);
		
		Page<Skill> skillPage = skillRepository.findByIdIn(skillIds, pageable);
		if (skillPage.hasContent())
			return new ResponseEntity<PagedResources<Resource<Skill>>>(assembler.toResource(skillPage),
					HttpStatus.OK);
		else
			throw new ElementNotFoundException(
					"couldn't find skills (ids= " + skillIds.stream().map(i -> "" + i).collect(Collectors.joining(","))
							+ " ) from contact (with id: " + id + ")");
	}
}
