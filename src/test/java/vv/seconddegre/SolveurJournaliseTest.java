package vv.seconddegre;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import vv.seconddegre.exceptions.CoefficientNulException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =====================================================================
 *  TESTS DU DÉCORATEUR  —  SolveurJournaliseTest
 * =====================================================================
 *
 * <h2>Objectif couverture 100% (JaCoCo) / mutation (PIT)</h2>
 * Le décorateur {@link SolveurJournalise} est du <b>code produit</b> :
 * il doit être aussi bien testé que le SUT. Cette classe vérifie :
 * <ul>
 *   <li>la <b>délégation</b> (résultats identiques au solveur nu) ;</li>
 *   <li>les <b>compteurs</b> (appels, cas sans racine) ;</li>
 *   <li>le <b>journal</b> (entrée, résultat, exception) ;</li>
 *   <li>la <b>propagation des exceptions</b> (pas de swallow) ;</li>
 *   <li>l'<b>empilement</b> de deux décorateurs (pattern) ;</li>
 *   <li>la <b>réinitialisation</b> des compteurs.</li>
 * </ul>
 */
class SolveurJournaliseTest {

    /** SUT nu, sans décor. */
    private final SolveurSecondDegre solveur = new SolveurSecondDegre();

    // ────────────────────────────────────────────────────────────────
    //  NIVEAU 1 : délégation transparente
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Délégation — même résultat que le solveur nu")
    class DelegationTests {

        @Test
        @DisplayName("Deux racines : identiques au solveur nu")
        void deuxRacinesIdentiques() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "test");
            assertArrayEquals(solveur.resoudre(1, -3, 2), decore.resoudre(1, -3, 2));
        }

        @Test
        @DisplayName("Racine double : identique au solveur nu")
        void racineDoubleIdentique() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "test");
            assertArrayEquals(solveur.resoudre(1, 2, 1), decore.resoudre(1, 2, 1));
        }

        @Test
        @DisplayName("Delta négatif : tableau vide identique")
        void deltaNegatifIdentique() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "test");
            assertArrayEquals(new double[0], decore.resoudre(1, 0, 1));
        }

        @Test
        @DisplayName("Accesseurs delegate / nom")
        void accesseursDelegateEtNom() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "mono-log");
            assertSame(solveur, decore.getDelegate());
            assertEquals("mono-log", decore.getNom());
        }

        @Test
        @DisplayName("Delegate null refusé (pré-condition du décorateur)")
        void delegateNullRefuse() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SolveurJournalise(null, "x"));
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  NIVEAU 2 : compteurs
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Compteurs — appels et cas sans racine")
    class CompteurTests {

        @Test
        @DisplayName("Compteurs initiaux à zéro")
        void compteursInitiaux() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "test");
            assertEquals(0, decore.getNombreAppels());
            assertEquals(0, decore.getNombreSansRacine());
            assertEquals("", decore.getJournal());
        }

        @Test
        @DisplayName("Trois appels → nombreAppels = 3")
        void troisAppels() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "test");
            decore.resoudre(1, -3, 2);
            decore.resoudre(1, 2, 1);
            decore.resoudre(2, 4, 2);
            assertEquals(3, decore.getNombreAppels());
        }

        @Test
        @DisplayName("Un seul cas δ<0 → nombreSansRacine = 1")
        void casSansRacine() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "test");
            decore.resoudre(1, 0, 1);   // δ = -4 → 0 racine
            decore.resoudre(1, -3, 2);  // δ =  1 → 2 racines
            assertEquals(2, decore.getNombreAppels());
            assertEquals(1, decore.getNombreSansRacine());
        }

        @Test
        @DisplayName("Appel qui lève une exception compte quand même")
        void appelEnExceptionCompte() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "test");
            assertThrows(CoefficientNulException.class,
                    () -> decore.resoudre(0, 1, 1));
            assertEquals(1, decore.getNombreAppels());
            assertEquals(0, decore.getNombreSansRacine());
        }

        @Test
        @DisplayName("reinitialiser() remet journal et compteurs à zéro")
        void reinitialisation() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "test");
            decore.resoudre(1, -3, 2);
            decore.reinitialiser();
            assertEquals(0, decore.getNombreAppels());
            assertEquals(0, decore.getNombreSansRacine());
            assertEquals("", decore.getJournal());
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  NIVEAU 3 : journal (contenu)
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Journal — entrée, résultat, exception")
    class JournalTests {

        @Test
        @DisplayName("Le journal contient les coefficients appelés")
        void journalContientAppel() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "audit");
            decore.resoudre(1.0, -3.0, 2.0);
            String journal = decore.getJournal();
            assertTrue(journal.contains("resoudre(1.0, -3.0, 2.0)"));
            assertTrue(journal.contains("[audit]"));
        }

        @Test
        @DisplayName("Le journal contient le nombre de racines")
        void journalContientResultat() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "audit");
            decore.resoudre(1, -3, 2);
            assertTrue(decore.getJournal().contains("2 racine(s)"));
        }

        @Test
        @DisplayName("Le journal contient l'exception levée puis la propage")
        void journalContientException() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "audit");
            assertThrows(CoefficientNulException.class,
                    () -> decore.resoudre(0, 1, 1));
            String journal = decore.getJournal();
            assertTrue(journal.contains("EXCEPTION CoefficientNulException"));
            // la ligne d'entrée reste tracée malgré l'exception
            assertTrue(journal.contains("resoudre(0.0, 1.0, 1.0)"));
        }

        @Test
        @DisplayName("Deux appels → deux lignes numérotées #1 et #2")
        void journalDeuxLignes() {
            SolveurJournalise decore = new SolveurJournalise(solveur, "audit");
            decore.resoudre(1, -3, 2);
            decore.resoudre(1, 0, 1);
            String journal = decore.getJournal();
            assertTrue(journal.contains("#1 "));
            assertTrue(journal.contains("#2 "));
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  NIVEAU 4 : pattern décorateur — empilement
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Pattern — empilement de décorateurs")
    class EmpilementTests {

        @Test
        @DisplayName("Décorateur de décorateur : les deux journalisent")
        void doubleDecorateur() {
            SolveurJournalise interne = new SolveurJournalise(solveur, "interne");
            SolveurJournalise externe = new SolveurJournalise(interne, "externe");

            double[] racines = externe.resoudre(1, -3, 2);

            assertArrayEquals(new double[]{1.0, 2.0}, racines);
            assertEquals(1, externe.getNombreAppels());
            assertEquals(1, interne.getNombreAppels());  // appel transmis
            assertTrue(externe.getJournal().contains("[externe]"));
            assertTrue(interne.getJournal().contains("[interne]"));
        }

        @Test
        @DisplayName("L'empilement préserve la propagation d'exception")
        void exceptionTraverseLesDecorateurs() {
            SolveurJournalise interne = new SolveurJournalise(solveur, "interne");
            SolveurJournalise externe = new SolveurJournalise(interne, "externe");
            assertThrows(CoefficientNulException.class,
                    () -> externe.resoudre(0, 1, 1));
            assertEquals(1, externe.getNombreAppels());
            assertEquals(1, interne.getNombreAppels());
        }
    }
}
