cd C:\personal-projects\kino4\out\artifacts\kino4_jar
java -jar Kino4.jar -u https://www.muzyczny.org/pl/repertuar.html -i 1800 -s -e dilerus@gmail.com -p "01</div><div class=""h6"">niedziela</div></div><div class=""d-none d-md-block col-md-1 separator""></div><div class=""col-sm-8 col-md-7 spektakl_szczegoly""><div class=""h2"">QUO VADIS</div>" "02</div><div class=""h6"">niedziela</div></div><div class=""d-none d-md-block col-md-1 separator""></div><div class=""col-sm-8 col-md-7 spektakl_szczegoly""><div class=""h2"">QUO VADIS</div>" "03</div><div class=""h6"">niedziela</div></div><div class=""d-none d-md-block col-md-1 separator""></div><div class=""col-sm-8 col-md-7 spektakl_szczegoly""><div class=""h2"">QUO VADIS</div>"


:: Parametry programu:
:: -u (URL) - adres sprawdzanej strony (domyslnie: https://trojmiasto.pl)
:: -i (interwal) - czas miedzy odpytaniami strony, w sekundach (domyslnie: 10s)
:: -f (finish) - ilosc iteracji programu (domyslnie: 1.000.000)
:: -e (e-mail) - adres email na ktory chcemy otrzymac informacje o sukcesie, domyslnie: nie wysyla maila
:: -s (sound) - czy program ma nadawac dzwiek w petli, nie potrzebuje dodatkowego parametru
:: -p (phrases) - slowa/zdania/frazy ktore maja byc wyszukiwane na stronie, usuwane sa spacje i znaki specjalne, domyslnie: brak, program sprawdza tylko czy strona sie zmienila
:: -d (day) - dzien tygodnia w ktorym zostanie uruchomiony skrypt (MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
:: -h (hour) - program bedzie sprawdzal czy jest juz po zadanej godzinie i poczeka az bedzie po tej godzinie
:: -date (date) - program bedzie sprawdzal czy jest juz po podanej dacie (dd-MM-yyyy), jak nie to zamknie program
:: -vb (value bigger) - gdy ustawiona, pierewszy parametr jest fragmentem strony przed wartoscia szukana, a drugi parametr jest wartoscia progowa po przekroczeniu ktorej bedzie sukces
:: -vs (value smaller) - gdy ustawiona, pierewszy parametr jest fragmentem strony przed wartoscia szukana, a drugi parametr jest wartoscia progowa po przekroczeniu ktorej bedzie sukces
:: Przyklad:  -u https://helios.pl -i 20 -f 100 -e example@gmail.com -s -p <strong>10</strong> <strong>11</strong>


:: java -jar Kino4.jar -u https://helios.pl/gdansk/kino-helios-forum/repertuar -p furiosa -s -e dilerus@gmail.com lukasz.miler@capgemini.com -d TUESDAY -date 14-05-2024 -h 12
:: java -jar kino4.jar -u https://helios.pl/gdansk/kino-helios-forum/filmy/diuna-czesc-druga-719 -i 10 -p "15 marca 2024"
:: java -jar Kino4.jar -u https://tvn24.pl -i 60 -p atomo -e lukasz.miler@capgemini.com
:: java -jar kino4.jar -u https://www.trojmiasto.pl -i 5
:: java -jar Kino4.jar -u https://www.muzyczny.org/pl/repertuar.html -i 15 -s -f 1 -e dilerus@gmail.com lukasz.miler@capgemini.com -p "01</div><div class=""h6"">niedziela</div></div><div class=""d-none d-md-block col-md-1 separator""></div><div class=""col-sm-8 col-md-7 spektakl_szczegoly""><div class=""h2"">QUO VADIS</div>" "02</div><div class=""h6"">niedziela</div></div><div class=""d-none d-md-block col-md-1 separator""></div><div class=""col-sm-8 col-md-7 spektakl_szczegoly""><div class=""h2"">QUO VADIS</div>" "03</div><div class=""h6"">niedziela</div></div><div class=""d-none d-md-block col-md-1 separator""></div><div class=""col-sm-8 col-md-7 spektakl_szczegoly""><div class=""h2"">QUO VADIS</div>" "04</div><div class=""h6"">niedziela</div></div><div class=""d-none d-md-block col-md-1 separator""></div><div class=""col-sm-8 col-md-7 spektakl_szczegoly""><div class=""h2"">QUO VADIS</div>" "05</div><div class=""h6"">niedziela</div></div><div class=""d-none d-md-block col-md-1 separator""></div><div class=""col-sm-8 col-md-7 spektakl_szczegoly""><div class=""h2"">QUO VADIS</div>" "06</div><div class=""h6"">niedziela</div></div><div class=""d-none d-md-block col-md-1 separator""></div><div class=""col-sm-8 col-md-7 spektakl_szczegoly""><div class=""h2"">QUO VADIS</div>"

