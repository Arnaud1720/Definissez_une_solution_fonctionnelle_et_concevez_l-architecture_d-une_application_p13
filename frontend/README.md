# Your Car Your Way - Frontend

Application Angular 17 pour la location de voitures en Europe.

## 🚀 Stack technique

- **Angular 17** (Standalone Components)
- **Tailwind CSS** pour le styling
- **RxJS** pour la gestion des flux
- **Signals** pour la réactivité

## 📁 Structure du projet

```
src/
├── app/
│   ├── core/                    # Services, guards, interceptors, models
│   │   ├── guards/
│   │   ├── interceptors/
│   │   ├── models/
│   │   └── services/
│   ├── features/                # Composants par fonctionnalité
│   │   ├── auth/               # Login, Register
│   │   ├── home/               # Page d'accueil
│   │   ├── search/             # Recherche et résultats
│   │   ├── profile/            # Profil utilisateur
│   │   ├── reservations/       # Liste des réservations
│   │   └── payment/            # Pages succès/annulation
│   └── shared/                 # Composants partagés (Navbar)
├── environments/
└── styles.scss
```

## 🛠️ Installation

```bash
# Installer les dépendances
npm install

# Lancer le serveur de développement
ng serve

# Build de production
ng build
```

## ⚙️ Configuration

Modifier `src/environments/environment.ts` pour l'URL du backend :

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/api'
};
```

## 🔒 Authentification

- JWT stocké dans localStorage
- Interceptor ajoute automatiquement le token aux requêtes
- Guards protègent les routes authentifiées

## 💳 Paiement Stripe

Le flux de paiement :
1. Utilisateur clique "Réserver et payer"
2. Frontend appelle `/api/payments/create-checkout-session`
3. Redirection vers Stripe Checkout
4. Après paiement → `/payment/success` ou `/payment/cancel`

## 📝 Composants

Chaque composant suit la structure Angular standard :
- `component.ts` - Logique
- `component.html` - Template
- `component.scss` - Styles

## 🎨 Styles

Classes Tailwind personnalisées dans `styles.scss` :
- `.btn-primary`, `.btn-secondary`, `.btn-outline`, `.btn-danger`
- `.input`, `.label`
- `.card`, `.container`
# Definissez_une_solution_fonctionnelle_et_concevez_l-architecture_d-une_application_frontend
