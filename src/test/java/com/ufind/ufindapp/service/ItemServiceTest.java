package com.ufind.ufindapp.service;

import com.ufind.ufindapp.dto.MarkItemClaimedRequest;
import com.ufind.ufindapp.entity.Item;
import com.ufind.ufindapp.entity.ItemStatus;
import com.ufind.ufindapp.exception.ItemNotFoundException;
import com.ufind.ufindapp.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private UUID itemId;
    private MarkItemClaimedRequest request;
    private Item availableItem;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        request = new MarkItemClaimedRequest(itemId);

        availableItem = Item.builder()
                .id(itemId)
                .title("Lost Wallet")
                .description("Black leather wallet found in library")
                .dateFound(LocalDate.of(2026, 2, 5))
                .locationFound("Central Library")
                .status(ItemStatus.AVAILABLE)
                .contactInfo("library@ufam.edu.br")
                .build();
    }

    // ===== Happy Path =====

    @Test
    @DisplayName("Should mark available item as claimed")
    void shouldMarkAvailableItemAsClaimed() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(availableItem));

        itemService.markItemAsClaimed(request);

        assertThat(availableItem.getStatus()).isEqualTo(ItemStatus.CLAIMED);
    }

    @Test
    @DisplayName("Should find item by ID from request")
    void shouldFindItemByIdFromRequest() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(availableItem));

        itemService.markItemAsClaimed(request);

        verify(itemRepository).findById(itemId);
    }

    @Test
    @DisplayName("Should change status from AVAILABLE to CLAIMED")
    void shouldChangeStatusFromAvailableToClaimed() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(availableItem));
        assertThat(availableItem.getStatus()).isEqualTo(ItemStatus.AVAILABLE); // verify initial state

        itemService.markItemAsClaimed(request);

        assertThat(availableItem.getStatus()).isEqualTo(ItemStatus.CLAIMED);
    }

    @Test
    @DisplayName("Should work with already claimed item (idempotent operation)")
    void shouldWorkWithAlreadyClaimedItem() {
        Item alreadyClaimedItem = Item.builder()
                .id(itemId)
                .title("Lost Keys")
                .description("Car keys")
                .dateFound(LocalDate.of(2026, 2, 1))
                .locationFound("Parking Lot")
                .status(ItemStatus.CLAIMED) // already claimed
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(alreadyClaimedItem));

        itemService.markItemAsClaimed(request);

        assertThat(alreadyClaimedItem.getStatus()).isEqualTo(ItemStatus.CLAIMED);
    }

    @Test
    void getItemByIdWhenItemExistsReturnsItem() {
        UUID id = UUID.randomUUID();
        Item item = new Item();
        item.setId(id);
        
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        
        Item result = itemService.getItemById(id);
        
        assertNotNull(result);
        assertEquals(id, result.getId());
    }


    // ===== Error Cases =====

    @Test
    @DisplayName("Should throw ItemNotFoundException when item does not exist")
    void shouldThrowItemNotFoundExceptionWhenItemDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        MarkItemClaimedRequest requestWithNonExistentId = new MarkItemClaimedRequest(nonExistentId);
        
        when(itemRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.markItemAsClaimed(requestWithNonExistentId))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessage("The queried item wasn't found.");
    }

    @Test
    @DisplayName("Should throw ItemNotFoundException with correct message")
    void shouldThrowItemNotFoundExceptionWithCorrectMessage() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.markItemAsClaimed(request))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("queried item")
                .hasMessageContaining("wasn't found");
    }

    @Test
    @DisplayName("Should NOT change status when item is not found")
    void shouldNotChangeStatusWhenItemNotFound() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        try {
            itemService.markItemAsClaimed(request);
        } catch (ItemNotFoundException e) {
            // expected
        }

        // availableItem should remain unchanged since it was never retrieved
        assertThat(availableItem.getStatus()).isEqualTo(ItemStatus.AVAILABLE);
    }

    @Test
    void getItemByIdWhenItemNotFoundThrowsException() {
        UUID id = UUID.randomUUID();
        
        when(itemRepository.findById(id)).thenReturn(Optional.empty());
        
        assertThrows(ItemNotFoundException.class, () -> itemService.getItemById(id));
    }


    // ===== Transactional Behavior =====

    @Test
    @DisplayName("Should rely on @Transactional to persist status change without explicit save")
    void shouldRelyOnTransactionalToPersistChanges() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(availableItem));

        itemService.markItemAsClaimed(request);

        // Verify that repository.save() is NOT called explicitly
        // JPA's dirty checking + @Transactional should handle persistence
        verify(itemRepository, never()).save(any(Item.class));
        
        // But the entity status should still be changed in memory
        assertThat(availableItem.getStatus()).isEqualTo(ItemStatus.CLAIMED);
    }
}