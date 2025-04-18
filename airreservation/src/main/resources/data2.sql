INSERT INTO country (name) VALUES
('Polska'),
('Niemcy'),
('Francja'),
('Hiszpania'),
('Włochy'),
('Wielka Brytania'),
('Stany Zjednoczone'),
('Holandia'),
('Turcja'),
('Grecja'),
('Egipt'),
('Zjednoczone Emiraty Arabskie'),
('Tajlandia'),
('Chiny'),
('Japonia'),
('Kanada'),
('Austria'),
('Czechy'),
('Szwecja'),
('Norwegia');

INSERT INTO airport (name, country_id) VALUES
('Lotnisko Frankfurt', (SELECT id FROM Country WHERE name = 'Niemcy')),
('Lotnisko Berlin-Brandenburg', (SELECT id FROM Country WHERE name = 'Niemcy')),
('Lotnisko Paryż-Charles de Gaulle', (SELECT id FROM Country WHERE name = 'Francja')),
('Lotnisko Madryt-Barajas', (SELECT id FROM Country WHERE name = 'Hiszpania')),
('Lotnisko Rzym-Fiumicino', (SELECT id FROM Country WHERE name = 'Włochy')),
('Lotnisko Szczecin-Goleniów', (SELECT id FROM Country WHERE name = 'Polska')),
('Lotnisko Bydgoszcz-Ignacy Jan Paderewski', (SELECT id FROM Country WHERE name = 'Polska')),
('Lotnisko Zielona Góra-Babimost', (SELECT id FROM Country WHERE name = 'Polska')),
('Lotnisko Olsztyn-Mazury', (SELECT id FROM Country WHERE name = 'Polska')),
('Lotnisko Radom', (SELECT id FROM Country WHERE name = 'Polska')),
('Lotnisko Lublin-Świdnik', (SELECT id FROM Country WHERE name = 'Polska')),
('Lotnisko Białystok-Krywlany', (SELECT id FROM Country WHERE name = 'Polska')),
('Lotnisko Koszalin-Zegrze Pomorskie', (SELECT id FROM Country WHERE name = 'Polska')),
('Lotnisko Słupsk-Redzikowo', (SELECT id FROM Country WHERE name = 'Polska')),
('Lotnisko Kielce-Masłów', (SELECT id FROM Country WHERE name = 'Polska')),
('Lotnisko Nowy Targ', (SELECT id FROM Country WHERE name = 'Polska')),
('Lotnisko Warszawa-Modlin', (SELECT id FROM Country WHERE name = 'Polska'));

INSERT INTO airplane (name, total_seats) VALUES ('Boeing 737', 189);
INSERT INTO airplane (name, total_seats) VALUES ('Airbus A320', 180);
INSERT INTO airplane (name, total_seats) VALUES ('Boeing 777', 396);
INSERT INTO airplane (name, total_seats) VALUES ('Airbus A380', 853);
INSERT INTO airplane (name, total_seats) VALUES ('Boeing 787 Dreamliner', 335);

