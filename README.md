# Anti-Arnaque — frontend Android

Application mobile Android (Java + XML) qui aide à repérer les SMS frauduleux :
l'utilisateur colle un message, obtient un **score de risque** et une **explication**,
consulte son **historique**, participe aux **signalements collectifs** et lit des
**conseils de sécurité**.

> Branche `frontend-etoundi` : écran d'accueil ajouté comme première interface,
> avec accès rapides et indicateurs locaux. L'onglet Conseils est conservé mais
> volontairement vide. Le reste de l'interface, la persistance SQLite, l'abstraction
> d'analyse et le client de signalements restent sur `frontend` (voir
> `docs/API_CONTRACT.md` et `docs/MODELE_IA.md`).

## Fonctionnalités livrées

| Fonctionnalité | Détail |
| --- | --- |
| Analyse de message | Score 0–100 %, niveau (faible / moyen / élevé), verdict, explications et signaux détectés |
| Moteur d'analyse | Heuristique locale explicable par défaut ; modèle distant via API Flask si configuré ; repli automatique hors ligne |
| Historique | SQLite local (`SQLiteOpenHelper`), liste filtrable visuellement, détail, suppression, effacement complet |
| Signalement collectif | Envoi d'une empreinte SHA-256 (jamais le texte brut), file d'attente SQLite, synchronisation manuelle |
| Conseil de sécurité | 7 réflexes affichés hors ligne |
| Configuration | Depuis l'onglet *Signalements*, saisir l'URL du backend (ex. `http://10.0.2.2:5000` sur émulateur) |
| Vie privée | `allowBackup=false`, trafic clair limité à localhost/10.0.2.2, consentement via dialogue avant signalement |

## Ouvrir dans Android Studio

1. Cloner le dépôt puis checkout la branche `frontend-etoundi` :
   ```bash
   git checkout frontend-etoundi
   ```
2. Ouvrir **le dossier racine** `Anti-Arnaque` dans Android Studio (pas le dossier `app`).
3. Laisser Android Studio télécharger les dépendances Gradle (wrapper 9.6.0, AGP 9.5.1).
4. Choisir un émulateur ou un téléphone en mode développeur, puis **Run ▶**.

SDK requis : Android SDK Platform 37, build-tools 36+, JDK 17 ou plus (JBR d'Android Studio convient).
`minSdk = 24`, `targetSdk = 37`.

### Commandes utiles

```bash
./gradlew :app:assembleDebug          # APK : app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:testDebugUnitTest      # tests JVM du classifieur
./gradlew :app:connectedDebugAndroidTest  # test instrumenté (appareil branché)
```

## Architecture

```
app/src/main/java/com/antiarnaque/app/
├── analysis/   SmsAnalyzer, HeuristicSmsAnalyzer, RemoteSmsAnalyzer, AnalyzerRepository
├── data/       AppDatabase (SQLite), HistoryDao, ReportDao, PreferencesManager
├── model/      AnalysisResult, HistoryEntry, ReportEntry, RiskLevel
├── report/     ReportService, HttpReportService, ReportManager
├── ui/         MainActivity + 4 fragments + HistoryAdapter
└── util/       AppExecutors, PrivacyUtils
```

Le **contrat d'analyse** est résumé dans [`docs/API_CONTRACT.md`](docs/API_CONTRACT.md).
Le branchement du **modèle TFLite** est décrit dans [`docs/MODELE_IA.md`](docs/MODELE_IA.md).
Le branchement **Firebase** est décrit dans [`docs/INTEGRATION_FIREBASE.md`](docs/INTEGRATION_FIREBASE.md).

## Éthique et vie privée

- L'analyse heuristique se fait **entièrement sur l'appareil**.
- Les signalements ne contiennent **ni le message en clair, ni numéro de téléphone** :
  seule une empreinte SHA-256, le score et un motif sont transmis.
- Aucune donnée n'est collectée sans action explicite de l'utilisateur et confirmation.
- Les seuils de risque et les performances du modèle doivent être documentés dans le
  rapport d'évaluation (précision, rappel, F1, matrice de confusion, déséquilibre des classes).

## État d'avancement

- [x] Interface 5 onglets (accueil / analyse / historique / signalements / conseils vides)
- [x] Analyse heuristique locale + explicabilité
- [x] Intégration API distante (Flask) avec repli hors ligne
- [x] Historique SQLite
- [x] Signalements anonymisés + file d'attente
- [x] Tests unitaires et de compilation
- [ ] Modèle TFLite embarqué (à fournir par la branche ML)
- [ ] Rapport d'évaluation du modèle (à fournir par la branche ML)
- [ ] compilation release signée (APK de distribution)
