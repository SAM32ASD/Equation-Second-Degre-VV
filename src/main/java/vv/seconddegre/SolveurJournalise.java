package vv.seconddegre;

import java.util.Arrays;

/**
 * =====================================================================
 *  DÉCORATEUR  —  SolveurJournalise
 * =====================================================================
 *
 * <h2>Pattern</h2>
 * <b>Décorator</b> (GoF) : cette classe implémente le même contrat
 * {@link ISolveurSecondDegre} que le solveur « réel » et lui
 * <b>délègue</b> le calcul, en ajoutant un comportement
 * transversal — ici la <b>journalisation</b> et le
 * <b>comptage</b> — sans modifier le SUT.
 *
 * <pre>
 *   client ──&gt; ISolveurSecondDegre
 *                    ▲
 *                    │ implémente
 *          ┌─────────┴──────────┐
 *   SolveurJournalise ──delegate──&gt; SolveurSecondDegre
 *   (journal + compteurs)          (calcul seul)
 * </pre>
 *
 * <h2>Avantages démontrés au projet</h2>
 * <ul>
 *   <li><b>Ouvert/fermé</b> : ajouter le journal ne touche pas au SUT ;</li>
 *   <li><b>Transparence</b> : le client continue d'appeler
 *       {@code resoudre(a,b,c)} ;</li>
 *   <li><b>Empilement</b> : plusieurs décorateurs se chaînent
 *       (décorateur de décorateur).</li>
 * </ul>
 *
 * <h2>Comportement observé</h2>
 * <ul>
 *   <li>chaque appel est tracé dans un journal (entrée + résultat) ;</li>
 *   <li>{@link #getNombreAppels()} compte tous les appels ;</li>
 *   <li>{@link #getNombreSansRacine()} compte les cas δ &lt; 0 ;</li>
 *   <li>les exceptions du delegate sont journalisées puis
 *       <b>relancées telles quelles</b> (pas de swallow).</li>
 * </ul>
 *
 * @author Projet V&amp;V
 * @version 1.0
 */
public class SolveurJournalise implements ISolveurSecondDegre {

    /** Solveur réel (SUT) qui fait le vrai calcul. */
    private final ISolveurSecondDegre delegate;

    /** Nom lisible du décorateur (utile dans les logs / tests). */
    private final String nom;

    /** Journal des appels (une ligne par appel). */
    private final StringBuilder journal = new StringBuilder();

    /** Nombre d'appels reçus par {@link #resoudre}. */
    private long nombreAppels;

    /** Nombre d'appels ayant renvoyé 0 racine (δ &lt; 0). */
    private long nombreSansRacine;

    /**
     * Construit le décorateur autour d'un solveur existant.
     *
     * @param delegate solveur à décorer (jamais {@code null})
     * @param nom      nom affichable du décorateur
     */
    public SolveurJournalise(ISolveurSecondDegre delegate, String nom) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate obligatoire");
        }
        this.delegate = delegate;
        this.nom = nom;
    }

    /**
     * Résout en journalisant l'entrée, le résultat (ou l'exception)
     * puis en déléguant le calcul au solveur décoré.
     *
     * @param a coefficient de x²
     * @param b coefficient de x
     * @param c terme constant
     * @return tableau des racines renvoyé par le delegate
     * @throws vv.seconddegre.exceptions.EquationException
     *         propagée telle quelle après journalisation
     */
    @Override
    public double[] resoudre(double a, double b, double c) {
        nombreAppels++;
        journal.append('#').append(nombreAppels)
               .append(" [").append(nom).append("] resoudre(")
               .append(a).append(", ").append(b).append(", ").append(c)
               .append(')');

        double[] racines;
        try {
            racines = delegate.resoudre(a, b, c);
        } catch (RuntimeException e) {
            journal.append(" -> EXCEPTION ")
                   .append(e.getClass().getSimpleName()).append('\n');
            throw e;
        }

        if (racines.length == 0) {
            nombreSansRacine++;
        }
        journal.append(" -> ").append(racines.length)
               .append(" racine(s) ").append(Arrays.toString(racines))
               .append('\n');
        return racines;
    }

    /** @return le solveur décoré (pour empiler d'autres décorateurs) */
    public ISolveurSecondDegre getDelegate() {
        return delegate;
    }

    /** @return le nom du décorateur */
    public String getNom() {
        return nom;
    }

    /** @return nombre total d'appels à {@link #resoudre} */
    public long getNombreAppels() {
        return nombreAppels;
    }

    /** @return nombre d'appels sans racine réelle (δ &lt; 0) */
    public long getNombreSansRacine() {
        return nombreSansRacine;
    }

    /** @return le journal complet (une ligne par appel) */
    public String getJournal() {
        return journal.toString();
    }

    /**
     * Vide le journal et remet les compteurs à zéro.
     */
    public void reinitialiser() {
        journal.setLength(0);
        nombreAppels = 0;
        nombreSansRacine = 0;
    }
}
