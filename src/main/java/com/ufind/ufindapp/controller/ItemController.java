package com.ufind.ufindapp.controller;

import com.ufind.ufindapp.dto.MarkItemClaimedRequest;
import com.ufind.ufindapp.dto.RegisterItemRequest;
import com.ufind.ufindapp.entity.Item;
import com.ufind.ufindapp.service.ItemService;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/item")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Item>> getAllItems(Pageable pageable) {
        return ResponseEntity.ok(itemService.getAllItems(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Item>> searchItems(
        @RequestParam String query,
        Pageable pageable
    ) {
        return ResponseEntity.ok(itemService.searchItems(query, pageable));
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('SECRETARY', 'ADMIN')")
    public ResponseEntity<Void> registerItem(
        @Valid
        @RequestBody
        RegisterItemRequest request
    ) {
        itemService.registerItem(request);
        return ResponseEntity.status(201).build();
    }

    @PatchMapping("")
    @PreAuthorize("hasAnyRole('SECRETARY', 'ADMIN')")
    public ResponseEntity<Void> markItemAsClaimed(
        @Valid
        @RequestBody
        MarkItemClaimedRequest request
    ){
        itemService.markItemAsClaimed(request);
        return ResponseEntity.status(200).build();
    }

}