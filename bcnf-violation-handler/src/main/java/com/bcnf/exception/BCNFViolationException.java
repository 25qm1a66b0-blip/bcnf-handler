package com.bcnf.exception;

import com.bcnf.model.FunctionalDependency;
import com.bcnf.model.Relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A custom, checked runtime exception that intercepts Boyce-Codd Normal Form (BCNF)
 * violations caused by key functional dependency violations.
 *
 * <p>A relation is in BCNF if and only if every non-trivial functional dependency
 * X -> Y has a <em>super key</em> as its determinant. When a determinant is not a
 * super key, the schema violates BCNF and this exception is thrown.</p>
 *
 * <p>The exception carries the violating dependency, the computed candidate keys,
 * a human readable explanation and a list of decomposition suggestions so the
 * caller can repair the schema without having to re-derive the analysis.</p>
 *
 * @author BCNF Violation Exception Handler (Group-29)
 * @since 1.0
 */
public class BCNFViolationException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Relation relation;
    private final FunctionalDependency violatingDependency;
    private final List<String> candidateKeys;
    private final List<String> decompositionSuggestions;
    private final boolean lossless;
    private final boolean dependencyPreserving;

    /**
     * Constructs a BCNFViolationException with the full diagnostic payload.
     *
     * @param relation                the relation that violates BCNF
     * @param violatingDependency    the functional dependency whose determinant is not a super key
     * @param candidateKeys          the candidate keys computed for the relation
     * @param decompositionSuggestions a list of suggested decomposed relations
     * @param lossless               whether the suggested decomposition is lossless-join
     * @param dependencyPreserving   whether the suggested decomposition preserves all FDs
     */
    public BCNFViolationException(Relation relation,
                                  FunctionalDependency violatingDependency,
                                  List<String> candidateKeys,
                                  List<String> decompositionSuggestions,
                                  boolean lossless,
                                  boolean dependencyPreserving) {
        super(buildMessage(relation, violatingDependency, candidateKeys));
        this.relation = Objects.requireNonNull(relation, "relation must not be null");
        this.violatingDependency = Objects.requireNonNull(violatingDependency, "violatingDependency must not be null");
        this.candidateKeys = candidateKeys == null ? new ArrayList<>() : new ArrayList<>(candidateKeys);
        this.decompositionSuggestions = decompositionSuggestions == null ? new ArrayList<>()
                : new ArrayList<>(decompositionSuggestions);
        this.lossless = lossless;
        this.dependencyPreserving = dependencyPreserving;
    }

    /**
     * Builds a comprehensive, human-readable diagnostic message suitable for display
     * in logs, console output or a graphical error dialog.
     */
    private static String buildMessage(Relation relation,
                                       FunctionalDependency violatingDependency,
                                       List<String> candidateKeys) {
        StringBuilder sb = new StringBuilder();
        sb.append("BCNF VIOLATION DETECTED").append(System.lineSeparator());
        sb.append("==================================================").append(System.lineSeparator());
        sb.append("Relation     : ").append(relation.getName()).append(System.lineSeparator());
        sb.append("Attributes   : ").append(relation.getAttributes()).append(System.lineSeparator());
        sb.append("Candidate Key(s): ").append(candidateKeys == null ? "[]" : candidateKeys)
          .append(System.lineSeparator());
        sb.append("--------------------------------------------------").append(System.lineSeparator());
        sb.append("Violating FD : ").append(violatingDependency).append(System.lineSeparator());
        sb.append("Determinant  : ").append(violatingDependency.getDeterminant())
          .append(" is NOT a super key of the relation.").append(System.lineSeparator());
        sb.append("--------------------------------------------------").append(System.lineSeparator());
        sb.append("Explanation  : In BCNF every non-trivial functional dependency must have a ")
          .append("super key as its determinant. The dependency above uses a proper subset of ")
          .append("attributes as a determinant, so the relation is not in BCNF and suffers from ")
          .append("insertion/update/deletion anomalies.").append(System.lineSeparator());
        sb.append("==================================================");
        return sb.toString();
    }

    /**
     * @return the relation that violates BCNF
     */
    public Relation getRelation() {
        return relation;
    }

    /**
     * @return the violating functional dependency
     */
    public FunctionalDependency getViolatingDependency() {
        return violatingDependency;
    }

    /**
     * @return an unmodifiable view of the candidate keys
     */
    public List<String> getCandidateKeys() {
        return List.copyOf(candidateKeys);
    }

    /**
     * @return an unmodifiable view of the decomposition suggestions
     */
    public List<String> getDecompositionSuggestions() {
        return List.copyOf(decompositionSuggestions);
    }

    /**
     * @return true if the suggested decomposition is lossless-join
     */
    public boolean isLossless() {
        return lossless;
    }

    /**
     * @return true if the suggested decomposition preserves all functional dependencies
     */
    public boolean isDependencyPreserving() {
        return dependencyPreserving;
    }

    /**
     * Returns a short, one-line summary of the violation.
     */
    public String getShortSummary() {
        return "BCNF violation on relation '" + relation.getName() + "' by FD " + violatingDependency
                + " (determinant '" + violatingDependency.getDeterminant() + "' is not a super key).";
    }

    @Override
    public String toString() {
        return "BCNFViolationException{" +
                "relation=" + relation.getName() +
                ", violatingDependency=" + violatingDependency +
                ", candidateKeys=" + candidateKeys +
                ", lossless=" + lossless +
                ", dependencyPreserving=" + dependencyPreserving +
                ", decompositionSuggestions=" + decompositionSuggestions +
                '}';
    }
}