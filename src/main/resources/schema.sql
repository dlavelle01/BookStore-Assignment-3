-- CREATE DATABASE IF NOT EXISTS BookShop;

-- H2 CREATE SCHEMA IF NOT EXISTS BookShop;

use BookShop;

CREATE TABLE IF NOT EXISTS `role`
(
    role_id     INT NOT NULL PRIMARY KEY,
    name        VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS `user`
(
    user_id               CHAR(36) PRIMARY KEY,
    user_name             VARCHAR(256) NOT NULL,
    `password`            VARCHAR(1024) NOT NULL,
    `salt`                VARCHAR(32) NOT NULL,
    `2FA`                 TINYINT(1) NOT NULL,
    `secret`              VARCHAR(64) NOT NULL,
    role_id               INT NOT NULL,
    last_modified_by      INT,
    last_modified_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    CONSTRAINT FK_User_Role FOREIGN KEY (role_id) REFERENCES `role`(role_id),
    CONSTRAINT UK_User_Name UNIQUE (user_name)
);

CREATE TABLE IF NOT EXISTS customer
(
    customer_id             	INT AUTO_INCREMENT PRIMARY KEY,
    user_id                     CHAR(36) NOT NULL,
    name                        VARCHAR(256) NOT NULL,
    surname                     VARCHAR(256) NOT NULL,
    date_of_birth            	DATE NOT NULL,
    address                     VARCHAR(2048) NOT NULL,
    phone_number        		VARCHAR(32) NOT NULL,
    email                       VARCHAR(512) NOT NULL,
    last_modified_date  		TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    CONSTRAINT FK_Customer_User FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS book
(
    book_id 		BIGINT AUTO_INCREMENT PRIMARY KEY,
    title			VARCHAR(2048) NOT NULL,
    isbn			VARCHAR(512) NOT NULL,
    author			VARCHAR(1024) NOT NULL,
    `year`			DATE,
    price			DECIMAL(5,2) DEFAULT 0 NOT NULL,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE IF NOT EXISTS inventory
(
    inventory_id 	            BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id			            BIGINT NOT NULL,
    copies			            INT NOT NULL DEFAULT 0,
    on_hold_for_customer_id		CHAR(36),
    created_date 	            TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    created_by		            INT,
    CONSTRAINT FK_Inventory_Book FOREIGN KEY (book_id) REFERENCES `book`(book_id) ON DELETE CASCADE,
    INDEX idx_inventory_book(book_id, copies, on_hold_for_customer_id)
);

-- ALTER TABLE inventory ADD INDEX idx_inventory_book(book_id, copies, on_hold_for_customer_id);

CREATE TABLE IF NOT EXISTS shopping_cart
(
	shopping_cart_id	BIGINT AUTO_INCREMENT PRIMARY KEY,
	book_id			    BIGINT NOT NULL,
	customer_id			INT NOT NULL,
    created_date 	    TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    abandoned			TINYINT(1) DEFAULT 0,
    CONSTRAINT FK_ShoppingCart_Book FOREIGN KEY (book_id) REFERENCES `book`(book_id) ON DELETE CASCADE,
    CONSTRAINT FK_ShoppingCart_Customer FOREIGN KEY (customer_id) REFERENCES `customer`(customer_id)
);

CREATE TABLE IF NOT EXISTS `order`
(
    order_id	        BIGINT AUTO_INCREMENT PRIMARY KEY,
    shopping_cart_id	BIGINT NOT NULL,
    created_date 	    TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    processed_date	    TIMESTAMP DEFAULT NULL,
    CONSTRAINT FK_Order_ShoppingCart FOREIGN KEY (shopping_cart_id) REFERENCES `shopping_cart`(shopping_cart_id) ON DELETE CASCADE
);