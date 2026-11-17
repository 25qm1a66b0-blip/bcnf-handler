package com.bcnf;

import com.bcnf.db.BCNFDatabaseMiddleware;
import com.bcnf.exception.BCNFViolationException;
import com.bcnf.model.FunctionalDependency;
import com.bcnf.model.Relation;
import com.bcnf.sql.BCNFSQLGenerator;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Console-based demonstration of all three capabilities:
 * 1. Interactive Architect - validate a schema
 * 2. Database Middleware Guard - intercept violations before DB creation
 * 3. Self-Healing Normalizer - generate SQL refactoring scripts
 *
 * @author BCNF Violation Exception Handler (Group-29)
 */
public final class Demo {

    public static void main(String[] args) throws SQLException {
        System.out.println("╔═══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║     BCNF VIOLATION EXCEPTION HANDLER — Console Demo                 ║");
        System.out.println("║     Group-29  •  KG Reddy College of Engineering & Technology       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // ───────────────────────────────────────────────────────────────
        // 1. BUILD THE SAMPLE RELATION FROM THE PPT
        //    StudentCourse(StudentID, Course, Instructor)
        //    FDs: (StudentID, Course) → Instructor
        //         Instructor → Course  (VIOLATES BCNF)
        // ───────────────────────────────────────────────────────────────
        Set<String> attrs = new LinkedHashSet<>(Set.of("StudentID", "Course", "Instructor"));
        Set<FunctionalDependency> fds = new LinkedHashSet<>();
        fds.add(new FunctionalDependency(Set.of("StudentID", "Course"), Set.of("Instructor")));
        fds.add(new FunctionalDependency(Set.of("Instructor"), Set.of("Course")));

        Relation studentCourse = new Relation("StudentCourse", attrs, fds);

        // ───────────────────────────────────────────────────────────────
        // 2. INTERACTIVE ARCHITECT: VALIDATE BCNF (THROWS EXCEPTION)
        // ───────────────────────────────────────────────────────────────
        System.out.println("┌────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ 1. INTERACTIVE ARCHITECT — Validate BCNF                         │");
        System.out.println("└────────────────────────────────────────────────────────────────────┘");
        System.out.println();

        try {
            com.bcnf.algorithm.BCNFValidator.validate(studentCourse);
            System.out.println("✅ No violation — relation is in BCNF");
        } catch (BCNFViolationException ex) {
            System.out.println(ex.getMessage());
            System.out.println();
            System.out.println("🔧 SUGGESTED DECOMPOSITION:");
            for (String s : ex.getDecompositionSuggestions()) {
                System.out.println("   → " + s);
            }
            System.out.println();
            System.out.println("📊 Properties:");
            System.out.println("   • Lossless-join: " + (ex.isLossless() ? "✅ Yes" : "❌ No"));
            System.out.println("   • Dependency preserving: " + (ex.isDependencyPreserving() ? "✅ Yes" : "❌ No"));
        }
        System.out.println();

        // ───────────────────────────────────────────────────────────────
        // 3. DATABASE MIDDLEWARE GUARD: INTERCEPT BEFORE DB CREATION
        // ───────────────────────────────────────────────────────────────
        System.out.println("┌────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ 2. DATABASE MIDDLEWARE GUARD — H2 In-Memory                       │");
        System.out.println("└────────────────────────────────────────────────────────────────────┘");
        System.out.println();

        BCNFDatabaseMiddleware middleware = BCNFDatabaseMiddleware.createH2();
        System.out.println("🔗 Connected to H2 in-memory database");
        System.out.println();

        try {
            middleware.createTable(
                    studentCourse.getName(),
                    studentCourse.getAttributes(),
                    studentCourse.getFunctionalDependencies()
            );
            System.out.println("✅ Table created successfully");
        } catch (BCNFViolationException ex) {
            System.out.println("🛑 MIDDLEWARE INTERCEPTED VIOLATION BEFORE DDL EXECUTION");
            System.out.println("   " + ex.getShortSummary());
            System.out.println("   The table was NOT created in the database.");
        }
        middleware.close();
        System.out.println();

        // ───────────────────────────────────────────────────────────────
        // 4. SELF-HEALING NORMALIZER: GENERATE SQL REFACTORING SCRIPT
        // ───────────────────────────────────────────────────────────────
        System.out.println("┌────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ 3. SELF-HEALING NORMALIZER — SQL Refactoring Script               │");
        System.out.println("└────────────────────────────────────────────────────────────────────┘");
        System.out.println();

        BCNFViolationException ex = new BCNFViolationException(
                studentCourse,
                new FunctionalDependency(Set.of("Instructor"), Set.of("Course")),
                List.of("{StudentID, Course}"),
                List.of(
                        "R1(Instructor, Course)  -- determinant + dependent",
                        "R2(StudentID, Course, Instructor)  -- determinant + remaining",
                        "FDs in R1: Instructor -> Course",
                        "FDs in R2: Instructor -> Course (X is a super key of R2)"
                ),
                true,  // lossless
                false  // not dependency preserving
        );

        String sqlScript = BCNFSQLGenerator.generateRefactoringScript(ex);
        System.out.println(sqlScript);
        System.out.println();

        String verifyQuery = BCNFSQLGenerator.generateVerificationQuery(ex);
        System.out.println(verifyQuery);
        System.out.println();

        // ───────────────────────────────────────────────────────────────
        // 5. BONUS: SHOW A VALID BCNF RELATION
        // ───────────────────────────────────────────────────────────────
        System.out.println("┌────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ 4. BONUS — A Valid BCNF Relation                                  │");
        System.out.println("└────────────────────────────────────────────────────────────────────┘");
        System.out.println();

        Set<String> validAttrs = new LinkedHashSet<>(Set.of("A", "B", "C"));
        Set<FunctionalDependency> validFds = new LinkedHashSet<>();
        validFds.add(new FunctionalDependency(Set.of("A"), Set.of("B", "C")));
        Relation valid = new Relation("ValidR", validAttrs, validFds);

        try {
            com.bcnf.algorithm.BCNFValidator.validate(valid);
            System.out.println("✅ Relation " + valid + " is in BCNF");
            System.out.println("   Candidate Key(s): {A}");
            System.out.println("   FD: A -> B, C  (A is a super key)");
        } catch (BCNFViolationException e) {
            System.out.println("Unexpected violation: " + e.getMessage());
        }
        System.out.println();

        System.out.println("╔═══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║     Demo complete. Run 'mvn exec:java -Dexec.mainClass=com.bcnf.ui.Main║");
        System.out.println("║     for the Swing GUI with full interactive capabilities.           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════╝");
    }
}