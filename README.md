# RAPPORT TACHE 2 ROY-LEMAIRE KHANFOUDI

## installation de tika

exploration du repo, run les tests preexistants sur le module pdf parser, et choix d'une classe interessante : data extractor

```
PDMetadataExtractor
```

## installation de chatunitest

essai avec chatgpt mais mon api key n'est pas acceptee, installation de ollama.

### premiere tentative de run chatunitest

build successful mais rien de produit.
generation d'une suite de tests candidate mais echoue la compilation

trouve des tmp files

powershell '''
C:\tmp\chatunitest-info\tika-parsers-standard-modules\tika-parsers-standard\tika-parsers\tika-parent\tika-parser-pdf-module\error-message
'''

introduit des dependances vers Mockito

```
Error in PDMetadataExtractor_extract_1_0_Test: line 28 : package org.mockito does not exist
```

inclusion de mockito dans le pom.xml

chatunitest echoue encore et ne produit aucun tests.

### sanity test

sanity test avec une autre classe : serialization PrettyMetadataKeyComparator

succes de la generation des tests!

### choix d'une autre classe plus interessante

nous choisissons ComponentConfig

run chatunitest, prend 18 minutes, enormement dhallucination et de candidats qui ne compilent pas.

quelques tests sont produits.

on run les tests que nous avons, les 3 tests sont rouges. nous sommes sur la bonne voie

### les tests sont rouges

Les tests échouent en raison d'assomptions erronées sur le comportement des methodes hasCustomLoader(), hasDefault() et hasListWrapper().

Après avoir regardé le comportement des tests, nous les avons modifié pour les faire passer.
Donc aucun des tests générés par chatunitest pour la classe ComponentConfig n'étaient valides avant une intervention de notre part.

PIT necessite une suite de tests verte, donc c'était nécéssaire.
