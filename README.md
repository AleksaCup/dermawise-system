# DermaWise

Expert system for skin condition diagnostics and dermatological therapy recommendations, built with Drools.
Project for the course "Sistemi bazirani na znanju" (SBNZ), FTN Novi Sad.

The project proposal is in `DermaWise PP - final.pdf`.

## What it does

- Fitzpatrick phototype and MED from the questionnaire
- acne (GAGS), rosacea with NRS subtype, perioral dermatitis, atopic dermatitis (SCORAD), seborrheic and contact dermatitis
- excluded differential diagnoses with a reason
- morning and evening routine with ingredient, concentration and frequency, based on AAD guidelines
- safety checks: pregnancy, benzoyl peroxide with a retinoid, known allergies, maximum concentration per phototype
- skin diary with CEP rules (flushing, UV dose against MED, reactions to new products)
- explanation of every fired rule with its source
- dermatologist review of prescription items, runtime editing of the concentration template

## Modules

| Module | Package | Contents |
|---|---|---|
| `model` | `com.ftn.sbnz.model` | facts, enums, questionnaire classes, diary events, analysis result |
| `kjar` | `com.ftn.sbnz.kjar` | DRL rules, templates, knowledge base factory, rule tests |
| `service` | `com.ftn.sbnz.service` | Spring Boot REST API, the only module that runs |

`kjar` depends on `model`, `service` depends on `kjar` and `model`.
The root `pom.xml` builds them in that order.

## Knowledge base

| File | Rules | Phase (agenda group) |
|---|---|---|
| `rules/phototype.drl` | 10 | phototype |
| `rules/diagnosis.drl` | 36 | diagnosis |
| `rules/therapy.drl` | 26 | therapy |
| `rules/safety.drl` | 9 | safety |
| `rules/cep_diary.drl` | 7 | CEP, stream mode |
| `rules/backward.drl` | 15 queries | used by the other files and the backward endpoints |
| `templates/SeverityCategory.drt` | 7 (one per row of `severity-categories.csv`) | diagnosis |
| `templates/MaxConcentrationByPhototype.drt` | 2 per row (rows are in the database) | safety |

Three knowledge bases are built in code by `KnowledgeBaseFactory`:

- `analysis`: phototype, diagnosis, therapy, safety, backward and both templates
- `backward`: backward, phototype and diagnosis, for hypothesis checks
- `cep`: cep_diary, safety, backward and the concentration template, in stream mode

When a dermatologist changes a template row, the service rebuilds all three and swaps them in without a restart.

## Requirements

- JDK 17
- Docker with Docker Compose

Maven does not have to be installed, the project uses the Maven wrapper.

## Build and test

```
./mvnw clean install
```

This builds `model`, then `kjar`, then `service`, and runs all tests.
Service tests use an in memory H2 database.

## Run with Docker

```
docker compose up --build
```

This starts PostgreSQL 16 and the service on `http://localhost:8080` with the `dev` profile.
PostgreSQL is exposed on host port `5433`.

Optional environment variables (for example in a `.env` file):

| Variable | Default |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | `dermawise` |
| `POSTGRES_PORT` | `5433` |
| `SERVICE_PORT` | `8080` |
| `SPRING_PROFILES_ACTIVE` | `dev` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` |

Stop with `docker compose down`. Add `-v` to also remove the database volume.

## Run locally

```
docker compose up -d postgres
./mvnw -pl service -am install -DskipTests
SPRING_PROFILES_ACTIVE=dev ./mvnw -pl service spring-boot:run
```

`JWT_SECRET` (base64, at least 64 bytes) should be set outside of local development.

## Demo accounts

Created on the first start:

| Email | Password | Role | Notes |
|---|---|---|---|
| `admin@dermawise.rs` | `admin123` | ADMIN | |
| `dermatolog@dermawise.rs` | `derm123` | DERMATOLOGIST | |
| `ana@dermawise.rs` | `user123` | USER | moderate acne, phototype II |
| `milica@dermawise.rs` | `user123` | USER | same questionnaire, pregnant |

## Swagger UI

`http://localhost:8080/swagger-ui.html`

Log in with `POST /api/auth/login`, copy the token and paste it into "Authorize".

## Demo scenarios

The `demo/` folder has request files for the VS Code REST Client extension.
Run them in order, each request has a comment that says what it shows.

| File | Scenario |
|---|---|
| `01-auth.http` | login for every role, 400, 401 and 403 |
| `02-fc1-acne.http` | forward chaining example 1: moderate acne on phototype II, then the same user pregnant |
| `03-fc2-rosacea-perioral.http` | forward chaining example 2: papulopustular rosacea, then perioral dermatitis |
| `04-bc1-rosacea-hypothesis.http` | backward chaining example 1: `imaRosaceju` with the findings it was proven through |
| `05-bc2-contact-dermatitis.http` | backward chaining example 2: contact dermatitis culprit through Kathon CG |
| `06-cep1-flushing.http` | CEP example 1: flushing episodes, rosacea suspicion and dominant trigger |
| `07-cep2-sun-photoaging.http` | CEP example 2: UV dose against MED, photoaging risk, tretinoin withdrawn for a pregnant user |
| `08-template-runtime.http` | template row added at runtime changes the next analysis |
| `09-dermatologist-review.http` | dermatologist approves and rejects prescription items |
| `10-admin-knowledge-base.http` | knowledge base overview and user management |

`POST /api/demo/cep-week` (only with the `dev` profile) fills a week of diary entries for the logged in user.

## API

| Path | Role | Purpose |
|---|---|---|
| `/api/auth` | public | register, login |
| `/api/profile` | USER | questionnaire |
| `/api/analysis` | USER | run the analysis, latest result, history |
| `/api/backward` | USER, DERMATOLOGIST | hypotheses, contact dermatitis cause, allergen check |
| `/api/diary` | USER | flushing, sun, new product and symptom entries |
| `/api/cep` | USER | diary alerts and recommendations |
| `/api/dermatologist` | DERMATOLOGIST | patients, profiles, review of prescription items |
| `/api/templates` | DERMATOLOGIST | `MaxConcentrationByPhototype` rows and the generated DRL |
| `/api/admin` | ADMIN | users, knowledge base overview |

## Literature

- Fitzpatrick TB. Soleil et peau. J Med Esthet 1975
- Doshi A, Zaheer A, Stiller MJ. Global Acne Grading System. Int J Dermatol 1997
- Wilkin J et al. Standard classification of rosacea (NRS). J Am Acad Dermatol 2002
- European Task Force on Atopic Dermatitis. The SCORAD index. Dermatology 1993
- Glogau RG. Aesthetic and anatomic analysis of the aging skin. Semin Cutan Med Surg 1996
- Zaenglein AL et al. Guidelines of care for the management of acne vulgaris. J Am Acad Dermatol 2016
- van Zuuren EJ et al. Interventions for rosacea based on the phenotype approach. Br J Dermatol 2019
- Sidbury R et al. Guidelines of care for the management of atopic dermatitis. J Am Acad Dermatol 2014
