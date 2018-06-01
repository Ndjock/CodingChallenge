package openwt.interview.coding.challenge.persistence.repos;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import openwt.interview.coding.challenge.persistence.entities.Contact;

public interface ContactRepository extends  PagingAndSortingRepository<Contact, Long> {
	@Override
	Page<Contact> findAll(Pageable pageable);
	@Query("select c from Contact c where id in :ids ")
	List<Contact> findByIdIn(@Param("ids") List<Long> ids); 
	
	Page<Contact> findByIdIn(List<Long> ids, Pageable pageable);
	
	Optional<Contact> findByEmail(String email);
	


}
