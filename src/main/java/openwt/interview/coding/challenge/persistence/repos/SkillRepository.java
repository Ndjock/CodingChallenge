package openwt.interview.coding.challenge.persistence.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import openwt.interview.coding.challenge.persistence.entities.Skill;

public interface SkillRepository extends PagingAndSortingRepository<Skill, Long> {
	@Override
	Optional<Skill> findById(Long id);
	@Override
	Page<Skill> findAll(Pageable pageable);
	@Query("select s from Skill s where id in :ids ")
	List<Skill> findByIdIn(@Param("ids") List<Long> ids); 
	
	Page<Skill> findByIdIn(List<Long> ids, Pageable pageable);
	
}
