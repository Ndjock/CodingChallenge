package openwt.interview.coding.challenge.persistence.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Entity
@Table(name = "contacts")
@Data
@ApiModel(description = "Class representing a contact")
public class Contact implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8820218026462277026L;

	@ApiModelProperty(notes = "Identifier of the contact. The id is currently generated by the database"
			, example = "1", position = 0)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ApiModelProperty(notes = "Firstname of the contact. It must not be empty and must only consist of letters"
			, example = "Arthur", required = true, position = 1)
	@NotEmpty(message="firstname must not be empty")
	@Pattern(regexp="[a-zA-Z]+",message="firstname must only consist of letters")
	@Column(name = "firstname")
	private String firstname;

	@ApiModelProperty(notes = "Lastname of the contact. It must not be empty and must only consist of letters"
			, example = "Abanda", required = true, position = 2)
	@NotEmpty(message="Lastname must not be empty")
	@Pattern(regexp="[a-zA-Z]+",message="lastname must only consist of letters")
	@Column(name = "lastname")
	private String lastname;

	@ApiModelProperty(notes = "Fullname of the contact. It must only consist of at most 5 words, each made of letters"
			, example = "Martin Luther King", required = true, position = 3)
	@NotEmpty(message="fullname must not be empty")
	@Pattern(regexp="(([a-zA-z]+)\\s*,?\\s*){1,5}",message="fullname must only consist of at most 5 words, each made of letters")
	@Column(name = "fullname")
	private String fullname;

	@ApiModelProperty(notes = "Email of the contact. It must match general email pattern"
			, example = "martin.luther@king.com", required = true, position = 4)
	@NotEmpty(message="email must not be empty")
	@Pattern(regexp="^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",message="Input must match general email pattern")
	@Column(name = "email")
	private String email;

	@ApiModelProperty(notes = "Email of the contact. It must match general email pattern"
			, example = "martin.luther@king.com", required = true, position = 3)
	@NotEmpty(message="phoneNumber must not be empty")
	@Pattern(regexp="^[0-9]{8,15}$",message="input must consist of a number of size between 8 and 15")
	@Column(name = "mobile_phone_number")
	private String phoneNumber;

	
	@ApiModelProperty(notes = "Adress line [streetname housenumber, zipcode town] of the contact. Must match general email pattern"
			, example = "martin.luther@king.com", required = true, position = 3)
	@NotEmpty(message="addressline must not be empty")
	@Pattern(regexp="^(\\s*([a-zA-z]+)\\s*)[0-9]{1,4}\\s*,\\s*[0-9]{1,5}\\s*(([a-zA-z]+)\\s*)$",message="Pattern must be streetname housenumber, zipcode town")
	@Column(name = "address_line")
	private String addressLine;

	@ManyToMany
	@JoinTable(name = "contacts_with_skills", joinColumns = @JoinColumn(name = "contact_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "skill_id", referencedColumnName = "id"))
	@JsonIgnore
	private List<Skill> skills = new ArrayList<>();

	public void addSkill(Skill skill) {
		if(!this.skills.contains(skill))
			this.skills.add(skill);
		if (!skill.getContacts().contains(this))
			skill.getContacts().add(this);
	}

	public void removeSkill(Skill skill) {
		this.skills.remove(skill);
		if (skill.getContacts().contains(this))
			skill.getContacts().remove(this);
	}
	
	public void removeAllSkills() {
		skills.stream()
				.map(Skill::getContacts)
				.filter(contacts -> contacts.contains(this))
				.forEach(contacts -> contacts.remove(this));
		skills.clear();
	}
	
}
