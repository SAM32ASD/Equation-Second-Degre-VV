# Projet V&V — Solveur 2nd Degré (Pairwise + Métamorphique + JaCoCo + PIT)

Projet complet de **Vérification & Validation** : solveur d'équation du second degré
testé par réduction **Pairwise**, tests **métamorphiques**, couverture **JaCoCo 100 %**
et mutation **PIT ≥ 85 %**.

---

## Résultats finaux

| Métrique | Seuil | Atteint | Statut |
|----------|-------|---------|--------|
| Tests unitaires | — | **457 / 457** | ✅ |
| JaCoCo — branches | 100 % | **100 %** (20/20) | ✅ |
| JaCoCo — instructions | 100 % | **100 %** (220/220) | ✅ |
| JaCoCo — lignes | 100 % | **100 %** (48/48) | ✅ |
| PIT — score brut | ≥ 85 % | **97,9 %** (47/48) | ✅ |
| PIT — hors équivalents | ≥ 85 % | **100 %** (47/47) | ✅ |
| Complexité V(G) `resoudre` | ≤ 10 | **3** | ✅ |

---

## Prérequis

- **Java 17+**
- **Maven 3.9+**
- **VS Code** + Extension Pack for Java (optionnel)

## Exécution

```bash
# Tests + JaCoCo (rapport : target/site/jacoco/)
mvn test

# Tests + JaCoCo + seuils + PIT (rapports : target/site/jacoco/ + target/pit-reports/)
mvn verify

# PIT seul
mvn org.pitest:pitest-maven:mutationCoverage
```

---

## Architecture

```
src/
├── main/java/vv/seconddegre/
│   ├── SolveurSecondDegre.java            ← SUT (code testé)
│   └── exceptions/
│       ├── EquationException.java         ← base (contexte typé)
│       ├── CoefficientNulException.java   ← a = 0
│       ├── DeltaInvalideException.java    ← delta = NaN/Inf
│       └── ResultatNonFiniException.java  ← racine = Inf
└── test/java/vv/seconddegre/
    ├── SolveurNeufParametresTest.java     ← Pairwise + métamorphique (MR1/MR2)
    ├── ExceptionEquationTest.java         ← couverture exceptions / cas limites
    ├── MutationsKillTest.java             ← tests ciblés anti-mutation
    └── QuadTestCase.java                  ← conteneur de cas de test

docs/
├── jacoco-pit-explication.md              ← COMPRENDRE JaCoCo & PIT (pour l'oral)
├── GFC-complexite.md                      ← graphe de flux + V(G)
└── survivants-pit.md                      ← tableau des 6 survivants analysés

reports/
├── jacoco/                                ← rapport HTML JaCoCo
└── pit/                                   ← rapport HTML/CSV/XML PIT
```

---

## Stratégie de test

### 1. Pairwise / All-Pairs
- Domaine naïf 9×9×9 = **729** combinaisons → réduit à **~200 cas**
- Couvre toutes les paires (a,b), (a,c), (b,c)

### 2. Tests métamorphiques (pas d'oracle exact)
| MR | Propriété |
|----|-----------|
| **MR1** | `P(x) = a·x² + b·x + c ≈ 0` pour chaque racine |
| **MR2** | Relations de Viète : `x₁+x₂ = −b/a`, `x₁·x₂ = c/a` |

### 3. Tests d'exception
- `a = 0` → `CoefficientNulException`
- `delta` NaN/Inf → `DeltaInvalideException`
- racine Inf → `ResultatNonFiniException`

### 4. Tests anti-mutation (`MutationsKillTest`)
Tuent les 5 mutants tueables de la 1re passe PIT ; le 6e est un équivalent justifié.

---

## Seuils dans le pom.xml

```xml
<jacoco.branch.coverage>1.00</jacoco.branch.coverage>       <!-- 100 % branches -->
<jacoco.instruction.coverage>1.00</jacoco.instruction.coverage>
<jacoco.line.coverage>1.00</jacoco.line.coverage>
<mutation.score.minimum>0.85</mutation.score.minimum>       <!-- ≥ 85 % mutation -->
```

`mvn verify` **échoue** si un seuil n'est pas atteint.

---

## Livrable Git

```bash
git init
git add .
git commit -m "Projet V&V : solveur 2nd degré — JaCoCo 100%, PIT 98%, GFC/V(G)"
```

Contenu du dépôt : code, tests, rapports JaCoCo/PIT (`reports/`), documentation
(`docs/`), `pom.xml` avec seuils.

---

## Plan de passage oral

1. **SUT** : équation du second degré, formule stable anti-catastrophe
2. **Pairwise** : 729 → ~200 cas, couverture des paires
3. **Métamorphique** : MR1 (réinjection) + MR2 (Viète), tolérances relatives
4. **Exceptions** : hiérarchie personnalisée, contexte typé
5. **JaCoCo** : 100 % branches — *voir `docs/jacoco-pit-explication.md` §1*
6. **PIT** : 48 mutations, 47 tuées, 1 équivalent justifié — *idem §2*
7. **Seuils** : vérification automatique dans `mvn verify`

> **Doc dédiée pour montrer la compréhension** :
> [`docs/jacoco-pit-explication.md`](docs/jacoco-pit-explication.md)
> — ce qu'est JaCoCo (agent, 5 métriques, branches), ce qu'est PIT
> (mutation, KILLED/SURVIVED, score, équivalents), et comment ils se complètent.
