# IFT3913 — Tâche 2 : KHANFOUDI ROY-LEMAIRE

## 1. Choix et justification de la classe (10 %)

### 1.1 Exploration d'Apache Tika

Nous avons d'abord récupéré et compilé Apache Tika, exploré l'organisation de ses modules et exécuté les tests préexistants du module `tika-parser-pdf-module`. Notre premier choix s'est porté sur `PDMetadataExtractor`, qui participe à l'extraction des métadonnées de documents PDF. Cette classe nous semblait offrir des comportements intéressants à tester.

Après plusieurs tentatives infructueuses de génération de tests sur `PDMetadataExtractor` (section 2.2), nous avons examiné le module `tika-serialization`. Un essai de validation sur `PrettyMetadataKeyComparator` y a confirmé que ChatUniTest pouvait générer des fichiers de tests. Nous avons finalement sélectionné `ComponentConfig`.

### 1.2 Rôle de `ComponentConfig`

Apache Tika utilise différents composants, notamment des parseurs (`Parser`) pour extraire le contenu des documents et des détecteurs (`Detector`) pour reconnaître leurs formats. Leur chargement peut être décrit par une configuration JSON.

La classe générique [`ComponentConfig<T>`](https://github.com/Mastalp/tika/blob/ift3913-tests/tika-serialization/src/main/java/org/apache/tika/serialization/ComponentConfig.java) **décrit les règles de chargement d'une catégorie de composants**. Elle ne charge pas elle-même les composants et n'effectue pas l'extraction des documents. C'est notamment `TikaLoader` qui consulte ces règles afin de choisir entre un chargeur spécialisé et le mécanisme générique de chargement.

La classe contient six attributs principaux :

| Attribut          | Rôle                                                                                         |
| ----------------- | -------------------------------------------------------------------------------------------- |
| `jsonField`       | Nom du champ JSON, par exemple `"parsers"`.                                                  |
| `componentClass`  | Type Java attendu, par exemple `Parser.class`.                                               |
| `loadAsList`      | Indique si la configuration demande un chargement sous forme de liste.                       |
| `listWrapper`     | Fonction permettant de transformer une liste en un objet du type attendu, souvent composite. |
| `defaultProvider` | Fonction fournissant une valeur ou un composant par défaut.                                  |
| `customLoader`    | Chargeur spécialisé facultatif.                                                              |

`ComponentConfig` emploie le patron de conception **Builder** pour définir progressivement ces propriétés. Par exemple, Tika peut enregistrer la configuration suivante :

```java
ComponentConfig.builder("parsers", Parser.class)
        .customLoader(new ParserLoader())
        .register();
```

Cette configuration associe le champ JSON `"parsers"` au type `Parser`, indique qu'il faut utiliser `ParserLoader` et s'enregistre dans `ComponentNameResolver`. **Elle ne charge pas immédiatement les parseurs.**

### 1.3 Pertinence du choix au regard des tests existants

Les méthodes `hasCustomLoader()`, `hasDefault()` et `hasListWrapper()` sont des comportements simples à isoler : elles indiquent si les fonctions correspondantes ont été configurées. Les tester revient à vérifier la cohérence des règles de chargement, et non la capacité de Tika à extraire un fichier PDF.

Notre première analyse avec PIT, réalisée avant l'ajout des tests, a renforcé ce choix : **69 % de couverture des lignes (25/36), mais aucun mutant tué parmi les 21 générés**. Sept mutants avaient survécu et quatorze n'étaient pas couverts. La classe offrait donc une occasion pertinente d'améliorer la détection des modifications de comportement (voir section 5.1).

## 2. Installation et intégration de ChatUniTest dans Maven (10 %)

### 2.1 Installation et configuration

Nous avons installé ChatUniTest afin de générer des tests unitaires à l'aide d'un modèle de langage. Notre première tentative avec l'API d'OpenAI a échoué à cause d'un problème de validation de la clé API. Nous avons alors utilisé **Ollama**, qui permet d'exécuter un modèle localement.

Le plugin est déclaré dans le fichier [`tika-serialization/pom.xml`](https://github.com/Mastalp/tika/blob/ift3913-tests/tika-serialization/pom.xml) :

```xml
<plugin>
    <groupId>io.github.zju-aces-ise</groupId>
    <artifactId>chatunitest-maven-plugin</artifactId>
    <version>2.1.1</version>
    <configuration>
        <apiKeys>ollama</apiKeys>
        <model>code-llama</model>
        <url>http://localhost:11434/v1/chat/completions</url>
    </configuration>
</plugin>
```

Depuis la racine du dépôt, nous avons utilisé la commande suivante sous Windows (PowerShell) pour cibler `ComponentConfig` :

```powershell
.\mvnw.cmd -f .\tika-serialization\pom.xml chatunitest:class "-DselectClass=org.apache.tika.serialization.ComponentConfig"
```

Ce processus nécessite que le serveur local Ollama soit disponible à l'adresse configurée. Le dépôt contient aussi les dépendances de test nécessaires, dont JUnit 5 et Mockito.

### 2.2 Première tentative sur `PDMetadataExtractor`

L'exécution de ChatUniTest sur `PDMetadataExtractor` se terminait par `BUILD SUCCESS` dans Maven, mais aucun test utilisable n'était conservé. En examinant les fichiers temporaires produits par l'outil, nous avons découvert que plusieurs tests candidats échouaient à la compilation.

Les messages d'erreur se trouvaient notamment sous Windows dans :

```text
C:\tmp\chatunitest-info\tika-parsers-standard-modules\tika-parsers-standard\tika-parsers\tika-parent\tika-parser-pdf-module\error-message
```

L'une des erreurs était :

```text
Error in PDMetadataExtractor_extract_1_0_Test:
line 28 : package org.mockito does not exist
```

Nous avons ajouté une dépendance vers Mockito au `pom.xml` du module concerné. Malgré cette modification, les exécutions suivantes n'ont toujours pas produit de tests utilisables pour cette classe.

Cette expérience montre qu'un **`BUILD SUCCESS` du plugin n'atteste pas nécessairement de la qualité ni même de la conservation de tests générés** : les candidats peuvent avoir été rejetés pendant la génération.

### 2.3 Test de validation avec `PrettyMetadataKeyComparator`

Pour vérifier que les difficultés ne provenaient pas d'une incapacité générale de notre installation, nous avons exécuté ChatUniTest sur `PrettyMetadataKeyComparator` du module `tika-serialization`. Cette fois, des fichiers de tests ont été générés.

Ce résultat confirmait que l'outil pouvait fonctionner dans notre environnement. Il ne permettait toutefois pas d'identifier précisément la cause des échecs avec `PDMetadataExtractor`. Nous avons poursuivi avec `ComponentConfig`.

## 3. Génération des tests pour `ComponentConfig` (10 %)

La génération de tests pour `ComponentConfig` a pris **environ 18 minutes**. ChatUniTest a produit plusieurs candidats, mais de nombreux essais comportaient des erreurs de compilation ou des références incorrectes à l'API.

Trois fichiers de tests ont été retenus. Ils se trouvent dans `tika-serialization/src/test/java/org/apache/tika/serialization/` :

| Fichier                                                                                                                                                                                                            | Méthode ciblée      |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------- |
| [`ComponentConfig_hasCustomLoader_7_3_Test.java`](https://github.com/Mastalp/tika/blob/ift3913-tests/tika-serialization/src/test/java/org/apache/tika/serialization/ComponentConfig_hasCustomLoader_7_3_Test.java) | `hasCustomLoader()` |
| [`ComponentConfig_hasDefault_6_3_Test.java`](https://github.com/Mastalp/tika/blob/ift3913-tests/tika-serialization/src/test/java/org/apache/tika/serialization/ComponentConfig_hasDefault_6_3_Test.java)           | `hasDefault()`      |
| [`ComponentConfig_hasListWrapper_4_3_Test.java`](https://github.com/Mastalp/tika/blob/ift3913-tests/tika-serialization/src/test/java/org/apache/tika/serialization/ComponentConfig_hasListWrapper_4_3_Test.java)   | `hasListWrapper()`  |

Les trois tests initiaux **compilaient, mais échouaient à l'exécution** en raison d'assertions incorrectes. Dans les fichiers conservés, les versions initiales sont commentées et les versions corrigées manuellement portent le préfixe `HW` (_hand-written_).

Il est donc important de distinguer les **candidats générés par ChatUniTest** des **tests effectivement exécutés après notre intervention**. PIT2 évalue ces derniers ; ses résultats ne peuvent pas être attribués au modèle sans tenir compte des corrections humaines.

## 4. Documentation et critique des tests générés (20 %)

### 4.1 `hasCustomLoader()` : attente erronée pour une configuration vide

La méthode testée retourne `true` uniquement si un chargeur spécialisé a été fourni :

```java
public boolean hasCustomLoader() {
    return customLoader != null;
}
```

**Assertion générée par ChatUniTest :**

```java
ComponentConfig config = new ComponentConfig.Builder<>("test", String.class).build();
assertTrue(config.hasCustomLoader()); // Incorrect
```

Le Builder n'a jamais reçu d'appel à `.customLoader(...)`. Par conséquent, `customLoader` vaut `null` et la valeur attendue est `false`.

**Correction exécutée — `HWdefaultConfigurationHasNoCustomLoader()` :**

```java
ComponentConfig<?> config =
        new ComponentConfig.Builder<>("test", String.class).build();
assertFalse(config.hasCustomLoader());
```

**Critique :** ChatUniTest a bien ciblé une méthode testable, mais a formulé une hypothèse incompatible avec l'état de l'objet construit. Le test corrigé vérifie le cas négatif. Il ne vérifie pas le cas positif, dans lequel un `customLoader` aurait été explicitement défini.

### 4.2 `hasDefault()` : confusion entre type Java et valeur par défaut

La méthode vérifie seulement si `defaultProvider` est défini :

```java
public boolean hasDefault() {
    return defaultProvider != null;
}
```

**Extrait du test généré :**

```java
ComponentConfig config = new ComponentConfig.Builder<>("jsonField", String.class).build();
assertTrue(config.hasDefault()); // Incorrect : aucun defaultProvider

config = new ComponentConfig.Builder<>("jsonField", Integer.class).build();
assertFalse(config.hasDefault());
```

Le type `String.class` ou `Integer.class` ne détermine pas la présence d'une valeur par défaut. C'est l'appel à `.defaultProvider(...)` qui renseigne cette propriété.

**Correction exécutée — `HWhasDefault()` :**

```java
ComponentConfig config = new ComponentConfig.Builder<>("jsonField", String.class).build();
assertFalse(config.hasDefault());

config = new ComponentConfig.Builder<>("jsonField", String.class)
        .defaultProvider(() -> "default value")
        .build();
assertTrue(config.hasDefault());
```

**Critique :** le test généré attribuait à tort le comportement de `hasDefault()` au type du composant. Le test corrigé compare les deux états pertinents : avec et sans fournisseur. En revanche, il ne teste pas la valeur effectivement renvoyée par `getDefault()`.

### 4.3 `hasListWrapper()` : confusion entre chargement en liste et regroupement

La méthode vérifie la présence d'une fonction `listWrapper` :

```java
public boolean hasListWrapper() {
    return listWrapper != null;
}
```

**Extrait du test généré :**

```java
ComponentConfig config = new ComponentConfig.Builder<>("jsonField", String.class).build();
assertFalse(config.hasListWrapper());

config = new ComponentConfig.Builder<>("jsonField", String.class)
        .loadAsList().build();
assertTrue(config.hasListWrapper()); // Incorrect
```

Or, `.loadAsList()` modifie le booléen `loadAsList` ; elle ne configure pas `listWrapper`. Pour cela, il faut utiliser `.wrapWith(...)`.

**Correction exécutée — `HWtestHasListWrapper()` :**

```java
ComponentConfig config = new ComponentConfig.Builder<>("jsonField", String.class).build();
assertFalse(config.hasListWrapper());

config = new ComponentConfig.Builder<>("jsonField", String.class)
        .wrapWith(list -> list.toString())
        .build();
assertTrue(config.hasListWrapper());
```

**Critique :** ChatUniTest a confondu deux propriétés voisines, mais indépendantes, du Builder. Le test corrigé vérifie l'existence de la fonction de regroupement, sans vérifier que son exécution par `wrapList()` produit le bon résultat.

### 4.4 Bilan critique de ChatUniTest

Aucun des trois tests initiaux ne passait sans intervention manuelle. Les erreurs observées ne concernaient pas seulement la compilation : elles traduisaient surtout une **mauvaise compréhension de l'état des objets et du rôle des méthodes**.

Les trois versions corrigées sont utiles, car elles distinguent les configurations où une propriété est absente de celles où elle est présente. Cependant, elles testent principalement des méthodes booléennes simples. Elles ne couvrent pas complètement les accesseurs, la transformation d'une liste, le chargement par défaut ni les effets de l'enregistrement auprès de `ComponentNameResolver`.

L'expérience illustre ainsi à la fois le potentiel de la génération automatique et la nécessité de relire les tests, de comprendre le code de production et de corriger les assertions avant d'en tirer des conclusions sur la qualité de la suite de tests.

## 5. Évaluation par analyse de mutation avec PIT (15 %)

Nous avons utilisé PIT (_PITest_), version **1.30.0**, avec le plugin JUnit 5. Dans le [`pom.xml` du module](https://github.com/Mastalp/tika/blob/ift3913-tests/tika-serialization/pom.xml), le motif `org.apache.tika.serialization.ComponentConfig*` cible à la fois `ComponentConfig` et sa classe interne `Builder`.

PIT modifie temporairement le comportement du code de production, puis exécute les tests :

- **`KILLED` :** la mutation entraîne l'échec d'au moins un test.
- **`SURVIVED` :** la mutation a été exécutée, mais aucun test ne l'a détectée.
- **`NO_COVERAGE` :** aucun test sélectionné n'a exécuté cette mutation.

### 5.1 PIT1 — Situation initiale (_baseline_)

La première analyse a été effectuée **sans les trois nouveaux tests** afin d'évaluer la capacité des tests préexistants à détecter les modifications de `ComponentConfig`.

| Métrique              |             PIT1 |
| --------------------- | ---------------: |
| Couverture des lignes | **69 % (25/36)** |
| Mutants générés       |               21 |
| Mutants tués          |            **0** |
| Mutants survivants    |                7 |
| Mutants non couverts  |               14 |
| Score de mutation     |          **0 %** |

Bien que les tests préexistants exécutent une partie du code, ils ne détectent aucune des mutations générées dans cette configuration PIT. Ce résultat souligne qu'**une couverture de lignes non nulle ne garantit pas la détection d'un comportement incorrect**.

### 5.2 PIT2 — Après ajout des tests corrigés

Nous avons relancé PIT sur la même classe cible, cette fois avec les trois tests issus de ChatUniTest et corrigés manuellement.

| Métrique                                |         PIT1 |             PIT2 |
| --------------------------------------- | -----------: | ---------------: |
| Couverture des lignes                   | 69 % (25/36) | **81 % (29/36)** |
| Mutants générés                         |           21 |               21 |
| Mutants tués                            |            0 |            **9** |
| Mutants survivants                      |            7 |                5 |
| Mutants non couverts                    |           14 |                7 |
| Score de mutation (_mutation coverage_) |          0 % |  **43 % (9/21)** |
| Force des tests (_test strength_)       |    0 % (0/7) |  **64 % (9/14)** |

La couverture des lignes augmente de **12 points de pourcentage** et le score de mutation de **43 points**. Neuf mutations sont désormais détectées. L'amélioration ne se limite donc pas à l'exécution de lignes supplémentaires : elle traduit également une meilleure capacité de certains tests à signaler un comportement modifié.

Toutefois, cinq mutations survivent encore, et sept ne sont pas couvertes. Le _test strength_ de 64 % est calculé sur les **14 mutants couverts** ; le score de mutation de 43 % est calculé sur l'ensemble des 21 mutants.

### 5.3 Commande et comparaison expérimentale

Depuis la racine du dépôt, la commande utilisée sous Windows pour exécuter PIT est :

```powershell
.\mvnw.cmd -pl :tika-serialization pitest:mutationCoverage
```

La commande est identique pour PIT1 et PIT2. **La variable qui change est la présence des trois tests ajoutés dans la suite exécutée** : absents lors de PIT1, présents lors de PIT2. Le rapport HTML permet ensuite de comparer la couverture, les mutants tués et ceux qui restent non détectés.

## 6. Documentation des mutants détectés et non détectés (15 %)

### 6.1 Mutants tués par les tests corrigés

**`hasCustomLoader()` — ligne 93 : deux mutations `KILLED`.** PIT a notamment remplacé la valeur retournée par `true` et inversé la condition. Dans `HWdefaultConfigurationHasNoCustomLoader()`, aucun chargeur n'est défini ; `assertFalse(config.hasCustomLoader())` attend donc `false`. Une mutation qui fait retourner `true` provoque l'échec du test.

**`hasDefault()` — ligne 86 : deux mutations `KILLED`.** Le test `HWhasDefault()` vérifie une configuration sans fournisseur (`false`) et une autre avec `defaultProvider` (`true`). Les mutations qui forcent la valeur de retour à `true` ou inversent la condition contredisent au moins l'une de ces assertions.

**`hasListWrapper()` — ligne 78 : deux mutations `KILLED`.** Le test `HWtestHasListWrapper()` compare une configuration sans `.wrapWith(...)` (`false`) et une configuration avec `.wrapWith(...)` (`true`). Les mutations qui imposent `true` ou inversent la condition sont détectées par les assertions correspondantes.

Trois autres mutations ont été tuées dans les méthodes du Builder :

| Ligne | Méthode mutée               | Test ayant tué le mutant | Pourquoi le test échoue                                                                                              |
| ----: | --------------------------- | ------------------------ | -------------------------------------------------------------------------------------------------------------------- |
|   146 | `Builder.wrapWith()`        | `HWtestHasListWrapper()` | PIT remplace le retour du Builder par `null`. L'appel chaîné à `.build()` ne peut plus s'exécuter.                   |
|   156 | `Builder.defaultProvider()` | `HWhasDefault()`         | Le Builder renvoie `null` et l'appel suivant à `.build()` échoue.                                                    |
|   177 | `Builder.build()`           | `HWhasDefault()`         | La méthode retourne `null` au lieu de construire `ComponentConfig`, ce qui empêche ensuite d'appeler `hasDefault()`. |

Ces trois derniers cas sont à distinguer des six mutations booléennes : ils sont détectés parce qu'ils **rompent la construction des objets utilisée par les tests**, et non parce qu'une assertion compare directement la valeur de retour de la méthode mutée. Ainsi, les neuf mutants `KILLED` de PIT2 sont expliqués.

### 6.2 Exemples de mutants non détectés

**`getComponentClass()` — ligne 62 : `SURVIVED`.** PIT a remplacé la classe Java retournée par `null`. La mutation a été exécutée, mais aucun test ne vérifiait suffisamment la valeur obtenue pour la détecter. Une assertion sur `getComponentClass()` pourrait combler cette lacune.

**`wrapList()` — ligne 74 : `NO_COVERAGE`.** PIT a remplacé le résultat de la méthode par `null`, mais aucun test sélectionné n'a exécuté ce chemin. Un test pourrait fournir une liste connue et vérifier la valeur retournée par `wrapList()` après configuration de `.wrapWith(...)`.

**`Builder.register()` — ligne 184 : `SURVIVED`.** PIT a supprimé l'appel à `ComponentNameResolver.registerComponentConfig(...)` sans provoquer d'échec. Les tests présents ne détectent donc pas la disparition de cet effet d'enregistrement.

### 6.3 Bilan des mutants survivants et non couverts

Les **cinq mutants survivants** dans PIT2 sont :

| Ligne | Méthode                  | Mutation                                              |
| ----: | ------------------------ | ----------------------------------------------------- |
|    62 | `getComponentClass()`    | Remplacement du résultat par `null`.                  |
|   111 | `builder()`              | Remplacement du résultat par `null`.                  |
|   135 | `Builder.loadAsList()`   | Remplacement du résultat par `null`.                  |
|   170 | `Builder.customLoader()` | Remplacement du résultat par `null`.                  |
|   184 | `Builder.register()`     | Suppression de l'appel à `registerComponentConfig()`. |

Les **sept mutants non couverts** concernent `getJsonField()` (un), `isLoadAsList()` (deux), `wrapList()` (deux), `getDefault()` (un) et `getCustomLoader()` (un).

On observe deux faiblesses distinctes : des méthodes **exécutées sans assertions assez discriminantes** (`SURVIVED`) et des méthodes **non exercées par les tests** (`NO_COVERAGE`). Les rapports de mutation nous aident ainsi à identifier précisément les futurs cas à tester.

## 7. Exécution des nouveaux tests dans GitHub Actions (10 %)

Nous avons activé les workflows GitHub Actions sur la branche `main` du dépôt. L'exécution du workflow **`main jdk17 windows build`**, le 12 octobre 2026, s'est terminée avec le statut **`success`** :

- [Workflow Windows / JDK 17 — exécution réussie](https://github.com/Mastalp/tika/actions/runs/38306825530)
- [Job `build (17)` — journal d'exécution](https://github.com/Mastalp/tika/actions/runs/38306825530/job/115002786478)

Le journal Maven de cette exécution indique explicitement :

```text
Running org.apache.tika.serialization.ComponentConfig_hasCustomLoader_7_3_Test
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

Running org.apache.tika.serialization.ComponentConfig_hasDefault_6_3_Test
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

Running org.apache.tika.serialization.ComponentConfig_hasListWrapper_4_3_Test
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

**Les trois nouveaux tests exécutables ont donc bien été lancés et ont réussi sur GitHub Actions dans cet environnement.** Il faut néanmoins distinguer cette réussite du statut de tous les workflows du dépôt : certains autres jobs, notamment des builds Linux/JDK, ont échoué lors de la même série d'exécutions. Nous ne les présentons donc pas comme entièrement réussis.

## 8. Bilan et conclusion

Cette tâche nous a permis de mettre en pratique la génération automatique de tests unitaires et de mesurer leur efficacité par analyse de mutation. La recherche d'une classe appropriée, les problèmes rencontrés avec `PDMetadataExtractor` et l'utilisation d'Ollama ont montré que la production de tests exploitables dépend autant de l'environnement de génération que de la structure du code testé.

Sur `ComponentConfig`, ChatUniTest a produit trois candidats compilables, mais aucun ne passait initialement. Nous avons dû analyser les méthodes de production et corriger des hypothèses erronées sur `customLoader`, `defaultProvider` et `listWrapper`. Cette intervention humaine a été essentielle pour obtenir des assertions cohérentes avec le comportement attendu.

La comparaison entre PIT1 et PIT2 montre ensuite une amélioration mesurable : la couverture des lignes est passée de **69 % à 81 %** et le score de mutation de **0 % à 43 %**, avec **neuf mutants tués sur 21**. Elle illustre l'intérêt des tests ajoutés, mais aussi leurs limites : cinq mutants survivent encore et sept ne sont pas couverts.

Enfin, les trois tests corrigés ont été exécutés avec succès dans un workflow GitHub Actions Windows/JDK 17. Il reste à compléter le critère des tests supplémentaires écrits indépendamment de ChatUniTest et à finaliser la traçabilité des rapports PIT ainsi que la validation des autres workflows.

Nous retenons surtout qu'**un test qui compile ou qui passe n'est pas nécessairement un bon test**. La lecture critique des assertions et l'analyse de mutation sont nécessaires pour évaluer si une suite de tests détecte réellement des changements de comportement.

---
