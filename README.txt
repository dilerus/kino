Parametry programu:
-u (URL) - adres sprawdzanej strony (domyslnie: https://trojmiasto.pl)
-i (interwal) - czas miedzy odpytaniami strony, w sekundach (domyslnie: 10s)
-f (finish) - ilosc iteracji programu (domyslnie: 1.000.000)
-e (e-mail) - adres email na ktory chcemy otrzymac informacje o sukcesie, domyslnie: nie wysyla maila
-s (sound) - czy program ma nadawac dzwiek w petli, nie potrzebuje dodatkowego parametru
-p (phrases) - slowa/zdania/frazy ktore maja byc wyszukiwane na stronie, usuwane sa spacje i znaki specjalne, domyslnie: brak, program sprawdza tylko czy strona sie zmienila
-d (day) - dzien tygodnia w ktorym zostanie uruchomiony skrypt (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
-h (hour) - program bedzie sprawdzal czy jest juz po zadanej godzinie i poczeka az bedzie po tej godzinie
-date (date) - program bedzie sprawdzal czy jest juz po podanej dacie (dd-MM-yyyy), jak nie to zamknie program
Przyklad:  -u https://helios.pl -i 20 -f 100 -e example@gmail.com -s -p <strong>10</strong> <strong>11</strong>