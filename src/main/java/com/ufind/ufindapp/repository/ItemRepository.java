package com.ufind.ufindapp.repository;

import com.ufind.ufindapp.entity.Item;
import com.ufind.ufindapp.entity.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

  List<Item> findByStatus(ItemStatus status);

  @Query("""
      SELECT i FROM Item i
      WHERE (LOWER(i.title) LIKE LOWER(CONCAT('%', :query, '%'))
         OR LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%'))
         OR LOWER(i.locationFound) LIKE LOWER(CONCAT('%', :query, '%')))
        AND i.status = :status
      """)
  List<Item> searchAvailableItems(@Param("query") String query, @Param("status") ItemStatus status);
}
