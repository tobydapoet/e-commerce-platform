package com.example.e_commerce.repository;

import com.example.e_commerce.entity.CartItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends CrudRepository<CartItem, Long> {
    long countByCartStoreId(Long cartStoreId);
    List<CartItem> findAllByIdIn(List<Long> ids);
    @Query("SELECT ci.cartStore.id AS cartStoreId, COUNT(ci) AS total " +
            "FROM CartItem ci WHERE ci.cartStore.id IN :cartStoreIds " +
            "GROUP BY ci.cartStore.id")
    List<CartStoreItemCount> countGroupByCartStoreId(@Param("cartStoreIds") List<Long> cartStoreIds);

    interface CartStoreItemCount {
        Long getCartStoreId();
        Long getTotal();
    }
}
