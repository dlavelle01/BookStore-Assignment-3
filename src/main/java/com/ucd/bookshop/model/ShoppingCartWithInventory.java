package com.ucd.bookshop.model;

/**
 * POJO for shopping cart
 */
public class ShoppingCartWithInventory extends ShoppingCart {

    private Long copies;

    public ShoppingCartWithInventory(Long id, Book book, Customer customer,
            Boolean abandoned, Long copies) {
        super(book, customer, abandoned);

        this.setId(id);
        this.copies = copies;
    }

    public ShoppingCartWithInventory(ShoppingCart shoppingCart, Long copies) {
        super(shoppingCart.getBook(), shoppingCart.getCustomer(), shoppingCart.getAbandoned());

        this.setId(shoppingCart.getId());
        this.copies = copies;
    }

    public Long getCopies() {
        return copies;
    }

    public void setCopies(Long copies) {
        this.copies = copies;
    }

    /**
     * Check if the book in this shopping car has inventory
     */
    public boolean isBookAvailable() {
        return copies != null && copies > 0;
    }

    /**
     * Convert back
     */
    public ShoppingCart toShoppingCart() {
        ShoppingCart cart = new ShoppingCart(getBook(), getCustomer(), getAbandoned());
        cart.setId(getId());
        cart.setCreatedDate(getCreatedDate());
        return cart;
    }
}