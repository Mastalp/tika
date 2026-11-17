# IFT3913 — Tâche 3 : KHANFOUDI ROY-LEMAIRE

## Documentation et justification des choix

### Premiers essais avec Semgrep — imports génériques

Nous avons d’abord intégré une règle Semgrep personnalisée pour repérer les imports génériques en Java, comme `import java.util.*;`. Cette règle sert principalement à vérifier la lisibilité et à faciliter la maintenance. Les imports explicites indiquent quels types le fichier utilise. Ce premier essai nous a permis de vérifier que GitHub Actions pouvait exécuter Semgrep et signaler une correspondance.

### Gate test — est-ce que Semgrep bloque le build?

Une fois Semgrep intégré, nous voulions vérifier que le workflow réagissait comme prévu quand une règle trouvait quelque chose. Nous avons ajouté une règle temporaire, `ift3913-gate-test`, et un fichier Java contenant le marqueur `IFT3913_GATE_TEST`.

- **Cas passant.** Le job `semgrep` a réussi, puis le job `build` a démarré. [Exécution GitHub Actions](https://github.com/Mastalp/tika/actions/runs/43905172697)
- **Cas bloquant.** Semgrep a signalé le marqueur et le job a échoué. Ce marqueur servait à valider le workflow. [Exécution GitHub Actions](https://github.com/Mastalp/tika/actions/runs/43908520977/job/133443773243) · [Commit de validation](https://github.com/Mastalp/tika/commit/313b542ab0f2fdd8d9dcb1df5c186380c166e385)

Dans le workflow `IFT3913 tests`, nous avons placé l’analyse avant les tests Maven. Le job `build` déclare `needs: semgrep`, donc les tests de `tika-serialization` et de ses modules requis démarrent si l’analyse réussit. L’option `--error` fait échouer Semgrep si une nouvelle correspondance est signalée. Avec `--baseline-commit`, la comparaison se fait avec l’état de la branche avant le push ou avec le commit de la branche cible fourni par GitHub pour une pull request.

Le workflow utilise `fetch-depth: 0` pour rendre le commit de référence accessible à Semgrep. La version 1.185.0 est épinglée pour utiliser la même version de l’outil localement et dans GitHub Actions. [Workflow `IFT3913 tests`](https://github.com/Mastalp/tika/blob/80793d859a17df3a5ec59072e8fd45961cbf6ea1/.github/workflows/ift3913-tests.yml)

### Analyse de référence avec `p/java`

Nous avons ensuite retiré la règle temporaire et lancé le workflow avec `--config p/java` seulement. Cette exécution a réussi. [Exécution GitHub Actions](https://github.com/Mastalp/tika/actions/runs/43911529330)

Après l’ajout des sept cas, un autre push a pris `80793d8` comme référence. Semgrep n’a trouvé aucun fichier à analyser dans le diff et le job `build` a réussi. Ce cas vérifie que les signalements déjà présents dans la révision de référence ne bloquent pas un push sans nouveau fichier à analyser. [Exécution GitHub Actions](https://github.com/Mastalp/tika/actions/runs/44150398183)

Pour la comparaison avant/après, nous avons aussi exporté une analyse complète sans `--baseline-commit`. Le rapport `semgrep-before.json` contient 11 signalements sur 1 475 fichiers. L’analyse a été effectuée avec Semgrep 1.185.0 et les 60 règles du jeu `p/java` chargé lors de nos essais.

## Recherche et validation des sept cas

### Première tentative — `ObjectInputStream`

Pour la CWE-502, nous avons d’abord ajouté une méthode `fromBytes(byte[])` dans `ParseContextUtils.java`. Elle utilisait `ObjectInputStream.readObject()` pour reconstruire un objet à partir d’un flux. Si ce flux provient d’une source non fiable, la désérialisation peut reconstruire des objets inattendus. Notre essai avec `semgrep scan --config p/java` n’a pourtant donné aucun signalement dans le code essayé.

Nous sommes donc allés voir les règles de Semgrep et avons consulté la liste renvoyée par le registre pour `p/java`. Nous avons constaté que la règle `object-deserialization` ne figurait pas parmi les 60 règles chargées. Cet essai nous a fait comprendre qu’une absence de signalement dépend aussi de la couverture du jeu de règles choisi. Nous avons gardé `p/java` pour la suite et cherché des cas détectables avec les règles disponibles dans notre configuration. [Jeu de règles Java de Semgrep](https://semgrep.dev/p/java)

### Premier cas — utilisation de MD5 (CWE-328)

Nous avons ajouté un appel à `MessageDigest.getInstance("MD5")` dans une méthode ajoutée à `ComponentConfig.java`. Nous avions déjà travaillé sur cette classe et elle nous permettait de faire un essai simple. L’analyse complète a relevé 12 signalements, contre 11 dans le rapport de référence. Le seul nouveau signalement correspondait à la règle `use-of-md5`, à la ligne 190. Le workflow a bloqué l’exécution de Maven, comme prévu. [Exécution GitHub Actions](https://github.com/Mastalp/tika/actions/runs/43915286617)

MD5 présente un risque lorsqu’une fonction de sécurité dépend de la résistance aux collisions. Après cette première détection, nous avons testé six autres catégories de faiblesse.

### Les six autres cas

| CWE     | Fichier et choix du cas                                                                                                       | Ce que Semgrep a relevé                                                                                                                                                                                                                                                                                                                                    |
| ------- | ----------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| CWE-502 | `ParseContextUtils.java:293`. Après le premier essai, nous sommes revenus avec Jackson, déjà utilisé dans le module.          | `jackson-unsafe-deserialization` signale une configuration qui laisse les données JSON influencer le type d’objet reconstruit. Le risque dépend de la provenance du JSON et des types accessibles. [Exécution](https://github.com/Mastalp/tika/actions/runs/43948596049)                                                                                   |
| CWE-89  | `JsonMetadataList.java:151`. La méthode ajoutée illustre une recherche de valeur par nom de métadonnée.                       | `formatted-sql-string` signale la construction d’une requête où une valeur externe peut être interprétée comme du SQL. [Exécution](https://github.com/Mastalp/tika/actions/runs/43952979391)                                                                                                                                                               |
| CWE-611 | `ConfigDeserializer.java:157`. Nous avons pris le cas d’une configuration XML héritée.                                        | `documentbuilderfactory-disallow-doctype-decl-missing` signale l’absence d’interdiction explicite du DOCTYPE dans la fabrique utilisée par la méthode ajoutée. Selon le parseur et sa configuration, un document non fiable pourrait provoquer la résolution de ressources externes. [Exécution](https://github.com/Mastalp/tika/actions/runs/43960137086) |
| CWE-319 | `JsonMetadata.java:134`. L’exemple réutilise la sérialisation JSON de la classe pour envoyer des métadonnées à un collecteur. | `unencrypted-socket` signale la connexion TCP en clair. Si les métadonnées contiennent des informations sensibles, la connexion ne protège pas leur confidentialité. [Exécution](https://github.com/Mastalp/tika/actions/runs/43962793223)                                                                                                                 |
| CWE-326 | `JsonConfigHelper.java:297`. L’exemple représente un contexte de connexion destiné à une configuration distante.              | `weak-ssl-context` signale l’utilisation du nom de protocole `SSL`. L’alerte porte sur la configuration choisie. Les protocoles effectivement négociés dépendent du fournisseur Java et de ses paramètres. [Exécution](https://github.com/Mastalp/tika/actions/runs/44125975523)                                                                           |
| CWE-327 | `GlobalSettings.java:129`. L’exemple illustre le déchiffrement d’une valeur de configuration.                                 | `ecb-cipher` et `use-of-aes-ecb` signalent le même choix de mode. En ECB, des blocs clairs identiques chiffrés avec la même clé donnent des blocs chiffrés identiques, ce qui révèle des répétitions dans les données. [Exécution](https://github.com/Mastalp/tika/actions/runs/44127950619)                                                               |

## Comparaison des rapports avant et après

Les analyses complètes comparent les commits [`0ac2023`](https://github.com/Mastalp/tika/commit/0ac2023036cdef2fbb30f9968fe82fbe4983a898) et [`80793d8`](https://github.com/Mastalp/tika/commit/80793d859a17df3a5ec59072e8fd45961cbf6ea1). Sur chaque révision, nous avons lancé la même commande avec le nom de fichier de sortie correspondant. La commande ci-dessous est celle du rapport après.

```bash
semgrep scan --config p/java --metrics=off --json --output ../semgrep-after.json .
```

En comparant `semgrep-before.json` et `semgrep-after.json` par fichier et identifiant de règle, nous retrouvons les 11 signalements de référence et **8 nouveaux signalements répartis sur 7 catégories CWE**. Le mode ECB explique l’écart puisqu’il déclenche deux règles au même emplacement.

Les exécutions associées aux sept cas montrent que Semgrep a renvoyé le code de sortie `1` et que GitHub Actions a ignoré le job `build`. La comparaison des rapports montre que les sept catégories sont présentes ensemble dans la version analysée après les modifications.

## Validation du Rickroll

Nous avons implémenté une action GitHub Actions réutilisable dans `.github/actions/rickroll/action.yml`.

Cette action est déclenchée lorsque le job Semgrep ou le job de tests échoue. Elle affiche un message humoristique dans les logs et ajoute un GIF ainsi qu'un lien vers la vidéo *Never Gonna Give You Up* dans le résumé GitHub Actions.

Le workflow utilise `if: failure()` pour déclencher le job `rickroll`.

**Validation :** le commit `ce9a082f3` a été utilisé pour tester le mécanisme. L'exécution GitHub Actions montre que le Rickroll s'est déclenché après un échec de Semgrep.

[Preuve du Rickroll dans GitHub Actions](https://github.com/Mastalp/tika/actions/runs/44151849871)

Ce test valide le déclenchement après un échec Semgrep. Le déclenchement après un échec Maven n'a pas été vérifié séparément.
