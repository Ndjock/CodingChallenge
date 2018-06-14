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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import openwt.interview.coding.challenge.persistence.entities.Contact;
import openwt.interview.coding.challenge.persistence.entities.Skill;
import openwt.interview.coding.challenge.persistence.repos.ContactRepository;
import openwt.interview.coding.challenge.persistence.repos.SkillRepository;
import openwt.interview.coding.challenge.web.dto.ResultResponse;
import openwt.interview.coding.challenge.web.error.ElementNotFoundException;

@RestController
@RequestMapping("/skills")

public class SkillController {

	@Autowired
	private SkillRepository skillRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	

	@ApiOperation(value = "${SkillController.getSkillById.value}")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Skill retrieved successfully"),
	        @ApiResponse(code = 404, message = "The skill you are trying to access is not found")
	})

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResultResponse<Skill>> getSkillById(
			@ApiParam(name="id",value="the id of the skill to be fetched")
			@PathVariable("id") long id) {
		Skill skill = skillRepository.findById(id)
				.orElseThrow(() -> new ElementNotFoundException("couldn't find skill with id: " + id));
		return new ResponseEntity<ResultResponse<Skill>>(new ResultResponse<Skill>(skill), HttpStatus.OK);
	}

	@ApiOperation(value = "View a list of existing skills")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Skill list retrieved successfully"),
	        @ApiResponse(code = 204, message = "No element was found the skill list is empty at all, or only in the requested page")
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
	public ResponseEntity<PagedResources<Resource<Skill>>> getSkills(
			@ApiParam(name="pageable",value="the (implicit) spring Data Pageable for sorting")
			Pageable pageable, PagedResourcesAssembler<Skill> assembler) {
		ResponseEntity<PagedResources<Resource<Skill>>> responseEntity = null;
		Page<Skill> skillPage = skillRepository.findAll(pageable);
		if (skillPage.hasContent())
			responseEntity = new ResponseEntity<PagedResources<Resource<Skill>>>(
					assembler.toResource(skillPage), HttpStatus.OK);
		else
			responseEntity = new ResponseEntity<PagedResources<Resource<Skill>>>(HttpStatus.NOT_FOUND);
		return responseEntity;
	}

	
	@ApiOperation(value = "Create a new Skill")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Skill was successfully created")
	})
	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> createContact(
			@ApiParam(name="contact", value="the serialized skill entity to be sent to the endpoint to be created")
			@Valid @RequestBody Skill skill,
			UriComponentsBuilder ucBuilder) {
		skill.setId(null);
		skillRepository.save(skill);
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(ucBuilder.path("/user/{id}").buildAndExpand(skill.getId()).toUri());
		return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
	}

	@ApiOperation(value = "update an existing skill")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Skill was successfully updated"),
	        @ApiResponse(code = 404, message = "The skill you are trying to update was not found")
	})
	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Skill> updateContact(
			@ApiParam(name="id",value="the id of the skill to be updated")
			@PathVariable("id") Long id,
			@ApiParam(name="skill", value="the serialized skill entity to be sent to the endpoint to be updated")			
			@Valid @RequestBody Skill updatedSkill) {
		skillRepository.findById(id)
				.orElseThrow(() -> new ElementNotFoundException("couldn't find skill (with id: " + id + ") to update"));
		updatedSkill.setId(id);
		skillRepository.save(updatedSkill);
		return new ResponseEntity<Skill>(updatedSkill, HttpStatus.OK);
	}
	
	@ApiOperation(value = "delete an existing skill")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Skill was successfully deleted"),
	        @ApiResponse(code = 404, message = "The skill you are trying to delete  was not found")
	})
	@DeleteMapping(value = "{id}")
	@Transactional
	public ResponseEntity<Void> delete(
			@ApiParam(name="id",value="the id of the skill to be deleted")
			@PathVariable("id") Long id) {
	Skill skill =	skillRepository
						.findById(id)
						.orElseThrow(() -> new ElementNotFoundException("couldn't find skill (with id: " + id + ") to delete"));
		skill.setId(id);
		skill.removeAllContacts();
		skillRepository.delete(skill);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "View list of contacts which have this skill")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Contact list is successful fetched"),
			@ApiResponse(code = 204, message = "there are no contacts associated with this skills at all or in the requested page"),
			@ApiResponse(code = 404, message = "The targeted skill is not present in the datastore")
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
	@GetMapping(value="/{id}/contacts",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PagedResources<Resource<Contact>>> getSkillContacts(
			@ApiParam(name="id",value="the id of the skill", required=true)
			@PathVariable("id") Long id, Pageable pageable,PagedResourcesAssembler<Contact> assembler) {

		Skill skill = skillRepository.findById(id)
			.orElseThrow(() -> new ElementNotFoundException("couldn't find skill (with id: " + id + ") for contacts to be fecthed from"));

		List<Long> contactIds = skill.getContacts()
									.stream()
									.map(Contact::getId)
									.collect(Collectors.toList());
		
		if(contactIds.isEmpty())
			return  new ResponseEntity<PagedResources<Resource<Contact>>>(HttpStatus.NO_CONTENT);
		
		Page<Contact> contactPage = contactRepository.findByIdIn(contactIds,pageable);
		
		if (contactPage.hasContent())
			return new ResponseEntity<PagedResources<Resource<Contact>>>(assembler.toResource(contactPage), HttpStatus.OK);
		else
			if(!contactIds.isEmpty())
				return  new ResponseEntity<PagedResources<Resource<Contact>>>(HttpStatus.NO_CONTENT);
			throw new ElementNotFoundException("couldn't find contacts (ids= "+contactIds.stream().map(i -> ""+i).collect(Collectors.joining(","))+" ) from skills (with id: " + id + ")");
	}
}
