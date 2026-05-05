## TODO

POST http://localhost:8082/apiFiles/find-files
responose -> taksId (wygeneruj UUID)

    GET http://localhost:8082/apiFiles/find-files/status/3
    zamiast 3 będziemy szukać po UUID

    Path -> Tabela
    1 kolumna - Path D/...
    2 kolumna - UUID task

    Relacje bazodanowe
    dwie tabele w postgresie
    testy integracyjne zobaczyc
    

Zrobic prostą stronę logowania:
- maven dependency spring-security
- bedziemy implmentowac authentykacje do seriwsu i autoryzacje do konkretnych ednpointow
  - 
