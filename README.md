# FitConnect — plateforme de réservation de cours

Projet microservices du TP « Système de Gestion de Réservation et de Paiement pour une Salle de Sport ».
Projet à but éducatif réalisé par Emmanuelle CURIANT et Liam GATTEGNO, liste des livrables attendus :
- [x] Code source
- [x] Fichiers de configuration dans config-repo
- [x] Routes dans api-gateway.yml
- [x] Clients Feign avec Circuit Breaker
- [x] Pattern Saga complet (réservation → paiement → confirmation)
- [x] Verrouillage optimiste dans class-service
- [x] Scheduler pour l'expiration des paiements
- [x] Collection Postman complète
- [x] Tests unitaires et d'intégration
- [x] README détaillé
- [x] Docker Compose pour 6 services (bonus)

## Stack

- Spring Boot 4.1.1
- Spring Cloud 2025.1.3
- Java 25 / Maven 3
- Eureka, Config Server et Spring Cloud Gateway
- H2 isolée par service
- Feign et Circuit Breaker Resilience4j
- OpenAPI et collection Postman

## Démarrer

```bash
mvn clean verify
docker compose build
docker compose up -d
```

Les cours de démonstration sont créés automatiquement par `class-service`. Le gateway est disponible sur `http://localhost:8080`.

Swagger UI :

```text
http://localhost:8091/swagger-ui/index.html
http://localhost:8092/swagger-ui/index.html
http://localhost:8093/swagger-ui/index.html
http://localhost:8094/swagger-ui/index.html
```

## Workflow principal

1. Créer une réservation avec le header `Idempotency-Key`.
2. Les places sont réservées et le statut est `PENDING_PAYMENT`.
3. Confirmer le paiement avec `PATCH /api/bookings/{id}/confirm`.
4. Le paiement inférieur à 100 € devient `SUCCESS`.
5. La réservation devient `CONFIRMED` et une notification est enregistrée.
6. Une annulation valide rembourse et libère les places.

## Qualité

```bash
mvn clean verify
docker compose config
```

JaCoCo bloque la build à moins de 100 % de lignes et de branches sur le périmètre métier configuré.
