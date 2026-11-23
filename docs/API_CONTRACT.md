# Contrat d'API entre le frontend Android et le backend

Deux endpoints sont utilisés par l'application. Les deux acceptent du JSON et
répondent du JSON. Aucune authentification n'est imposée par le frontend (à ajouter
côté serveur pour la production).

## 1. Analyse — `POST /api/analyze`

Requête :

```json
{ "message": "Félicitations, vous avez gagné..." }
```

Réponse attendue :

```json
{
  "score": 0.93,
  "label": "spam",
  "risk_level": "ELEVE",
  "reasons": [
    "Promesse de gain inhabituelle",
    "Demande de code confidentiel"
  ],
  "signals": ["gagn", "code de validation"],
  "engine": "tflite"
}
```

- `score` : probabilité que le message soit frauduleux, **entre 0 et 1**.
  Un pourcentage 0–100 est accepté et converti automatiquement.
- `label` : `"spam"` ou `"legit"`.
- `risk_level` : optionnel (`FAIBLE`, `MOYEN`, `ELEVE`). Si absent, les seuils du client
  s'appliquent : < 0,40 faible, 0,40–0,74 moyen, ≥ 0,75 élevé.
- `reasons` : tableau de chaînes, affiché tel quel dans l'application.
- `signals` : tableau de mots-clés / features détectées.
- `engine` : nom du moteur, affiché à l'utilisateur pour transparence.

En cas d'erreur réseau, de timeout ou de JSON invalide, le frontend bascule
automatiquement sur l'heuristique locale et affiche `heuristique locale (serveur indisponible)`.

## 2. Signalement — `POST /api/reports`

Requête :

```json
{
  "message_hash": "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
  "score": 0.93,
  "reason": "Promesse de gain inhabituelle",
  "timestamp": 1763920000000
}
```

Réponse : n'importe quel code 2xx (le corps n'est pas lu).

**Le message brut n'est jamais envoyé.** L'empreinte est calculée avec
`SHA-256(message.trim().lowercase())` côté client.

## Exemple minimal Flask

```python
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.post("/api/analyze")
def analyze():
    message = request.get_json(silent=True, {}).get("message", "")
    score = model.predict_proba([message])[0][1]  # à branches avec votre modèle
    return jsonify({
        "score": float(score),
        "label": "spam" if score >= 0.5 else "legit",
        "risk_level": "ELEVE" if score >= 0.75 else ("MOYEN" if score >= 0.4 else "FAIBLE"),
        "reasons": ["Modèle entraîné sur le dataset public de spam SMS"],
        "signals": [],
        "engine": "flask-tflite",
    })

@app.post("/api/reports")
def report():
    data = request.get_json(silent=True) or {}
    # stocker data["message_hash"], data["score"], data["reason"]...
    return jsonify({"ok": True}), 201
```

## Test depuis l'émulateur

Le backend doit écouter sur `0.0.0.0` et l'URL à saisir dans l'application est
`http://10.0.2.2:5000`. Depuis un téléphone physique, utilisez l'IP LAN de l'ordinateur
et le même réseau Wi-Fi. La configuration `network_security_config.xml` autorise le
trafic HTTP clair uniquement vers localhost, 127.0.0.1 et 10.0.2.2 : pour une IP LAN
ou Internet, passez en HTTPS.
