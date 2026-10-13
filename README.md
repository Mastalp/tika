# IFT3913 — Tâche 2 : augmentation d'une suite de tests avec ChatUniTest et PIT

**Université de Montréal — Automne 2026**  
**Binôme :** KHANFOUDI Maysaa (`Maysaa29`) et Laurent-Philippe ROY-LEMAIRE (`Mastalp`)  
**Projet étudié :** Apache Tika, fork pédagogique [`Mastalp/tika`](https://github.com/Mastalp/tika)  
**Module :** `tika-serialization`  
**Classe retenue :** `org.apache.tika.serialization.ComponentConfig`  


## 1. Objectif et démarche

La tâche consiste à augmenter les tests d'une à trois classes Apache Tika déjà testées, mais dont la couverture de code est incomplète, à utiliser **ChatUniTest** avec un modèle ouvert exécuté localement, puis à évaluer la qualité des tests à l'aide de **PIT** (test de mutation). Lorsque des mutants survivent ou ne sont pas atteints, de nouveaux tests sont écrits manuellement. Chaque test doit être justifié par son **intention**, ses **données d'entrée** et son **oracle** (résultat attendu).

Nous avons suivi trois états successifs : **A. tests présents avant l'ajout des nouveaux tests**, **B. tests initialement proposés par ChatUniTest mais corrigés manuellement**, **C. état B complété par six nouveaux tests manuels**. Cette distinction est indispensable pour attribuer correctement les gains mesurés.

## 2. Choix de la classe

### 2.1 Classe retenue : `ComponentConfig`

`ComponentConfig` représente la configuration d'un composant utilisé dans la sérialisation ou le chargement des composants de Tika. Elle offre des accesseurs (`getJsonField`, `getComponentClass`, `getDefault`, `getCustomLoader`), des indicateurs (`hasDefault`, `hasListWrapper`, `hasCustomLoader`, `isLoadAsList`) et un constructeur fluide (`Builder`) permettant de définir des comportements optionnels.

**Raisons du choix :**

- Le module `tika-serialization` figure explicitement parmi les modules admissibles de la tâche 2.
- Des tests JUnit existaient déjà dans le module, mais l'analyse initiale de `ComponentConfig` montrait **25 lignes couvertes sur 36 (69 %)**, donc une couverture inférieure à 100 %.
- La première analyse PIT révélait **21 mutations, aucune tuée**, ce qui permettait de travailler à la fois sur les portions non exercées et sur les assertions insuffisantes.
- La classe est relativement ciblée et permet de comparer des oracles précis sur des valeurs retournées, des booléens, des objets chargés et les effets d'un `Builder`.

### 2.2 Autres pistes explorées

Une exploration préliminaire a aussi porté sur `org.apache.tika.parser.pdf.PDMetadataExtractor`, dans `tika-parser-pdf-module`. Ce module est autorisé pour le devoir, mais la génération de tests et certaines exécutions du module PDF ont rencontré des difficultés de configuration et des échecs ; **aucun résultat PDF n'est amalgamé aux mesures finales de `ComponentConfig`**. La classe effectivement retenue pour l'étude comparative complète et reproduite ci-dessous est `ComponentConfig`.

## 3. Environnement de travail et outils

| Élément | Environnement / rôle |
|---|---|
| Système | Windows 10, PowerShell |
| Éditeur | Visual Studio Code |
| Version Java locale | Eclipse Temurin / OpenJDK 17 |
| Construction | Apache Maven 3.9.16 ; module Tika `4.0.0-SNAPSHOT` |
| Framework de test | JUnit 5, exécuté avec Maven Surefire |
| Génération IA | ChatUniTest Maven Plugin `2.1.1` ; modèle ouvert via Ollama local |
| Configuration IA observée dans `pom.xml` | URL `http://localhost:11434/v1/chat/completions`, modèle configuré `code-llama` ; la version exacte du modèle effectivement chargé au moment des générations doit être confirmée avec le coéquipier |
| Mesure de couverture | JaCoCo `0.8.15` |
| Mutation | PIT `1.30.0`, avec `pitest-junit5-plugin` `1.2.3` |
| Gestion de versions | Git et GitHub ; fork `Mastalp/tika` |

Le module contient la configuration ChatUniTest/PIT dans `tika-serialization/pom.xml`. Le ciblage PIT porte sur `org.apache.tika.serialization.ComponentConfig*` et les tests `org.apache.tika.serialization.*`. Le périmètre des **21 mutations** est resté identique lors des comparaisons.

**Difficultés rencontrées :** le clonage sous Windows a été interrompu par `Filename too long`, résolu en activant les chemins longs et en restaurant le dépôt. Le fichier de tests manuels a déclenché 19 erreurs Checkstyle liées aux fins de ligne `CRLF` et à l'absence d'en-tête de licence Apache. La conversion en `LF` et l'ajout de la licence ont permis de relancer les tests avec succès.


## 4. Génération avec ChatUniTest : acceptations, rejets et corrections

Les fichiers de tests proviennent de la génération ChatUniTest et ont été conservés dans `tika-serialization/chatunitest-tests/` ainsi que, pour leur version exécutable corrigée, dans `tika-serialization/src/test/java/org/apache/tika/serialization/`.

**Bilan des trois cas documentés :**

| Test ciblé | Oracle généré par ChatUniTest | Décision | Correction manuelle et justification |
|---|---|---|---|
| `hasCustomLoader()` | `assertTrue(config.hasCustomLoader())` pour un `Builder` sans chargeur personnalisé | **Rejeté comme incorrect** | Remplacé par `assertFalse(...)` : aucune option `customLoader` n'a été fournie. Test corrigé `HWdefaultConfigurationHasNoCustomLoader`. |
| `hasDefault()` | Attend `true` après `build()` sans fournisseur par défaut, puis `false` sur une configuration d'un autre type | **Rejeté comme incorrect** | Vérification `false` pour la configuration sans fournisseur, puis `true` après `.defaultProvider(() -> "default value")`. Test corrigé `HWhasDefault`. |
| `hasListWrapper()` | Attend `true` après `.loadAsList()` | **Rejeté comme incorrect** | `.loadAsList()` et `.wrapWith(...)` n'ont pas la même signification : le test corrigé vérifie `false` par défaut puis `true` après `.wrapWith(list -> list.toString())`. Test `HWtestHasListWrapper`. |

Les trois propositions initiales sont visibles **commentées** dans leurs fichiers Java. **Elles n'ont pas été acceptées ni exécutées telles quelles.** Les trois méthodes corrigées ont été écrites manuellement à partir des comportements attendus. Elles forment le point de comparaison intermédiaire de la phase B.

**Nombre de corrections observables :** trois tests générés ont exigé au minimum **une correction d'oracle chacun** (trois assertions ou scénarios logiques rectifiés), et ont été réécrits sous forme de trois méthodes `HW...`. Le nombre total de générations tentées, d'essais rejetés en dehors de ces fichiers et le nombre de compilations intermédiaires ne sont pas établis par les fichiers disponibles ; nous ne les inventons pas.

### 4.1 Comparaison qualitative des oracles

Un oracle correct vérifie un comportement réel défini par l'API, et non une valeur arbitraire. Dans les trois propositions rejetées, l'IA a notamment supposé que l'existence d'un `Builder`, le type du composant ou l'activation du chargement sous forme de liste impliquaient automatiquement l'existence d'une option (`default`, `customLoader` ou `listWrapper`). Ces hypothèses sont fausses.

Les oracles corrigés sont **plus spécifiques** : ils distinguent une configuration sans option et une configuration où l'option est explicitement définie. Ainsi, `assertFalse(config.hasDefault())` seul serait insuffisant pour caractériser la méthode ; le test corrigé ajoute un second scénario avec `defaultProvider` et `assertTrue(...)`. Cette approche contrôle les deux états au lieu de simplement vérifier que la méthode peut être appelée.

**Accepté :** le squelette des tests générés et les méthodes intéressantes à tester ont servi de point de départ. **Corrigé :** les oracles et données de configuration. **Rejeté :** les attentes erronées des versions IA non retouchées. Aucun gain de mutation n'est attribué à ces tests originaux pris isolément.

## 5. Protocole de test et commandes de reproduction

À partir d'une copie du fork contenant les changements étudiés, avec Java, Maven et, pour régénérer des tests IA, Ollama/ChatUniTest installés :

```powershell
cd tika-serialization
mvn "-Dtest=ComponentConfigManualTest" test
mvn org.pitest:pitest-maven:1.30.0:mutationCoverage
```

La commande JUnit a produit : **`Tests run: 6, Failures: 0, Errors: 0, Skipped: 0`**, suivie de **`BUILD SUCCESS`**. La dernière analyse PIT s'est également achevée par **`BUILD SUCCESS`**.

Rapports locaux :
- `tika-serialization/target/site/jacoco/index.html` : JaCoCo ;
- `tika-serialization/target/pit-reports/index.html` et `mutations.xml` : PIT (si la configuration locale les place dans ce répertoire).

Pour reproduire une comparaison **A / B / C**, il est nécessaire d'exécuter la même configuration PIT **sur trois états de code distincts** (ou de sélectionner explicitement les jeux de tests correspondants), d'archiver chaque rapport sous un nom distinct et d'éviter d'écraser les résultats précédents. Les rapports A et B ont été fournis par le coéquipier ; la phase C est mesurée localement après les six ajouts manuels. **Ne pas présenter une commande unique comme si elle reconstruisait automatiquement les trois états historiques.**

## 6. Analyse PIT avant et après

| Mesure | A — Tests originaux | B — ChatUniTest corrigé manuellement | C — B + 6 tests manuels |
|---|---:|---:|---:|
| Lignes couvertes | **25/36 (69 %)** | **29/36 (81 %)** | **36/36 (100 %)** |
| Mutations générées | 21 | 21 | 21 |
| Mutations tuées | **0/21 (0 %)** | **9/21 (43 %)** | **19/21 (90 %)** |
| Mutations sans couverture | 14* | 7 | 0 |
| Mutations survivantes | 7* | 5 | 2 |
| Force des tests | 0 % (0/7) | 64 % (9/14) | 90 % (19/21) |

\* Pour l'état A, les nombres de mutations couvertes et non couvertes sont déduits des indicateurs PIT `0/21`, `0/7` et du total de 21 mutants ; l'état détaillé mutation par mutation n'a pas été fourni en XML. Les états B et C ont été vérifiés à partir des rapports et sorties PIT disponibles.

**Interprétation :** la phase B ajoute neuf mutations tuées par rapport à A, mais implique des **corrections humaines** des propositions de ChatUniTest. La phase C ajoute **10 mutants tués** par rapport à B, soit **+47 points de score de mutation**. En tout, la progression atteint **0 % → 43 % → 90 %**. Le nombre de mutants (21) reste constant, ce qui rend les scores directement comparables sur la même classe.

### 6.1 Mutants détectés par les tests générés puis corrigés (phase B)

Selon le rapport détaillé PIT de la phase B, les neuf mutants tués sont :

| Méthode / ligne | Mutation | Test correcteur qui la détecte | Explication de l'échec du mutant |
|---|---|---|---|
| `hasCustomLoader`, 93 | Inversion de condition | `HWdefaultConfigurationHasNoCustomLoader` | L'absence de chargeur doit être rapportée correctement. |
| `hasCustomLoader`, 93 | Retour forcé à `true` | même test | Le résultat attendu est `false` sans chargeur. |
| `hasDefault`, 86 | Inversion de condition | `HWhasDefault` | Le test oppose configuration sans et avec fournisseur par défaut. |
| `hasDefault`, 86 | Retour forcé à `true` | même test | Le scénario sans fournisseur exige `false`. |
| `hasListWrapper`, 78 | Inversion de condition | `HWtestHasListWrapper` | Les deux états sans/avec wrapper sont vérifiés. |
| `hasListWrapper`, 78 | Retour forcé à `true` | même test | Le scénario sans wrapper exige `false`. |
| `Builder.build`, 177 | Retour remplacé par `null` | `HWtestHasListWrapper` | Le test utilise l'objet construit : un `null` provoque l'échec. |
| `Builder.defaultProvider`, 156 | Retour remplacé par `null` | `HWhasDefault` | Le chaînage suivi de `build()` nécessite un `Builder` valide. |
| `Builder.wrapWith`, 146 | Retour remplacé par `null` | `HWtestHasListWrapper` | Le chaînage suivi de `build()` doit rester valide. |

Cette identification repose sur les tests tueurs référencés dans le XML PIT de la phase B ; elle ne prouve **pas** que les versions non corrigées de ChatUniTest auraient tué ces mutants.

## 7. Six tests manuels supplémentaires : intentions, données et oracles

Les six méthodes suivantes figurent dans `tika-serialization/src/test/java/org/apache/tika/serialization/ComponentConfigManualTest.java`. Elles ont été exécutées ensemble avec succès par Maven.

| Nom JUnit | Intention | Données choisies et motivation | Oracle / comportement attendu |
|---|---|---|---|
| `testGetJsonField` | Vérifier la conservation du champ JSON | `"parsers"` : chaîne non vide, distincte du retour mutant `""` | `assertEquals("parsers", config.getJsonField())` ; détecte un retour vide. |
| `testIsLoadAsList` | Vérifier les deux états du chargement en liste | Même type `String`, une configuration normale puis une avec `.loadAsList()` | `assertFalse(...)` puis `assertTrue(...)` ; distingue clairement les deux états. |
| `testGetComponentClass` | Vérifier le type de composant retourné | `String.class` : objet `Class` facile à comparer et non nul | `assertEquals(String.class, config.getComponentClass())` ; détecte un retour `null`. |
| `testGetDefault` | Vérifier la valeur produite par le fournisseur | Fournisseur `() -> "valeur par defaut"` : résultat connu et contrôlé | `assertEquals("valeur par defaut", config.getDefault())` ; vérifie réellement la lecture du défaut. |
| `testGetCustomLoader` | Vérifier la conservation d'un chargeur personnalisé | Chargeur lambda renvoyant une chaîne connue ; objet réutilisé tel quel | `assertTrue(config.hasCustomLoader())` et `assertSame(loader, config.getCustomLoader())` ; contrôle présence et identité. |
| `testWrapList` | Vérifier l'enveloppement d'une liste et l'absence de wrapper | Liste `List.of("A", "B")`, résultat formaté `"A,B"` ; puis configuration sans wrapper | `assertEquals("A,B", ...)` et `assertThrows(IllegalStateException.class, ...)` ; valide les deux branches de comportement. |

**Pourquoi ces données ?** Les entrées sont volontairement simples mais **discriminantes** : une chaîne non vide pour détecter un retour vide, une classe non nulle pour détecter `null`, deux états pour une propriété booléenne, une identité d'objet pour vérifier qu'un même chargeur est restitué, et une liste à deux éléments pour vérifier la transformation et non une simple absence d'exception.

### 7.1 Progression expérimentale pendant l'ajout des tests

| État | Tests manuels ajoutés | Lignes couvertes | Mutations tuées | Score PIT | Mutants `NO_COVERAGE` |
|---|---|---:|---:|---:|---:|
| B | Aucun | 29/36 (81 %) | 9/21 | 43 % | 7 |
| C1 | `getJsonField` | 30/36 (83 %) | 10/21 | 48 % | 6 |
| C2 | + `isLoadAsList` | 31/36 (86 %) | 13/21 | 62 % | 4 |
| C3 | + `getComponentClass` | 31/36 (86 %) | 14/21 | 67 % | 4 |
| C4 | + `getDefault` | 32/36 (89 %) | 15/21 | 71 % | 3 |
| C6 | + `getCustomLoader`, `wrapList` | 36/36 (100 %) | 19/21 | 90 % | 0 |

Les étapes intermédiaires C1 à C4 ont chacune été mesurées après ajout du test concerné ; les deux derniers tests ont été évalués **ensemble**, donc il serait inexact de leur attribuer isolément une fraction précise des quatre mutants tués lors de cette dernière exécution.

### 7.2 Pourquoi les mutants ont été tués

La dernière suite détecte désormais notamment les mutations dans `getJsonField`, `getComponentClass`, les retours booléens de `isLoadAsList`, la lecture du défaut, la récupération du chargeur personnalisé et les deux branches de `wrapList`. Les oracles comparent une valeur réelle à une attente suffisamment précise pour échouer quand PIT modifie le comportement. La phase C a également détecté des modifications dans le chaînage du `Builder` lorsque celles-ci sont effectivement utilisées par les scénarios. Pour connaître **le test tueur exact de chacun des 10 mutants supplémentaires**, consulter le champ `killingTest` dans le `mutations.xml` final ; ne pas déduire cette correspondance du seul ordre d'ajout.

## 8. Mutants encore survivants à la fin

Malgré **100 % de couverture des lignes**, **deux mutants survivent** :

| Méthode / ligne | Modification introduite par PIT | Analyse et amélioration possible |
|---|---|---|
| `ComponentConfig.builder()`, 111 | Remplacement de la valeur de retour par `null` | Les six tests manuels utilisent directement `new ComponentConfig.Builder<>(...)` plutôt que l'accesseur statique `builder()`. Ajouter un test appelant **directement** `ComponentConfig.builder(...)` et vérifiant l'objet retourné pourrait détecter cette mutation. La signature exacte doit être respectée. |
| `ComponentConfig.Builder.register()`, 184 | Suppression de l'appel `ComponentNameResolver.registerComponentConfig(...)` | Vérifier non seulement que `register()` s'exécute, mais aussi que la configuration est retrouvable via `ComponentNameResolver` après enregistrement. Un test peut nécessiter d'isoler ou nettoyer l'état partagé du registre. |

Ces mutants **ne sont pas démontrés équivalents**. Ils constituent des angles morts résiduels. Une couverture des lignes de 100 % ne garantit donc ni un score de mutation de 100 %, ni l'absence de bogues.

## 9. Synthèse et preuves visuelles des résultats

### 9.1 Résultats comparatifs

| Étape | Couverture des lignes | Couverture des mutations | Force des tests |
|---|---:|---:|---:|
| Avant ajout des nouveaux tests | 69 % (25/36) | 0 % (0/21) | 0 % (0/7) |
| Après les tests ChatUniTest **corrigés manuellement** | 81 % (29/36) | 43 % (9/21) | 64 % (9/14) |
| Après les six tests manuels | **100 % (36/36)** | **90 % (19/21)** | **90 % (19/21)** |

### 9.2 Captures des rapports de couverture PIT

**Figure 1 — Rapport initial transmis par le coéquipier.** Il présente 69 % de couverture des lignes et 0 % de couverture des mutations.

![Rapport PIT initial — 69 % de couverture des lignes et 0 % de mutations tuées](preuves-pit/coverage_initial.png)

**Figure 2 — Rapport après correction manuelle des tests proposés par ChatUniTest.** Il présente 81 % de couverture des lignes et 43 % de couverture des mutations.

![Rapport PIT après tests corrigés — 81 % de couverture des lignes et 43 % de mutations tuées](preuves-pit/coverage_apres_chatunitest.png)

**Résultat final (après six tests manuels).** PIT a affiché `Line Coverage: 36/36 (100%)`, `Generated 21 mutations Killed 19 (90%)`, `Mutations with no coverage 0`, `Test strength 90%` et `BUILD SUCCESS`. Il n'y a pas de capture du rapport final du coéquipier : ces chiffres proviennent de l'exécution locale finale.


## 10. Organisation des fichiers et artefacts

```text
tika-serialization/
├── pom.xml                                 # configuration Maven / ChatUniTest / PIT
├── chatunitest-tests/                      # sorties de génération conservées
│   └── org/apache/tika/serialization/
│       ├── ComponentConfig_hasCustomLoader_7_3_Test.java
│       ├── ComponentConfig_hasDefault_6_3_Test.java
│       └── ComponentConfig_hasListWrapper_4_3_Test.java
├── src/main/java/org/apache/tika/serialization/
│   └── ComponentConfig.java
└── src/test/java/org/apache/tika/serialization/
    ├── ComponentConfig_hasCustomLoader_7_3_Test.java
    ├── ComponentConfig_hasDefault_6_3_Test.java
    ├── ComponentConfig_hasListWrapper_4_3_Test.java
    └── ComponentConfigManualTest.java        # 6 nouveaux tests manuels
```

Les rapports PIT avant/après peuvent être joints sous un répertoire de preuves ou reliés depuis ce README, **sans inclure aveuglément les répertoires `target/` dans Git**. Ajouter les liens relatifs effectifs une fois les rapports archivés : **[À COMPLÉTER]**.

## 11. Usage de l'IA et traçabilité

**ChatUniTest** a été employé pour proposer des tests JUnit de `ComponentConfig` au moyen d'un modèle local accessible via Ollama. Ses trois scénarios documentés avaient des attentes incorrectes ; ils ont été **rejetés sous leur forme initiale**, puis remplacés par des méthodes corrigées manuellement. Les six autres tests ont été ajoutés ensuite pour viser des mutants survivants ou non couverts.

**ChatGPT (OpenAI)** a également été utilisé comme aide à l'explication des mutations et structuré la documentation.

## 12. Limites et conclusion

Sur la classe `ComponentConfig` et ses 21 mutations, les tests initiaux ont obtenu **0 %**, les trois tests issus de ChatUniTest puis corrigés manuellement **43 %**, et l'ajout de six tests manuels **90 % (19/21)**. La couverture des lignes est passée de **69 % à 100 %**. Cela montre que la génération IA est utile comme point de départ, mais que la validation des oracles et les tests manuels guidés par PIT sont essentiels. Deux mutations restent survivantes (`builder()` et `register()`) : le score final ne prouve donc pas l'absence de bogues. La validation GitHub Actions reste à documenter.
