# Tableau des survivants PIT — Analyse complète

## Contexte

| Métrique | Valeur |
|----------|--------|
| Mutations générées | **48** |
| Mutations tuées | **47** |
| Survivants | **1** |
| **Score de mutation** | **97,9 %** (hors équivalent : **100 %**) |
| Seuil exigé | ≥ 85 % |
| Couverture lignes (PIT) | 48/48 = **100 %** |
| Tests exécutés | 547 |

> Score hors équivalents = 47 / (48 − 1) = **100 %** ≥ 85 % ✅

---

## Liste des 6 survivants de la 1re passe + analyse

| # | Ligne | Mutateur | Description | Décision | Test ajouté / Justification |
|---|-------|----------|-------------|----------|------------------------------|
| 1 | 94 | ConditionalsBoundary | `delta < 0` → `delta <= 0` | **TUÉ** | `MutationsKillTest.deltaZeroUneSeuleRacine` : delta=0 exige exactement 1 racine (le mutant en rend 0) |
| 2 | 100 | MathMutator | `2.0 * a` → `2.0 / a` | **TUÉ** | `MutationsKillTest.racineDoubleExacteA4` : a=4, racine exacte −1 (le mutant donne −16) |
| 3 | 100 | MathMutator | `/` → `*` sur `−b/(2a)` | **TUÉ** | Idem — le mutant donne −64 au lieu de −1 |
| 4 | 100 | InvertNegs | `−b` → `b` (negation retirée) | **TUÉ** | `MutationsKillTest.racineDoubleBNegatif` : x²−6x+9 → +3 exact (le mutant donne −3) |
| 5 | 51 | PrimitiveReturns | `return coefficient` → `return 0.0` | **TUÉ** | `MutationsKillTest.coefficientNonNul` : `new CoefficientNulException(42.0)` exige 42.0 |
| 6 | 111 | ConditionalsBoundary | `b >= 0` → `b > 0` | **ÉQUIVALENT — justifié** | Voir ci-dessous |

---

## Justification écrite du survivant #6 (équivalent)

### Mutant

```java
// Original (ligne 111)              // Mutant
if (b >= 0.0) {                      if (b > 0.0) {
    q = -0.5 * (b + Math.sqrt(delta));   // identique
} else {                             } else {
    q = -0.5 * (b - Math.sqrt(delta));   // identique
}
```

### Différence d'observable

Le mutant ne diverge que lorsque **`b == 0.0` exactement** (la frontière `>=` vs `>`).

### Pourquoi c'est équivalent

Pour `b = 0`, l'équation devient `a·x² + c = 0`, dont les racines sont **toujours symétriques** :

```
x₁ = +√(−c/a)     et     x₂ = −√(−c/a)      (somme = −b/a = 0)
```

- **Branche originale** (`b >= 0` vrai) : `q = −½·√δ` → `{x₁, x₂} = {−k, +k}`
- **Branche mutant** (`b > 0` faux → else) : `q = +½·√δ` → `{x₁, x₂} = {+k, −k}`

Or la ligne 128 applique un **tri croissant** :

```java
racines = new double[]{ Math.min(x1, x2), Math.max(x1, x2) };
```

Après tri, les deux branches produisent **le tableau identique** `[-k, +k]`.

### Vérification empirique

Les tests `MutationsKillTest.SurvivantBoundaryB` exécutent le cas `b = 0` et passent
**avec le code original ET avec le mutant** — preuve opérationnelle que le comportement
observable est le même.

### Conclusion

> **Mutant équivalent** : non observable par aucun test, car le tri `min/max` masque
> la différence de signe de `q` lorsque `b = 0` (racines symétriques).
> Classé **équivalent justifié** — exclu du dénominateur du score.

---

## Score final

| Formule | Calcul | Résultat |
|---------|--------|----------|
| Score brut | 47 / 48 | **97,9 %** |
| Score hors équivalents | 47 / (48 − 1) | **100 %** |
| Seuil pom.xml | `mutation.score.minimum` | **85 %** |
| Conformité | 100 % ≥ 85 % | ✅ |

---

## Rapports

- **JaCoCo HTML** : `reports/jacoco/index.html`
- **PIT HTML** : `reports/pit/index.html`
- **PIT CSV** : `reports/pit/mutations.csv`
- **PIT XML** : `reports/pit/mutations.xml`
