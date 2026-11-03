# JaCoCo & PIT — Comprendre et expliquer les outils de mesure

> Document pédagogique : ce que sont ces outils, comment ils fonctionnent,
> ce qu'ils mesurent, et **nos résultats concrets** sur ce projet.

---

## 1. JaCoCo — Mesure de la couverture de code

### 1.1 Qu'est-ce que JaCoCo ?

**JaCoCo** (Java Code Coverage) est un outil de **couverture de code** :
il observe quels sont les fragments de code **réellement exécutés** pendant
les tests, et calcule des pourcentages.

> **Question type enseignant** : *« Que mesure la couverture ? »*
> **Réponse** : la fraction du code **exécutée** par les tests. Un code
> couvert ≠ code correctement testé — c'est une mesure **nécessaire mais
> insuffisante** (d'où l'intérêt de PIT).

### 1.2 Comment ça marche technique ?

```
┌──────────────┐   1. prepare-agent    ┌─────────────────┐
│  mvn test    │ ────────────────────► │ Agent JaCoCo    │
│  (surefire)  │   injecte un agent    │ (instrumentation│
└──────┬───────┘   JVM (-javaagent)    │  bytecode)      │
       │                               └────────┬────────┘
       │ 2. exécution des tests                 │
       │    → l'agent enregistre chaque         │
       │      ligne/branche exécutée            │
       ▼                                        ▼
┌──────────────┐   3. report         ┌─────────────────┐
│ target/      │ ◄────────────────── │ jacoco.exec      │
│  jacoco.exec │                     │ (binaire)        │
└──────────────┘                     └─────────────────┘
       │
       │ 4. rapport HTML/CSV/XML
       ▼
target/site/jacoco/index.html
```

1. **`prepare-agent`** : ajoute `-javaagent:org.jacoco.agent.jar` à la JVM des tests
2. L'**agent** instrumente le bytecode à chaud et enregistre l'exécution
3. À la fin, il écrit le fichier binaire **`jacoco.exec`**
4. Le goal **`report`** transforme ce binaire en HTML/CSV/XML
5. Le goal **`check`** compare aux **seuils** du `pom.xml` et **échoue le build** s'ils ne sont pas atteints

### 1.3 Les 5 métriques JaCoCo

| Métrique | Ce qu'elle compte | Notre valeur |
|----------|-------------------|--------------|
| **Instruction** | Chaque bytecode exécuté ou non | **220/220 = 100 %** |
| **Branche** | Chaque demi-chemin des `if`/`&&`/`||` | **20/20 = 100 %** |
| **Ligne** | Chaque ligne de source exécutée | **48/48 = 100 %** |
| **Méthode** | Chaque méthode appelée au moins une fois | **100 %** |
| **Classe** | Chaque classe visitée | **100 %** |

### 1.4 Pourquoi les BRANCHES sont le plus important ?

```java
if (delta < 0.0) {          // une branche = un demi-chemin
    racines = new double[0]; // "vrai"
} else {
    // ...                  // "faux"
}
```

- **Couverture de lignes** : la ligne `if (delta < 0)` est exécutée ✓
- **Couverture de branches** : il faut avoir pris les **deux côtés** (vrai ET faux)

> Exemple concret de notre projet : la ligne `if (delta < 0.0)` est couverte
> en lignes dès le premier test, mais il faut un test avec `delta ≥ 0` **et**
> un test avec `delta < 0` pour couvrir les **2 branches**.

**C'est pour ça que le seuil du pom.xml exige `BRANCH = 1.00` (100 %).**

### 1.5 Seuils dans le pom.xml

```xml
<jacoco.branch.coverage>1.00</jacoco.branch.coverage>      <!-- 100 % branches -->
<jacoco.instruction.coverage>1.00</jacoco.instruction.coverage>
<jacoco.line.coverage>1.00</jacoco.line.coverage>
```

Exécuté par `mvn verify` → si un pourcentage est en dessous,
**le build échoue** (`Rule violated for bundle...`).

### 1.6 Limite fondamentale de la couverture

> **Un test peut couvrir 100 % du code et ne détecter aucun bug.**

Exemple : un test qui appelle `resoudre(1, 2, 1)` mais **ne vérifie rien**
sur le résultat → 100 % de couverture, 0 % de détection.
→ C'est le **motif d'existence de PIT**.

---

## 2. PIT — Test de mutation (qualité des tests)

### 2.1 Qu'est-ce que PIT ?

**PIT** (Pitest) mesure la **qualité des tests** en cherchant s'ils
détecteraient des **bugs**. Il le fait par **mutation** :

> **Principe** : on **injecte artificiellement des fautes** dans le code
> (les **mutants**), puis on relance les tests. Si un test échoue sur un
> mutant, le mutant est **tué** (= le test aurait détecté ce bug).
> S'il passe, le mutant **survit** (= le test est trop faible).

### 2.2 Le cycle de mutation

```
        Code original
             │
             ▼
    ┌─────────────────┐
    │ Génération de   │  PIT applique des "mutateurs" :
    │ mutants         │  < → <=,  −b → b,  * → /,  return x → return 0...
    └────────┬────────┘
             │  ex. : if (delta < 0)  →  if (delta <= 0)
             ▼
    ┌─────────────────┐
    │ Pour chaque     │  on exécute TOUS les tests
    │ mutant :        │  sur le code muté
    │ run tests       │
    └────────┬────────┘
             │
      ┌──────┴──────┐
      ▼             ▼
  TEST ÉCHEC     TEST OK
  → KILLED       → SURVIVED
  (bon test !)   (test insuffisant)
```

