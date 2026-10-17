# Spawn Coin — NeoForge 1.21.1

Projet source pour VS Code. Statut : compilation et essais en jeu à effectuer.

## Compiler sous Windows
Installer un **JDK 21**, puis ouvrir ce dossier dans VS Code et accepter les extensions Java recommandées.
Faire **Ctrl+Maj+B** ou lancer **BUILD.bat**.
Alternative PowerShell : `.\gradlew.bat build`.
Internet requis pour les dépendances du premier build. Le wrapper Gradle est inclus.
Résultat : `build/libs/spawncoin-1.0.0.jar`.
Test client : `.\gradlew.bat runClient`.

## Installation
Même JAR sur le serveur et les clients. Minecraft 1.21.1, NeoForge 21.1.172 ou version 21.1 ultérieure compatible. Java 21.
Un opérateur utilise `/spawncoin` ou `/give @s spawncoin:coin`.
Prendre la pièce en main, clic droit dans le vide : spawn du monde (Overworld), puis retour exact (dimension et orientation comprises).
Aucune recette, aucune consommation, délai de 1 seconde. La commande exige les droits opérateur ; la pièce est utilisable par tous.
Retour propre au joueur, partagé entre ses pièces, enregistré dans ses données et recopié après mort.
Descendre des montures avant utilisation. Les compagnons ne sont pas téléportés.
Une destination bloquée ou hors limites est refusée et le retour conservé.
**Aménager un spawn sûr avec /setworldspawn**, sur sol solide sans lave ni vide. Le contrôle anticollision ne garantit pas l'absence de tous les dangers.
Clic droit sur coffre/porte : interaction du bloc prioritaire, viser dans le vide.

## Vérifications avant publication
Compiler ; essayer sur une copie du monde et un serveur de test : aller-retour Overworld/Nether/End, deux joueurs simultanés, reconnexion, redémarrage, mort et destination bloquée.
Aucun test en jeu n'a été réalisé dans l'environnement de préparation.

## CurseForge
Créer un projet Minecraft Mod et téléverser le JAR de build/libs ; sélectionner Minecraft 1.21.1 et NeoForge. Attendre l'approbation.
Le propriétaire du pack ajoute le mod approuvé et publie une version du pack ; installer aussi le JAR sur le serveur.
La publication n'est pas effectuée par cette archive.
