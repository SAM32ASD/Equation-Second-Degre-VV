package vv.seconddegre;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vv.seconddegre.exceptions.CoefficientNulException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =====================================================================
 *  TESTS ANTI-MUTATION  —  MutationsKillTest
 * =====================================================================
 *
 * <h2>Objectif</h2>
 * Tuer les mutants survivants relevés par PIT (score cible ≥ 85%).
 * Chaque test vise un mutant précis, identifié par sa ligne et sa
 * description dans le rapport {@code target/pit-reports}.
 *
 * <h2>Survivants traités</h2>
 * <table border="1">
 *   <tr><th>#</th><th>Ligne</th><th>Mutant</th><th>Test</th></tr>
 *   <tr><td>1</td><td>94</td><td>boundary &lt; → &lt;=</td>
 *       <td>delta=0 → exactement 1 racine</td></tr>
 *   <tr><td>2</td><td>100</td><td>× → ÷ (2.0*a)</td>
 *       <td>delta=0 avec a≠1, racine exacte</td></tr>
 *   <tr><td>3</td><td>100</td><td>÷ → × </td>
 *       <td>idem</td></tr>
 *   <tr><td>4</td><td>100</td><td>removed negation (−b)</td>
 *       <td>idem</td></tr>
 *   <tr><td>5</td><td>getCoefficient</td><td>return field → return 0</td>
 *       <td>constructeur avec valeur ≠ 0</td></tr>
 *   <tr><td>6</td><td>111</td><td>boundary &gt;= → &gt;</td>
 *       <td><b>ÉQUIVALENT</b> — justifié (voir plus bas)</td></tr>
 * </table>
 *
 * <h2>Justification du survivant #6 (équivalent)</h2>
 * Ligne 111 : {@code if (b >= 0.0)} → {@code if (b > 0.0)}.
 * La différence ne se produit que pour {@code b == 0.0} exactement.
 * Or pour b = 0, l'équation {@code ax² + c = 0} a des racines
 * <b>symétriques</b> {@code ±√(−c/a)}. Les deux branches calculent
 * q de signe opposé, donc {x1, x2} et {−x1, −x2} — après le tri
 * {@code Math.min/Math.max} le tableau final est <b>identique</b>.
 * Mutant donc non observable : classé <b>équivalent justifié</b>.
 */
class MutationsKillTest {

    private final SolveurSecondDegre solveur = new SolveurSecondDegre();

    // ────────────────────────────────────────────────────────────────
    //  #1 — Ligne 94 : delta < 0.0  →  delta <= 0.0  (boundary)
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Survivant #1 — boundary delta < 0 (ligne 94)")
    class SurvivantDeltaBoundary {

        @Test
        @DisplayName("delta = 0 doit renvoyer EXACTEMENT 1 racine (pas 0)")
        void deltaZeroUneSeuleRacine() {
            // a=1, b=2, c=1 → delta = 4 − 4 = 0
            // Mutant delta<=0 renverrait un tableau VIDE → longueur 0 ≠ 1
            double[] racines = solveur.resoudre(1.0, 2.0, 1.0);
            assertEquals(1, racines.length,
                "delta = 0 doit produire exactement une racine double");
            assertEquals(-1.0, racines[0], 1e-12,
                "racine double attendue : −b/2a = −1");
        }

        @Test
        @DisplayName("delta < 0 doit renvoyer 0 racine (contrôle négatif)")
        void deltaNegatifAucuneRacine() {
            double[] racines = solveur.resoudre(1.0, 0.0, 1.0); // delta = −4
            assertEquals(0, racines.length);
        }

