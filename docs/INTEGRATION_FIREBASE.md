# Branchement des signalements sur Firebase

Le frontend utilise l'interface `ReportService` (`app/.../report/`). L'implémentation
actuelle `HttpReportService` envoie les signalements anonymisés à `POST /api/reports`.
Pour utiliser **Cloud Firestore** à la place :

## 1. Dépendances

Dans `app/build.gradle` :

```groovy
implementation platform('com.google.firebase:firebase-bom:34.8.0')
implementation 'com.google.firebase:firebase-firestore'
```

## 2. Configuration sans `google-services.json`

Le projet utilise volontairement une initialisation manuelle pour rester compilable
sans secret versionné. Dans `FirebaseReportService` :

```java
FirebaseOptions options = new FirebaseOptions.Builder()
        .setApiKey("...")
        .setApplicationId("1:xxxxxxxx:android:xxxxxxxx")
        .setProjectId("anti-arnaque")
        .setStorageBucket("anti-arnaque.firebasestorage.app")
        .build();
FirebaseApp app = FirebaseApp.initializeApp(context, options, "anti-arnaque");
FirebaseFirestore db = FirebaseFirestore.getInstance(app);
```

Placez ces valeurs dans `res/values/firebase.xml` **non versionné** (ou dans
`BuildConfig` via `buildConfigField`) et ajoutez le fichier au `.gitignore`. Si vous
préférez le plugin Google Services classique, ajoutez
`id 'com.google.gms.google-services'` et déposez `google-services.json` à la racine
de `app/` (déjà ignoré par Git).

## 3. Implémentation

```java
public final class FirebaseReportService implements ReportService {
    @Override
    public boolean send(ReportEntry report) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("message_hash", report.getMessageHash());
        data.put("score", report.getScore());
        data.put("reason", report.getReason());
        data.put("created_at", FieldValue.serverTimestamp());
        Tasks.await(db.collection("reports").add(data));
        return true;
    }
}
```

Puis, dans `ReportManager.submit()` et `syncPending()`, remplacer
`new HttpReportService(endpoint)` par `new FirebaseReportService(context)`.

## 4. Règles de sécurité Firestore

Interdire la lecture des signalements aux utilisateurs anonymes et limiter l'écriture
à un document valide (empreinte SHA-256 de 64 caractères, score dans [0,1]) :

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /reports/{reportId} {
      allow read: if false;
      allow create: if request.resource.data.keys().hasAll(['message_hash','score','reason'])
                    && request.resource.data.message_hash.size() == 64
                    && request.resource.data.score is number
                    && request.resource.data.score >= 0
                    && request.resource.data.score <= 1;
      allow update, delete: if false;
    }
  }
}
```

L'empreinte SHA-256 n'est pas une anonymisation parfaite (un message connu peut être
retrouvé par recherche d'empreinte). Pour une production, ajouter un sel côté serveur
(HMAC) ou ne transmettre que des features agrégées.
