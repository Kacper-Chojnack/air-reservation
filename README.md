# System Rezerwacji Lotniczych (Air Reservation System)

## ✈️ Wprowadzenie

Projekt **Air Reservation System** to kompleksowa aplikacja webowa zbudowana w oparciu o Java Spring Boot, umożliwiająca użytkownikom wyszukiwanie lotów, dokonywanie rezerwacji miejsc oraz zarządzanie swoim profilem. Aplikacja zawiera również panel administracyjny do zarządzania lotami, pasażerami i rezerwacjami.

## ✨ Główne Funkcjonalności

### Dla Pasażerów:

*   **Wyszukiwanie Lotów:** Możliwość wyszukiwania dostępnych lotów na podstawie lotniska wylotu, lotniska przylotu (opcjonalnie) oraz daty wylotu. Wyniki są paginowane.
*   **Przeglądanie Lotów:** Wyświetlanie listy nadchodzących lotów na stronie głównej.
*   **Rejestracja:** Nowi użytkownicy mogą utworzyć konto, podając dane.
*   **Potwierdzenie E-mail:** Proces rejestracji wymaga potwierdzenia adresu e-mail poprzez kliknięcie linku aktywacyjnego wysłanego na podany adres (wykorzystuje Mailtrap do testowania).
*   **Logowanie / Wylogowanie:** Bezpieczne logowanie za pomocą Spring Security.
*   **Profil Użytkownika:** Zalogowani użytkownicy mogą przeglądać swoje dane oraz historię (przeszłe i przyszłe) rezerwacji.
*   **Rezerwacja Miejsca:** Możliwość wyboru konkretnego, dostępnego miejsca w samolocie dla wybranego lotu.
*   **Blokada Miejsca:** Podczas procesu rezerwacji wybrane miejsce jest tymczasowo blokowane (domyślnie na 1 minutę), aby zapobiec jednoczesnej rezerwacji przez różnych użytkowników.
*   **Finalizacja Rezerwacji Hasłem:** Aby sfinalizować rezerwację (po zablokowaniu miejsca), użytkownik musi potwierdzić operację swoim hasłem.
*   **Potwierdzenie Rezerwacji E-mail:** Po pomyślnej finalizacji rezerwacji, użytkownik otrzymuje e-mail z potwierdzeniem i szczegółami lotu.

### Dla Administratorów:

*   **Panel Administracyjny:** Dostępny pod `/admin`, zabezpieczony rolą ADMIN.
*   **Zarządzanie Lotami (CRUD):**
    *   Listowanie wszystkich lotów z paginacją.
    *   Tworzenie nowych lotów (wymaga podania lotnisk, samolotu, daty, numeru, czasu trwania).
    *   Edycja istniejących lotów.
    *   Usuwanie lotów (niemożliwe, jeśli istnieją powiązane rezerwacje).
*   **Zarządzanie Pasażerami (CRUD):**
    *   Listowanie wszystkich pasażerów z paginacją.
    *   Edycja danych pasażerów (imię, nazwisko, e-mail, telefon, rola, status konta - aktywne/nieaktywne).
    *   Możliwość resetowania hasła pasażera przez administratora.
    *   Usuwanie pasażerów (usuwa pasażera, nawet jeśli posiada rezerwacje - rezerwacje *nie są* automatycznie usuwane).
*   **Zarządzanie Rezerwacjami:**
    *   Listowanie wszystkich rezerwacji z paginacją i szczegółami.
    *   Edycja rezerwacji (obecnie *tylko zmiana numeru miejsca* na inne dostępne w danym locie).
    *   Usuwanie rezerwacji.

### Funkcjonalności Systemowe:

*   **Inicjalizacja Danych:** Przy pierwszym uruchomieniu (lub gdy tabele są puste), system automatycznie dodaje podstawowe dane: kraje, lotniska, typy samolotów, konto administratora (`admin`/`admin`) oraz przykładowe cykliczne harmonogramy lotów.
*   **Generowanie Cyklicznych Lotów:** Scheduler (`RecurringFlightGeneratorScheduler`) codziennie (o 1:00 w nocy) sprawdza zdefiniowane harmonogramy i generuje przyszłe loty, jeśli jeszcze nie istnieją.
*   **Czyszczenie Wygasłych Blokad:** Scheduler (`SeatLockCleanupScheduler`, `FlightStatusScheduler`) regularnie usuwa z bazy danych tymczasowe blokady miejsc, które wygasły.
*   **Obsługa Błędów:** Zdefiniowane typy błędów biznesowych (`BusinessException`, `ErrorType`) dla lepszej obsługi i informacji zwrotnej.
*   **Bezpieczeństwo:** Uwierzytelnianie i autoryzacja oparte na rolach, ochrona CSRF, hashowanie haseł.
*   **Mapowanie Obiektów:** Użycie MapStruct do efektywnego mapowania między encjami JPA a obiektami DTO.

