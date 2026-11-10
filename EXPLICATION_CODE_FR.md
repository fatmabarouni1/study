# Comprendre StudyFlow Simple — sept activités

Chaque écran possède une classe Java pour son comportement et un fichier XML pour son interface.
Les deux nouvelles pages sont AddTaskActivity et DurationActivity.

## 1. La structure commune

```java
public class AddTaskActivity extends Activity
```

La classe hérite du composant Android Activity et peut représenter un écran.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_task);
}
```

Android appelle onCreate quand il crée l'écran. super laisse Android initialiser la classe de base.
setContentView affiche le fichier XML indiqué.
Le Bundle contient éventuellement l'état sauvegardé lors d'une recréation.

```java
taskInput = findViewById(R.id.taskInput);
```

findViewById retrouve un composant du XML. R est une classe générée pour accéder aux ressources.

```java
findViewById(R.id.validateTaskButton).setOnClickListener(v -> returnTask());
```

Un clic appelle returnTask(). `v ->` est une écriture courte d'un écouteur de clic.

## 2. LoginActivity.java : connexion de démonstration

onCreate ouvre SharedPreferences et vérifie la valeur connected.
Une session déjà ouverte permet d'ouvrir directement MainActivity.
Sinon, on affiche les champs et on configure le bouton Se connecter.

login() récupère les deux saisies, compare les identifiants de démonstration avec equals(),
affiche une erreur s'ils sont incorrects, ou sauvegarde la session.
Elle ouvre ensuite MainActivity avec un Intent et transmet l'e-mail avec putExtra("email", email).
finish() ferme la page de connexion.
Les identifiants sont publics et affichés sur l'écran : il s'agit d'une démonstration locale.

## 3. MainActivity.java : accueil

onCreate vérifie la connexion, affiche activity_main.xml et récupère l'e-mail de l'Intent,
ou celui sauvegardé dans SharedPreferences si aucun e-mail n'a été transmis.
Les boutons ouvrent la liste, Focus ou le bilan.
La déconnexion modifie connected puis ouvre LoginActivity et ferme l'accueil.

Focus est ouvert avec un résultat attendu :

```java
private static final int FOCUS_REQUEST = 1;
startActivityForResult(new Intent(this, FocusActivity.class), FOCUS_REQUEST);
```

onActivityResult vérifie le numéro de la demande, RESULT_OK et la présence de l'Intent data.
La méthode lit completedMinutes, affiche le résultat sur accueil puis ouvre SummaryActivity.
En cas d'annulation, un message indique que la séance n'a pas été terminée.
onSaveInstanceState conserve le message lors d'une rotation.

## 4. TodoActivity.java : liste et sauvegarde

- tasks est une ArrayList de titres, dans leur ordre d'ajout.
- completed est un Set de titres cochés. Un Set ne contient pas de doublons.
- adapter est un ArrayAdapter qui relie les titres à une ListView.
- preferences conserve les données localement.

onCreate recharge les tâches et configure la liste. Le modèle Android
simple_list_item_multiple_choice fournit une ligne avec case à cocher.
Toucher une ligne modifie son état. Un appui long ouvre la confirmation de suppression.

Le bouton Ajouter ouvre maintenant une vraie page :

```java
Intent intent = new Intent(this, AddTaskActivity.class);
intent.putStringArrayListExtra("existingTasks", new ArrayList<>(tasks));
startActivityForResult(intent, ADD_TASK_REQUEST);
```

existingTasks permet à la page de vérifier qu'un titre n'existe pas déjà.
Le code de demande ADD_TASK_REQUEST vaut 2.

onActivityResult récupère taskTitle si le résultat est RESULT_OK. La tâche est ajoutée,
sauvegardée puis affichée. RESULT_CANCELED laisse la liste inchangée.

saveTasks() utilise TextUtils.join("\n", tasks) pour rassembler les titres, un titre par ligne.
putString sauvegarde cette chaîne et putStringSet sauvegarde l'ensemble des titres cochés.
À la lecture, split("\n") reconstruit la liste des titres.

showTasks() appelle notifyDataSetChanged(), remet les cases cochées selon completed et actualise le compteur.
Les titres sont uniques pour permettre cette représentation simple.

## 5. AddTaskActivity.java : nouvelle page avec résultat

onCreate affiche le formulaire et définit RESULT_CANCELED par défaut.
Valider appelle returnTask(). Annuler appelle finish() sans changer ce résultat.
Le bouton Retour Android annule également la saisie.

returnTask() :

1. Lire le titre avec getText().toString().trim().
2. Remplacer un éventuel retour à la ligne par un espace.
3. Refuser un titre vide.
4. Récupérer existingTasks et refuser un doublon.
5. Préparer un Intent de résultat, mettre le titre dans taskTitle et fermer la page.

```java
Intent result = new Intent();
result.putExtra("taskTitle", title);
setResult(RESULT_OK, result);
finish();
```

Cette page ne sauvegarde pas la tâche. Elle renvoie seulement la valeur ; TodoActivity la sauvegarde.

## 6. FocusActivity.java : minuteur et deux échanges

Focus reçoit d'abord un résultat de DurationActivity, puis renvoie son propre résultat à MainActivity.
Ces deux échanges sont indépendants et possèdent leurs callbacks dans des classes différentes.

### Les variables

| Variable | Rôle |
| --- | --- |
| remaining | Temps restant en millisecondes |
| endTime | Instant prévu de fin, depuis le démarrage du téléphone |
| running | Le temps est en train de défiler |
| started | Une séance est commencée et non terminée, même en pause |
| sessionMinutes | Durée initiale de la séance |
| sessionId | Identifiant utilisé pour éviter de compter deux fois la même séance |

### Le choix de durée

Le bouton Choisir une durée envoie la durée affichée avec selectedMinutes,
puis ouvre DurationActivity avec startActivityForResult et DURATION_REQUEST = 3.

onActivityResult vérifie le code 3 et RESULT_OK, récupère selectedMinutes puis actualise le champ et remaining.
L'annulation ne change rien. Choisir une durée ne démarre pas le minuteur.
Le bouton est désactivé dès qu'une séance est commencée, y compris pendant la pause.

### Les méthodes du minuteur

- startTimer() vérifie une durée de 1 à 180 minutes, prépare la séance ou la reprend.
- runCountdown() crée CountDownTimer(remaining, 1000). onTick actualise chaque seconde ; onFinish termine la séance.
- pauseTimer() conserve le temps restant et annule les actualisations.
- resetTimer() annule le compteur et revient à 25 minutes.
- updateScreen() affiche le temps et active/désactive les commandes.
- completeSession() sauvegarde une séance terminée une seule fois, puis renvoie completedMinutes avec RESULT_OK à MainActivity et appelle finish().

```java
remaining = Math.max(0, endTime - SystemClock.elapsedRealtime());
```

La différence donne le temps restant et Math.max empêche une valeur négative.
Une minute vaut 60 000 millisecondes ; le L de 60000L indique un long.

```java
String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)
```

/ 60 donne les minutes, % 60 donne les secondes restantes et %02d affiche au moins deux chiffres.

onSaveInstanceState sauvegarde l'état dans un Bundle pour une recréation.
onStop annule les actualisations lorsque l'écran n'est plus visible ; onStart les reprend à partir de l'échéance.
onDestroy nettoie le compteur. Retour abandonne une séance non terminée.
Le minuteur n'est pas un service à cette étape du projet.

## 7. DurationActivity.java : nouvelle page de choix

onCreate affiche les trois RadioButton et récupère la durée transmise par Focus.
RadioGroup permet de cocher une seule option parmi 15, 25 et 50 minutes.
Si la durée manuelle reçue n'est pas une de ces options, 25 est proposé.
L'utilisateur peut annuler pour conserver sa durée manuelle.

Lors d'une rotation, Android restaure le choix du RadioGroup ; l'initialisation n'est donc faite
que si savedInstanceState est null.

returnDuration() lit le bouton sélectionné, transforme ce choix en nombre de minutes,
prépare l'Intent de résultat puis ferme la page :

```java
result.putExtra("selectedMinutes", minutes);
setResult(RESULT_OK, result);
finish();
```

La clé selectedMinutes est la même dans cette page et dans onActivityResult de FocusActivity.

## 8. SummaryActivity.java : bilan

onCreate lit le nombre de séances, les minutes cumulées et la dernière durée dans SharedPreferences.
Elle lit aussi le nombre actuel de titres cochés et affiche ces informations dans quatre TextView.
Le bouton Retour appelle finish(). Les séances abandonnées ne sont pas ajoutées aux totaux.

## 9. Le principe de startActivityForResult

| Étape | Instruction | Rôle |
| --- | --- | --- |
| Ouvrir | startActivityForResult(intent, code) | Ouvrir une page et identifier la demande |
| Préparer le retour | setResult(RESULT_OK, result) | Mettre le statut et les données du retour |
| Fermer | finish() | Fermer la page appelée |
| Recevoir | onActivityResult(requestCode, resultCode, data) | Lire et traiter la réponse |

requestCode indique quelle demande revient. resultCode indique validation ou annulation.
data contient les données ajoutées avec putExtra. Il faut vérifier data != null avant de les lire.
L'attente du résultat est asynchrone : le thread de l'interface n'est pas bloqué.
Cette API historique est utilisée pour la consigne du TP.

## 10. Les fichiers XML et Gradle

Chaque Activity possède son XML dans res/layout. Les noms et identifiants relient le Java aux vues.
LinearLayout aligne les composants ; ScrollView permet le défilement ; EditText permet la saisie ;
Button réagit aux clics ; TextView affiche du texte ; ListView affiche les tâches ; RadioGroup gère les choix exclusifs.

strings.xml contient les textes, colors.xml les couleurs et styles.xml les propriétés communes.
Les drawables XML définissent des fonds arrondis. Tous sont indentés avec quatre espaces et un attribut par ligne.
AndroidManifest.xml déclare les sept activités et désigne LoginActivity comme lanceur avec MAIN et LAUNCHER.
app/build.gradle définit SDK, identifiant d'application et niveau Java. Les composants natifs suffisent : pas de dépendance externe d'interface.

## Présentation orale

« Mon application possède sept activités. Elle regroupe une connexion de démonstration, un accueil,
une to-do list, un minuteur et un bilan. Deux pages supplémentaires servent à ajouter une tâche et choisir une durée.
Elles renvoient les valeurs à leurs écrans appelants avec setResult et finish.
Les écrans appelants utilisent startActivityForResult et onActivityResult.
SharedPreferences conserve les tâches et statistiques ; CountDownTimer gère le minuteur. »
