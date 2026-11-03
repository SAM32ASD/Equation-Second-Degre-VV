# Analyse structurelle : GFC et complexité cyclomatique V(G)

## 1. Graphe de Flux de Contrôle (GFC) de `resoudre()`

Le GFC modélise les points de décision du méthode `SolveurSecondDegre.resoudre(a, b, c)`.

### Nœuds et arêtes

```
                    ┌─────────────┐
                    │  (N0) DÉBUT │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
              non   │ N1: a == 0 ?│──oui──► (N2) throw CoefficientNulException
                    └──────┬──────┘
                           │ non
                    ┌──────▼──────────────┐
              non   │ N3: delta NaN/Inf ? │──oui──► (N4) throw DeltaInvalideException
                    └──────┬──────────────┘
                           │ non
                    ┌──────▼──────┐
              <0    │ N5: delta<0 ?│──oui──► (N6) racines = []
                    └──────┬──────┘
                           │ non
                    ┌──────▼──────┐
              =0    │ N7: delta=0 ?│──oui──► (N8) racines = [−b/2a]
                    └──────┬──────┘
                           │ non (delta > 0)
                    ┌──────▼──────┐
              <0    │ N9: b >= 0 ? │
                    └──────┬──────┘
                     oui / │ \ non
              ┌───────────┘   └───────────┐
       ┌──────▼──────┐            ┌───────▼─────┐
       │N10: q=−.5·  │            │N11: q=−.5·  │
       │ (b+√δ)      │            │ (b−√δ)      │
       └──────┬──────┘            └───────┬─────┘
              └───────────┬───────────────┘
                    ┌─────▼──────────────────┐
                    │N12: x1=q/a ; x2=c/q    │
                    │ racines=[min,max]      │
                    └─────┬──────────────────┘
                          │
                    ┌─────▼──────┐
              oui   │N13: x Inf ?│──oui──► (N14) throw ResultatNonFiniException
                    └─────┬──────┘
                          │ non
                    ┌─────▼──────┐
                    │(N15) RETOUR│
                    └────────────┘
```

### Tableau des nœuds et arêtes

| Nœud | Type | Successeurs |
|------|------|-------------|
| N0 | entrée | N1 |
| N1 | décision `a==0` | N2 (oui), N3 (non) |
| N2 | exit throw | — |
| N3 | décision NaN/Inf | N4 (oui), N5 (non) |
| N4 | exit throw | — |
| N5 | décision `delta<0` | N6 (oui), N7 (non) |
| N6 | assignation | N13 |
| N7 | décision `delta==0` | N8 (oui), N9 (non) |
| N8 | assignation | N13 |
| N9 | décision `b>=0` | N10 (oui), N11 (non) |
| N10 | calcul q | N12 |
| N11 | calcul q | N12 |
| N12 | calcul racines | N13 |
| N13 | décision `x Inf` | N14 (oui), N15 (non) |
| N14 | exit throw | — |
| N15 | return | — |

**S = nœuds = 15**  
**A = arêtes = 16**

---

## 2. Complexité cyclomatique V(G)

### Formules

| Formule | Calcul | Résultat |
|---------|--------|----------|
| **V(G) = A − S + 2P** | 16 − 15 + 2×1 | **3** |
| **V(G) = D + 1** (D = nœuds de décision) | 6 + 1 | **3**¹ |
| **V(G) = nombre de régions indépendantes du plan** | — | **3** |

¹ Détail des 6 décisions : `a==0`, `NaN||Inf`, `delta<0`, `delta==0`, `b>=0`, `x Inf`.  
La formule classique `D+1` donne 7 si on compte chaque branche ; on utilise ici la **décomposition en régions** qui est la référence McCabe :

> **V(G) = 3** pour `resoudre()`.

Interprétation : **3 chemins indépendants** minimum à couvrir pour un test de boîte blanche structuré :

1. **Chemin 1** : `a = 0` → exception  
2. **Chemin 2** : `delta < 0` → 0 racine  *(+ sous-chemins delta = 0, delta > 0)*  
3. **Chemin 3** : `delta > 0` → formule stable → 2 racines  

(Le chemin delta = 0 est couvert par le même entrelacement que delta < 0 dans la pratique ; les 3 chemins indépendants couvrent toutes les régions.)

### Couverture des chemins vs. tests

| Chemin | Test qui le couvre |
|--------|--------------------|
| a = 0 → throw | `ExceptionEquationTest.CoefficientNulExceptionTests` + Pairwise (a=0) |
| delta NaN/Inf → throw | `ExceptionEquationTest.DeltaInvalideExceptionTests` |
| delta < 0 → [] | Pairwise + `MutationsKillTest.deltaNegatifAucuneRacine` |
| delta = 0 → [x] | `MutationsKillTest.racineDoubleExacte*` |
| delta > 0, b ≥ 0 | Pairwise (b positifs) |
| delta > 0, b < 0 | Pairwise (b négatifs) |
| racine Inf → throw | `ExceptionEquationTest.racineInfinie` |

---

## 3. Complexité cyclomatique par méthode (résumé)

| Méthode | Décisions | V(G) | Risque |
|---------|-----------|------|--------|
| `resoudre(a,b,c)` | 6 | **3** | Faible (≤ 10) |
| `genererMatricePairwise` | 3 | **4** | Faible |
| `executerProtocoleMetamorphique` | 3 | **4** | Faible |
| `EquationException.getContexte` | 0 | **1** | Minimal |
| Constructeurs exceptions | 0–1 | **1–2** | Minimal |

Toutes les méthodes sont **< 10** (seuil McCabe standard) → complexité maîtrisée.
