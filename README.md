# Shop — Plateforme e-commerce (Spring Boot Clean Architecture + Next.js)

Plateforme e-commerce complète : catalogue, panier, tunnel de commande avec paiement Stripe,
espace client et back-office administrateur.

## Démarrage rapide (Docker)

```bash
docker compose up --build
```

| Service | URL |
|---|---|
| Boutique (Next.js) | http://localhost:3000 |
| API (Spring Boot) | http://localhost:8080/api/products |
| MailDev (e-mails/factures) | http://localhost:1080 |

Compte administrateur par défaut : `admin@shop.com` / `Admin123!` (variables `ADMIN_EMAIL` / `ADMIN_PASSWORD`).
Le catalogue de démonstration (16 produits) est inséré par la migration Flyway `V2` ; `V3` rattache à chaque produit son illustration (`frontend/public/products/*.svg`).

### Paiement
* **Par défaut** : passerelle simulée (`PAYMENT_PROVIDER=fake`), aucune clé requise.
* **Stripe (sandbox)** : copier `.env.example` en `.env`, renseigner les clés de test, mettre `PAYMENT_PROVIDER=stripe`, puis
  `docker compose up --build`. Carte de test : `4242 4242 4242 4242`.
  Webhook en local : `stripe listen --forward-to localhost:8080/api/payments/webhook` (renseigner `STRIPE_WEBHOOK_SECRET`).

## Utiliser Neon comme base de données

1. Créez un projet sur [neon.tech](https://neon.tech) et ouvrez **Connect**.
2. Prenez la connexion **directe** (décochez *Connection pooling*) : c'est la plus sûre pour Flyway et les verrous
   `SELECT ... FOR UPDATE` du checkout. Convertissez-la au format JDBC :
   `jdbc:postgresql://<hôte>/<base>?sslmode=require` (utilisateur et mot de passe séparés).
3. Renseignez `NEON_JDBC_URL`, `NEON_USER`, `NEON_PASSWORD` dans `.env`, puis :
   ```bash
   docker compose -f docker-compose.neon.yml --env-file .env up --build
   ```
   Ou sans Docker : copiez `backend/.env.example` en `backend/.env`, remplissez-le, puis `mvn spring-boot:run` dans `backend/`
   (le fichier est chargé automatiquement ; lancez la commande depuis le dossier `backend`).

Flyway crée le schéma et les 16 produits de démo au premier démarrage. Si vous utilisez l'hôte *pooler* (`-pooler`),
ajoutez `&prepareThreshold=0` à l'URL. Le premier appel après une période d'inactivité peut prendre quelques
secondes (réveil de Neon) ; le timeout de connexion est réglé en conséquence. Les tests d'intégration continuent
d'utiliser Testcontainers, jamais votre base Neon.

## Déploiement (Neon + Render + Vercel)

1. **Base** : Neon (voir plus haut).
2. **Backend sur Render** : *New → Blueprint*, sélectionnez le dépôt (`render.yaml` est à la racine). Renseignez `DB_URL`,
   `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`. Notez l'URL obtenue (`https://xxx.onrender.com`).
3. **Frontend sur Vercel** : *Add New Project*, **Root Directory = `frontend`**, variable `NEXT_PUBLIC_API_URL` = URL Render
   (sans `/` final). Notez l'URL Vercel.
4. Retournez sur Render et mettez `CORS_ALLOWED_ORIGINS` = URL Vercel (sans `/` final), puis redéployez le backend.

## Développement local

```bash
# Base de données
docker compose up db maildev

# Backend (Java 21, Maven)
cd backend && mvn spring-boot:run

# Frontend (Node 20+)
cd frontend && cp .env.example .env.local && npm install && npm run dev
```

Tests backend : `mvn test` (unitaires + test d'architecture ArchUnit) ; `mvn verify` ajoute les tests
d'intégration `*IT` qui démarrent un vrai PostgreSQL via **Testcontainers** (Docker requis).

## Architecture backend (Clean Architecture / hexagonale)

```
com.shop
├── domain            Cœur métier pur (aucune dépendance Spring/JPA)
│   ├── model         User, Product, Order, OrderStatus, ... (règles : stock, transitions de statut)
│   ├── repository    Ports de persistance (interfaces)
│   └── exception     Erreurs métier
├── application       Cas d'usage (orchestration) + ports sortants
│   ├── port          PasswordHasher, TokenIssuer, PaymentGateway, EmailSender
│   └── usecase       auth / catalog / order / admin
├── infrastructure    Adaptateurs (détails techniques)
│   ├── persistence   Entités JPA, Spring Data, adaptateurs de repository
│   ├── security      JWT, filtre, Spring Security 6, BCrypt, RBAC
│   ├── payment       StripePaymentGateway / FakePaymentGateway
│   ├── mail          SmtpEmailSender (MailDev) / LoggingEmailSender
│   └── config        Initialisation de l'admin
└── presentation      API REST : contrôleurs, DTO, gestion globale des erreurs
```

La règle de dépendance (`presentation → application → domain ← infrastructure`) est vérifiée automatiquement
par `ArchitectureTest` (ArchUnit).

### Points techniques notables
* **Stock fiable** : verrou pessimiste (`SELECT ... FOR UPDATE`, ordre déterministe) lors de la réservation du stock au checkout.
* **Commande validée seulement après paiement** : la commande naît `PENDING` (stock réservé) ; elle passe `PAID` quand le
  fournisseur confirme (webhook Stripe signé ou vérification serveur du PaymentIntent). Idempotent.
* **Annulation** d'une commande non payée : le stock est restitué.
* **Machine à états** des commandes dans le domaine : `PENDING → PAID → SHIPPED → DELIVERED` (+ `PENDING → CANCELLED`).
* **RBAC** : `ROLE_CUSTOMER`, `ROLE_ADMIN` ; `/api/admin/**` réservé aux admins ; un client ne voit que ses commandes.
* **Flyway** : schéma versionné (`V1`) + données de démo (`V2`), Hibernate en `ddl-auto=validate`.
* **Recherche** multicritères (mots dans nom/description/catégorie, catégorie, fourchette de prix, stock) via `Specification`.

## API (extrait)

| Méthode | Route | Accès |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| GET | `/api/products?q=&category=&sort=&minPriceCents=&maxPriceCents=&inStock=&page=&size=` | public |
| GET | `/api/products/{id}`, `/api/categories` | public |
| POST | `/api/orders` (checkout → PaymentIntent) | client |
| POST | `/api/orders/{id}/confirm-payment`, `/api/orders/{id}/cancel` | client |
| GET | `/api/orders`, `/api/orders/{id}` | client (ses commandes) |
| GET/PUT | `/api/users/me` | client |
| POST | `/api/payments/webhook` | Stripe (signature) |
| GET/POST/PUT/PATCH/DELETE | `/api/admin/products...` | admin |
| GET/PATCH | `/api/admin/orders...`, `/api/admin/dashboard` | admin |

## Frontend
Next.js (App Router, TypeScript), Tailwind CSS, TanStack React Query (cache/appels API), Zustand (panier persistant
et session), Stripe Elements (`PaymentElement`).

## Pistes d'amélioration
Refresh tokens, upload d'images (S3/MinIO), recherche plein texte PostgreSQL (`tsvector`), file de messages pour les e-mails,
métriques Prometheus, CI GitHub Actions.
