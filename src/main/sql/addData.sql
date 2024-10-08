insert into users(name, email, password, token) values ('Mota', 'random@gmail.com', 'lsg11', '678678'),
                                                       ('Rodnocka', 'redneckted@gmail.com', 'lsg11', '678678678'),
                                                       ('Toras', 'toras@gmail.com', 'lsg11', '67867867878');

insert into boards(description, name) values    ('Trabalho de LS', 'LS'),
                                                ('Trabalho de PC', 'PC');

insert into user_board(userNumber, boardId) values  (1,1),
                                                    (1,2),
                                                    (2,2);

insert into lists(name, boardId) values ('Done', 1),
                                        ('Doing', 1),
                                        ('Todo', 1),
                                        ('Done', 2),
                                        ('Doing', 2),
                                        ('Todo', 2);

insert into cards(index,name, description, creationDt, conclusionDt, listId, boardId)
    values  (1,'Card 1', 'Test', current_date, null, 1, 1),
            (1,'Card2 1', 'Test', current_date, null, 4, 2),
            (2,'Card2 2', 'Test', current_date, null, 4, 2),
            (3,'Card2 3', 'Test', current_date, null, 4, 2),
            (4,'Card2 4', 'Test', current_date, null, 4, 2),
            (1,'Card3 1', 'Test', current_date, null, 5, 2),
            (2,'Card3 2', 'Test', current_date, null, 5, 2),
            (3,'Card3 3', 'Test', current_date, null, 5, 2),
            (3,'Card3 3', 'Test', current_date, null, null, 1),
            (3,'Card3 3', 'Test', current_date, null, null, 2);
