# Zadanie dockerfile: opis i budowa obrazu aplikacji FilesApi
# Używamy oficjalnego obrazu Mavena z Javą 21 do budowania aplikacji
# "builder" to alias tego etapu - przyda się w kolejnym kroku
FROM maven:3.9-eclipse-temurin-21 AS builder

# Ustawiamy katalog roboczy wewnątrz kontenera
# Wszystkie kolejne komendy będą wykonywane w /app
WORKDIR /app

# Kopiujemy tylko plik pom.xml (opis zależności projektu)
# Robimy to osobno żeby Docker mógł zcache'ować pobrane zależności
COPY pom.xml .

# Pobieramy wszystkie zależności z Maven bez budowania kodu
# Dzięki temu przy kolejnym buildzie, jeśli pom.xml się nie zmienił,
# Docker użyje cache i nie będzie pobierał zależności od nowa
RUN mvn dependency:go-offline

# Kopiujemy cały kod źródłowy do kontenera
COPY src ./src

# Budujemy aplikację - kompilujemy kod i pakujemy do pliku .jar
# -DskipTests pomija testy żeby przyspieszyć budowanie
RUN mvn package -DskipTests


# ---- DRUGI ETAP - obraz produkcyjny ----
# Używamy lekkiego obrazu tylko z JRE (do uruchamiania, nie kompilowania)
# Ten obraz będzie znacznie mniejszy niż obraz z Mavenem
FROM eclipse-temurin:21-jre-alpine

# Ustawiamy katalog roboczy w docelowym kontenerze
WORKDIR /app

# Kopiujemy zbudowany plik .jar z pierwszego etapu (builder)
# target/*.jar - bierzemy plik jar z folderu target
# app.jar - nazywamy go app.jar dla uproszczenia
COPY --from=builder /app/target/*.jar app.jar

# Informujemy Dockera że aplikacja będzie nasłuchiwać na porcie 8082
# To tylko dokumentacja - nie otwiera portu automatycznie
EXPOSE 8082

# Komenda która uruchomi się gdy kontener zostanie wystartowany
# Uruchamiamy nasz plik app.jar przez Javę
CMD ["java", "-jar", "app.jar"]