### 2.3 Score de mutation

```
                    mutants tués
Score mutation = ───────────────── × 100
                  mutants générés
```

| Notre projet | Valeur |
|--------------|--------|
| Mutants générés | **48** |
| Tués (KILLED) | **47** |
| Survivants (SURVIVED) | **1** (équivalent justifié) |
| **Score brut** | 47/48 = **97,9 %** |
| **Score hors équivalents** | 47/(48−1) = **100 %** |
| Seuil exigé | **≥ 85 %** ✅ |

### 2.4 Exemples de mutateurs utilisés

| Mutateur | Ce qu'il change | Exemple dans notre code |
|----------|-----------------|-------------------------|
| **ConditionalsBoundary** | `<` ↔ `<=`, `>` ↔ `>=` | `delta < 0` → `delta <= 0` (ligne 94) |
| **NegateConditionals** | inverse un `if` | `if (a==0)` → `if (a!=0)` |
| **MathMutator** | `*` ↔ `/`, `+` ↔ `-` | `2.0 * a` → `2.0 / a` (ligne 100) |
| **InvertNegs** | supprime une négation | `-b` → `b` (ligne 100) |
| **PrimitiveReturns** | remplace un `return` par la valeur par défaut | `return coefficient` → `return 0.0` |
| **RemoveConditional** | supprime une condition | remplace le `if` par son corps |

### 2.5 États possibles d'un mutant

| État | Signification |
|------|---------------|
| **KILLED** | Un test a échoué sur le mutant → **le test détecte le bug** ✅ |
| **SURVIVED** | Tous les tests passent sur le mutant → **faille de la suite** ⚠️ |
| **NO_COVERAGE** | Aucun test n'atteint la ligne mutée | 
| **TIMED_OUT** | Le mutant a bouclé (infinite loop) |
| **NON_VIABLE** | Mutant illisible/incorrect |

### 2.6 Survivants : tuer ou justifier

Pour chaque survivant, **deux options** :

1. **Ajouter un test qui le tue** → le mutant passe à KILLED
2. **Prouver qu'il est équivalent** → il ne change aucun comportement observable,
   on l'exclut du dénominateur du score

#### Notre survivant #6 — équivalent justifié

```java
// Original                    // Mutant
if (b >= 0.0) {                if (b > 0.0) {
```

- Diffère **uniquement** si `b == 0.0`
- Or pour `b = 0`, racines symétriques `±k` → les deux branches donnent `{−k, +k}`
- Après `Math.min/Math.max` (tri), le tableau final est **identique**
- **Conclusion** : non observable → **équivalent justifié** (exclu du score)

### 2.7 Couverture vs Mutation — comparison

| | JaCouCo (couverture) | PIT (mutation) |
|---|---|---|
| **Question** | « Le code a-t-il été **exécuté** ? » | « Les tests **détectent-ils** les fautes ? » |
| **Mesure** | % de code parcouru | % de mutants tués |
| **Force** | Vue structurelle complète | Qualité sémantique des tests |
| **Faiblesse** | Un test vide couvre sans vérifier | Plus lent, génère des faux positifs (équivalents) |
| **Seuil projet** | **100 % branches** | **≥ 85 %** (100 % hors équivalents) |

> **Les deux sont complémentaires** : JaCoCo garantit qu'on a *touché* tout le
> code, PIT garantit qu'on l'a *bien testé*.

---

## 3. Intégration dans le pom.xml (résumé)

```xml
<!-- SEUILS -->
<jacoco.branch.coverage>1.00</jacoco.branch.coverage>
<mutation.score.minimum>0.85</mutation.score.minimum>

<!-- JaCoCo : 3 goals enchaînés -->
prepare-agent  →  report (phase test)  →  check (phase verify)

<!-- PIT : s'exécute à la phase verify -->
mutationCoverage  (avec pitest-junit5-plugin)
```

**Une seule commande pour tout valider :**

```bash
mvn verify
# → tests (457) + JaCoCo 100% + seuils + PIT 97,9%
# → BUILD SUCCESS si tout est atteint, sinon BUILD FAILURE
```

---

## 4. Plan de passage oral (JaCoCo & PIT)

1. **« Qu'est-ce que JaCoCo ? »** → instrumentation bytecode via agent JVM,
   mesure instructions/branches/lignes exécutées
2. **« Pourquoi les branches ? »** → une ligne exécutée ≠ les deux côtés du `if` testés
3. **« Comment on impose le seuil ? »** → properties dans pom + goal `check` qui casse le build
4. **« Et la limite ? »** → couverture ≠ qualité : un test qui ne vérifie rien couvre sans détecter
5. **« Du coup PIT ? »** → mutation : on injecte des fautes, on voit si les tests réagissent
6. **« Votre score ? »** → 47/48 tués = 97,9 %, 1 équivalent justifié (b≥0 vs b>0 avec tri min/max)
7. **« Pourquoi ce mutant est équivalent ? »** → b=0 ⇒ racines symétriques ⇒ tri identique
8. **« Les deux ensemble ? »** → JaCoCo = on a tout touché, PIT = on a bien vérifié
