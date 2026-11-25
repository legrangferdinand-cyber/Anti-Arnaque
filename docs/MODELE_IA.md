# Branchement du modèle d'IA (TFLite)

Le frontend ne dépend pas d'une bibliothèque de machine learning : il consomme le
**contrat** décrit dans `API_CONTRACT.md`. Deux chemins sont possibles.

## Option A — Flask (recommandée pour démarrer)

1. Entraîner le classifieur binaire en Python sur un dataset public de spam SMS
   (ex. `UCI SMS Spam Collection`) complété par des exemples synthétiques locaux.
2. Exposer `POST /api/analyze` comme dans `API_CONTRACT.md`.
3. Dans l'application, onglet **Signalements → Configurer le serveur**, saisir l'URL.
4. Le frontend appelle automatiquement le modèle ; l'heuristique sert de repli.

Le rapport d'évaluation doit préciser : accuracy, precision, recall, F1, matrice de
confusion et gestion du déséquilibre des classes (`class_weight`, sur-échantillonnage
ou seuil de décision ajusté). Mesurer séparément les faux positifs (un SMS légitime
classé arnaque) car ils sont les plus coûteux pour l'acceptabilité de l'application.

## Option B — TFLite embarqué

1. Convertir le modèle entraîné en `spam_model.tflite` et le déposer dans
   `app/src/main/assets/`.
2. Ajouter la dépendance TensorFlow Lite dans `app/build.gradle` :
   ```groovy
   implementation 'org.tensorflow:tensorflow-lite:2.17.0'
   ```
3. Créer `TfliteSmsAnalyzer implements SmsAnalyzer` dans le package `analysis` :
   - construire l'`Interpreter` depuis `assets/spam_model.tflite` ;
   - tokenizer le message (vocabulaire exporté depuis Python dans `assets/vocab.txt`) ;
   - produire un `float[]` de probabilités d'entrée ;
   - retourner un `AnalysisResult` avec `engine = "tflite local"`.
4. Dans `AnalyzerRepository`, insérer le nouvel analyseur **avant** le repli heuristique :
   ```java
   try {
       return new TfliteSmsAnalyzer(context).analyze(message);
   } catch (Exception e) {
       return heuristic.analyze(message);
   }
   ```

L'interface `SmsAnalyzer` garantit que ni les fragments ni la base SQLite n'ont à
changer : le score, le libellé, les explications et le nom du moteur suivent le même
format.

## Confidentialité des explications

Le modèle doit renvoyer des **signaux compréhensibles** (mots-clés ou features) plutôt
qu'une décision opaque. C'est une exigence pédagogique et éthique du projet :
l'utilisateur doit pouvoir comprendre pourquoi un message est considéré comme risqué.
