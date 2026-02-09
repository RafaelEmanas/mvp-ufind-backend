package com.ufind.ufindapp.controller;

import com.ufind.ufindapp.dto.MarkItemClaimedRequest;
import com.ufind.ufindapp.dto.RegisterItemRequest;
import com.ufind.ufindapp.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/item")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
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