use BookShop;

INSERT INTO `role`
    SELECT tmp.role_id, tmp.name
    FROM (
         SELECT 1 as role_id, 'ADMIN' as name
         UNION
         SELECT 2, 'CUSTOMER'
     )  tmp
         LEFT JOIN `role` r on r.role_id = tmp.role_id
WHERE r.role_id IS NULL;

INSERT INTO `user` (user_id, user_name, `password`, `salt`, role_id)
    SELECT tmp.user_id, tmp.name, tmp.pass, tmp.salt, tmp.role
    FROM (
         SELECT 'c51d81b9-542a-11f0-8264-0242ac1b0002' as user_id, 'admin' name, SHA2(CONCAT('S190T2', 'admin'), 256) pass, 'S190T2' salt, 1 role
         UNION
         SELECT '1fef2db3-542b-11f0-8264-0242ac1b0002', 'jane.doe', SHA2(CONCAT('S290X2', 'jane.doe'), 256), 'S290X2', 2
         ) tmp
    LEFT JOIN `user` r on r.user_id = tmp.user_id
    WHERE r.user_id IS NULL;


INSERT INTO book (title, isbn, author, `year`, price)
    SELECT tmp.title, tmp.isbn, tmp.author, tmp.yr, tmp.price
    FROM (
         SELECT 'The Clockmaker of Meridiem' title, '978-1-60309-452-8' isbn, 'Elora Finch' author, '2017-10-15' yr, 14.99 price
         UNION
         SELECT 'Quantum Espresso for Beginners', '978-0-262-03384-8', 'Dr. C. Maxwell Nguyen', '2020-03-09', 35.50
         UNION
         SELECT 'Through the Velvet Door', '978-1-4028-9462-6', 'J. R. Kessler', '2023-08-21', 21.75
         UNION
         SELECT 'Sketches from the Hollow Earth', '978-0-14-017739-8', 'Imani Osei', '2019-05-02', 18.00
     ) tmp
     LEFT JOIN book r on r.title = tmp.title
WHERE r.title IS NULL;

INSERT INTO customer(user_id, name, surname, date_of_birth, address, email, phone_number)
    SELECT tmp.user_id, tmp.name, tmp.surname, tmp.db, tmp.addr, tmp.email, tmp.phone
    FROM (
             SELECT '1fef2db3-542b-11f0-8264-0242ac1b0002' user_id, 'Jane' name, 'Doe' surname, '1981-04-02' db, 'Mainguard St, Galway' addr, 'jane.doe@gmail.com' email, '018118181' phone
         ) tmp
    LEFT JOIN customer c on c.user_id = tmp.user_id
    WHERE c.user_id IS NULL;

INSERT INTO inventory(book_id, copies, on_hold_for_customer_id)
    SELECT tmp.book_id, tmp.copies, tmp.on_hold_for_customer_id
    FROM (
             SELECT 1 book_id, 10 copies, null on_hold_for_customer_id
             UNION
             SELECT 1 book_id, -2 copies, '1fef2db3-542b-11f0-8264-0242ac1b0002' on_hold
             UNION
             SELECT 2, 5, null
         )  as tmp
             LEFT JOIN inventory i on i.book_id = tmp.book_id and i.copies = tmp.copies -- and COALESCE(i.on_hold_for_customer_id, 0) = and COALESCE(tmp.on_hold_for_customer_id, 0)
    WHERE i.inventory_id IS NULL;