## 🛠️ Technologie i Narzędzia

*   **Język:** Java 21
*   **Framework:** Spring Boot 3.4.4
    *   Spring Web (MVC)
    *   Spring Security
    *   Spring Data JPA
    *   Spring Validation
    *   Spring Scheduling
    *   Spring Mail
*   **Silnik Szablonów:** Thymeleaf
*   **ORM:** Hibernate
*   **Baza Danych:** PostgreSQL
*   **Budowanie Projektu:** Maven
*   **Narzędzia Pomocnicze:**
    *   Lombok 
    *   MapStruct 
*   **Logowanie:** SLF4J 
*   **Testowanie E-maili:** Mailtrap (konfiguracja w `application.properties` lub w pliku env.example dla docker compose)
*   **Konteneryzacja:** Docker

## ⚙️ Konfiguracja i Uruchomienie

### Wymagania Wstępne

*   JDK 21 lub nowszy
*   Maven 3.6+
*   Działająca instancja PostgreSQL (lub Docker)
*   Konto Mailtrap do testowania wysyłki e-maili.
*   Docker i Docker Compose

### Konfiguracja

1.  **Klonowanie Repozytorium:**
    ```bash
    git clone [[<URL_REPOZYTORIUM>](https://github.com/Kacper-Chojnack/air-reservation/tree/master)](https://github.com/Kacper-Chojnack/air-reservation.git)
    cd airreservation
    ```
2.  **Konfiguracja Bazy Danych i Poczty:**
    *   Uzupełnij plik .env.example zgodnie z szablonem, a następnie zapisz plik jako .env:
        ```dotenv
        DB_USER=postgres
        DB_PASSWORD=twoje_bezpieczne_haslo_tutaj # Zmień na bezpieczne hasło!
        DB_NAME=airreservation
        MAIL_HOST=sandbox.smtp.mailtrap.io
        MAIL_PORT=2525
        MAIL_USERNAME=TWOJ_MAILTRAP_USERNAME     # Zmień na swoje dane Mailtrap
        MAIL_PASSWORD=TWOJ_MAILTRAP_PASSWORD     # Zmień na swoje dane Mailtrap
        MAIL_FROM=no-reply@airreservation.test
        APP_BASE_URL=http://localhost:8080       # Ustaw poprawny port jeśli zmieniasz APP_PORT
        APP_PORT=8080                            # Port na którym aplikacja będzie dostępna na hoście
        ```

### Uruchomienie z Docker Compose

1.  **Upewnij się, że Docker jest zainstalowany.**
2.  **Utwórz i skonfiguruj plik `.env`** (jak opisano w sekcji Konfiguracja).
3.  **Uruchom kontenery:**
    ```bash
    docker compose up --build -d
    ```
4.  Aplikacja będzie dostępna pod adresem `http://localhost:8080` (lub portem zdefiniowanym w `APP_PORT` w pliku `.env`). Baza danych PostgreSQL będzie działać w osobnym kontenerze (`airreservation_db`).

## 🔑 Dostęp do Aplikacji

*   **Strona Główna / Wyszukiwarka:** `http://localhost:8080/`
*   **Logowanie:** `http://localhost:8080/login`
*   **Rejestracja:** `http://localhost:8080/passengers/create`
*   **Panel Admina:** `http://localhost:8080/admin` (wymaga zalogowania jako admin)

*   **Domyślne dane logowania administratora (dostępne w celach testowych):**
    *   **Email:** `admin`
    *   **Hasło:** `admin`
 
*   **Domyślne dane logowania pasażera (dostępne w celach testowych):**
    *   **Email:** `pasazer@mail.pl`
    *   **Hasło:** `pasazer`

*   **Rejestracja:** Po rejestracji nowego użytkownika, sprawdź skrzynkę Mailtrap (lub skonfigurowany serwer SMTP), aby znaleźć e-mail aktywacyjny i kliknąć link. Dopiero po aktywacji konto będzie włączone i możliwe będzie zalogowanie.

### Planowane ulepszenia:

*   **Pełna obsługa i wyszukiwarka lotów w dwie strony:** 
*   **Generowanie biletów kodem QR:** 
