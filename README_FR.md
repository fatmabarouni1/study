# StudyFlow Simple — 7 activités

Application pédagogique Android en Java et XML. Cette version ajoute les deux pages proposées,
avec deux échanges supplémentaires utilisant startActivityForResult().

## Ouvrir et lancer

1. Extraire ce ZIP dans un nouveau dossier.
2. Android Studio > File > Open > choisir StudyFlow_Simple, qui contient settings.gradle.
3. Attendre Sync. Utiliser SDK 35 et JDK 17 ou un JDK compatible avec AGP 8.7.3.
4. Sélectionner la configuration Android App « app », un émulateur puis Run.
5. Si app manque : Edit Configurations > + > Android App > module app > Launch Default Activity.

Connexion de démonstration : demo@studyflow.test / 123456.
Ces identifiants publics servent à l'exercice, sans inscription, serveur ni authentification de production.

## Les sept activités

| Java | XML | Fonction |
| --- | --- | --- |
| LoginActivity | activity_login.xml | Connexion de démonstration |
| MainActivity | activity_main.xml | Bienvenue et navigation |
| TodoActivity | activity_todo.xml | Liste des tâches sauvegardées |
| AddTaskActivity | activity_add_task.xml | Nouveau : saisir et renvoyer une tâche |
| FocusActivity | activity_focus.xml | Minuteur |
| DurationActivity | activity_duration.xml | Nouveau : choisir et renvoyer une durée |
| SummaryActivity | activity_summary.xml | Bilan des séances et des tâches |

Les classes Java sont dans app/src/main/java/tn/fatma/studyflowsimple/.
Les interfaces sont dans app/src/main/res/layout/.
Tous les fichiers XML sont indentés, avec un attribut par ligne.

## Les trois échanges avec résultat

| Activity appelante | Page ouverte | requestCode | Donnée retournée |
| --- | --- | --- | --- |
| MainActivity | FocusActivity | FOCUS_REQUEST = 1 | completedMinutes |
| TodoActivity | AddTaskActivity | ADD_TASK_REQUEST = 2 | taskTitle |
| FocusActivity | DurationActivity | DURATION_REQUEST = 3 | selectedMinutes |

Chaque Activity appelante utilise startActivityForResult() et reçoit la réponse dans onActivityResult().
Chaque page appelée prépare ses données avec setResult(RESULT_OK, intent), puis appelle finish().
Annuler ou Retour avant validation renvoie RESULT_CANCELED et ne modifie pas les données de l'appelant.
Ce mécanisme est asynchrone : il ne bloque pas le thread de l'interface.
L'ancienne API est utilisée ici pour répondre à la consigne du TP.

## Utilisation

- Accueil > Ma to-do list > Ajouter : ouvre la nouvelle page Ajouter une tâche.
- Écrire un titre unique, puis Valider : le titre est renvoyé à TodoActivity, qui l'ajoute et le sauvegarde.
- Toucher une tâche pour cocher/décocher ; appui long pour supprimer après confirmation.
- Accueil > Focus Time > Choisir une durée : ouvre une page avec 15, 25 et 50 minutes.
- Valider : la durée et le temps affiché changent ; la séance ne démarre qu'avec Démarrer.
- La saisie manuelle de 1 à 180 minutes est aussi disponible pour des essais courts.
- Pause et Reprendre conservent le temps restant. Réinitialiser remet le minuteur à 25 minutes.
- La durée ne peut pas être changée pendant une séance déjà commencée : réinitialiser d'abord.
- Une rotation conserve les champs et le minuteur. Un retour arrière ferme la séance.
- À la fin du minuteur, les statistiques sont sauvegardées puis la durée est renvoyée à MainActivity.
- MainActivity affiche le résultat, puis ouvre Mon bilan. Retour depuis bilan permet de voir le message reçu sur accueil.
- Le bilan reste accessible directement depuis accueil.

La version conserve les tâches et les statistiques sur le téléphone avec SharedPreferences.
Le minuteur utilise CountDownTimer et le cycle de vie de FocusActivity. Aucun service n'est encore ajouté à cette étape.
Les sept activités sont dans une seule application et une seule variante debug.

## Compiler

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug
```

## Essais manuels à faire sur émulateur

1. Ouvrir Ajouter une tâche, écrire « Réviser Android », valider : vérifier l'ajout dans la liste.
2. Essayer un titre vide et un doublon : rester sur le formulaire avec une erreur.
3. Saisir une tâche puis Annuler : vérifier que la liste reste identique.
4. Tourner la page Ajouter une tâche : vérifier que le texte saisi est conservé.
5. Choisir 15, puis 25, puis 50 minutes : vérifier chaque fois le champ et le temps affiché dans Focus.
6. Ouvrir le choix de durée puis Annuler : vérifier que l'ancienne durée reste identique.
7. Tourner le choix de durée après avoir sélectionné 50 minutes : vérifier que le choix est conservé.
8. Choisir une durée, démarrer, mettre en pause : vérifier que le choix de durée est désactivé jusqu'à Réinitialiser.
9. Saisir manuellement 1 minute, terminer la séance : vérifier le résultat reçu et le bilan augmenté d'une séance et d'une minute.
10. Quitter une séance avant la fin : vérifier le message d'annulation et les statistiques inchangées.
11. Fermer puis rouvrir l'application : vérifier les tâches et leurs états cochés.

L'explication du code est dans EXPLICATION_CODE_FR.md. Les contrôles effectués sont dans VALIDATION.txt.
Un APK de démonstration signé est inclus dans APK. Pour modifier le code, utiliser Run depuis Android Studio.
