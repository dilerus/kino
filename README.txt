Parametry programu:
 -u (URL) - adres sprawdzanej strony (domyslnie: https://trojmiasto.pl)
 -i (interwal) - czas miedzy odpytaniami strony, w sekundach (domyslnie: 10s)
 -f (finish) - ilosc iteracji programu
 -e (e-mail) - adres/y email na ktory chcemy otrzymac informacje o sukcesie, domyslnie: nie wysyla maila
 -s (sound) - czy program ma nadawac dzwiek w petli, nie potrzebuje dodatkowego parametru
 -date (date) - program bedzie sprawdzal czy jest juz po podanej dacie (dd-MM-yyyy), jak nie to zamknie program
 -d (day) - dzien tygodnia w ktorym zostanie uruchomiony skrypt (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
 -h (hour) - program bedzie sprawdzal czy jest juz po zadanej godzinie, i poczeka az bedzie po tej godzinie
 -p (phrases) - slowa/zdania/frazy ktore maja byc wyszukiwane na stronie, usuwane sa spacje i znaki specjalne, domyslnie: brak, program sprawdza tylko czy strona sie zmienila
 -n (negacja) - gdy ustawiona, program bedzie czekal az podane frazy znikna ze strony, nie potrzebuje dodatkowego parametru
Przyklad:  -u https://helios.pl -i 20 -f 100 -e example@gmail.com -s -p <strong>10</strong> <strong>11</strong>