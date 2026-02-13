package com.ufind.ufindapp.service;

import com.ufind.ufindapp.dto.MarkItemClaimedRequest;
import com.ufind.ufindapp.dto.RegisterItemRequest;
import com.ufind.ufindapp.entity.Item;
import com.ufind.ufindapp.entity.ItemStatus;
import com.ufind.ufindapp.exception.ItemNotFoundException;
import com.ufind.ufindapp.repository.ItemRepository;

import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Page<Item> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public Item getItemById(UUID id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new ItemNotFoundException("Item not found with id: "+id));
    }

    public Page<Item> searchItems(String query, Pageable pageable) {
        return itemRepository.searchItems(query, pageable);
    }

    public void registerItem(RegisterItemRequest request) {

        Item newItem = Item.builder()
                .title(request.title())
                .description(request.description())
                .dateFound(request.dateFound())
                .locationFound(request.locationFound())
                .status(request.status())
                .imageUrl(request.imageUrl())
                .contactInfo(request.contactInfo())
                .build();
   
        itemRepository.save(newItem);

    }

    @Transactional
    public void markItemAsClaimed(MarkItemClaimedRequest request) {

        Item item = itemRepository.findById(request.id())
            .orElseThrow(() -> new ItemNotFoundException("The queried item wasn't found."));

        item.setStatus(ItemStatus.CLAIMED);

    }

}