# Règles ProGuard pour Anti-Arnaque.
# Les modèles de données (org.json) et les vues sont conservés tels quels.
-keep class com.antiarnaque.app.model.** { *; }
-keepattributes Signature
