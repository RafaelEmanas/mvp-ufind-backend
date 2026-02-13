package com.ufind.ufindapp.repository;

import com.ufind.ufindapp.entity.Item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

	@Query("""
			SELECT i FROM Item i
			WHERE (LOWER(i.title) LIKE LOWER(CONCAT('%', :query, '%'))
			   OR LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%'))
			   OR LOWER(i.locationFound) LIKE LOWER(CONCAT('%', :query, '%')))
			""")
	Page<Item> searchItems(@Param("query") String query, Pageable pageable);

}