        @Test
        @DisplayName("delta > 0 doit renvoyer 2 racines (contrôle positif)")
        void deltaPositifDeuxRacines() {
            double[] racines = solveur.resoudre(1.0, 0.0, -1.0); // delta = 4
            assertEquals(2, racines.length);
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  #2 #3 #4 — Ligne 100 : −b / (2.0 * a)  (3 mutants math/neg)
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Survivants #2-3-4 — formule racine double (ligne 100)")
    class SurvivantRacineDouble {

        /**
         * delta = 0 avec a = 4 (≠ 1) pour distinguer × de ÷ :
         *   correct   : −b / (2·a)     = −8 / 8 = −1
         *   mul→div   : −b / (2 / a)   = −8 / 0.5 = −16
         *   div→mul   : −b · (2 · a)   = −8 · 8 = −64
         *   no-neg    :  b / (2 · a)   =  8 / 8 =  1
         * Toute déviation casse l'égalité exacte ci-dessous.
         */
        @Test
        @DisplayName("delta = 0, a = 4 : racine exacte −1 tue les 3 mutants")
        void racineDoubleExacteA4() {
            // 4x² + 8x + 4 = 0  →  (2x+2)² = 0  →  x = −1
            double[] racines = solveur.resoudre(4.0, 8.0, 4.0);
            assertEquals(1, racines.length);
            assertEquals(-1.0, racines[0], 0.0,
                "valeur EXACTE requise pour tuer les mutants ligne 100");
        }

        @Test
        @DisplayName("delta = 0, a = 2 : racine exacte −1")
        void racineDoubleExacteA2() {
            // 2x² + 4x + 2 = 0  →  x = −4/4 = −1
            double[] racines = solveur.resoudre(2.0, 4.0, 2.0);
            assertEquals(1, racines.length);
            assertEquals(-1.0, racines[0], 0.0);
        }

        @Test
        @DisplayName("delta = 0, b négatif : signe correct de la racine")
        void racineDoubleBNegatif() {
            // x² − 6x + 9 = 0  →  x = 3  (si negation retirée : x = −3)
            double[] racines = solveur.resoudre(1.0, -6.0, 9.0);
            assertEquals(1, racines.length);
            assertEquals(3.0, racines[0], 0.0,
                "removed-negation produirait −3 au lieu de +3");
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  #5 — getCoefficient : return field → return 0.0
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Survivant #5 — getCoefficient() (PrimitiveReturns)")
    class SurvivantGetCoefficient {

        @Test
        @DisplayName("Constructeur avec valeur ≠ 0 : accesseur doit la renvoyer")
        void coefficientNonNul() {
            // Le mutant remplace 'return coefficient' par 'return 0.0'
            // (valeur par défaut double). Avec coefficient = 42.0,
            // l'échec est garanti.
            CoefficientNulException ex = new CoefficientNulException(42.0);
            assertEquals(42.0, ex.getCoefficient(), 0.0,
                "getCoefficient doit renvoyer la valeur stockée, pas 0.0");
        }

        @Test
        @DisplayName("Constructeur avec valeur négative")
        void coefficientNegatif() {
            CoefficientNulException ex = new CoefficientNulException(-7.5);
            assertEquals(-7.5, ex.getCoefficient(), 0.0);
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  #6 — Ligne 111 : b >= 0.0  →  b > 0.0  — ÉQUIVALENT
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Survivant #6 — boundary b >= 0 (ligne 111) — ÉQUIVALENT")
    class SurvivantBoundaryB {

        /**
         * Ce mutant n'est observable que si b == 0.0 exactement.
         * Pour b = 0, les racines sont symétriques ±k : les deux
         * branches produisent {−k, +k} ou {+k, −k}, et le tri
         * Math.min/Math.max rend le tableau identique.
         *
         * Ce test documente/explore le cas b = 0 pour montrer que
         * le résultat est bien le même quel que soit le sens de
         * la comparaison — confirmation de l'équivalence.
         */
        @Test
        @DisplayName("b = 0, delta > 0 : racines symétriques (équivalence)")
        void bZeroRacinesSymetriques() {
            // x² − 1 = 0  →  racines {−1, +1}
            double[] racines = solveur.resoudre(1.0, 0.0, -1.0);
            assertEquals(2, racines.length);
            assertEquals(-1.0, racines[0], 1e-12);
            assertEquals(1.0, racines[1], 1e-12);
            // Symétrie : x1 + x2 = 0 = −b/a
            assertEquals(0.0, racines[0] + racines[1], 1e-12);
        }

        @Test
        @DisplayName("b = 0, a ≠ 1 : racines symétriques aussi")
        void bZeroRacinesSymetriquesA4() {
            // 4x² − 4 = 0  →  x = ±1
            double[] racines = solveur.resoudre(4.0, 0.0, -4.0);
            assertEquals(2, racines.length);
            assertEquals(-1.0, racines[0], 1e-12);
            assertEquals(1.0, racines[1], 1e-12);
        }

        // NOTE : ces tests PASSSENT aussi avec le mutant (b > 0 au lieu
        // de b >= 0) car le résultat final est identique après tri.
        // C'est la preuve opérationnelle de l'équivalence → justifié.
    }
}
