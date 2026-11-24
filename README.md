# Library of Things

A small Java 17 console application where neighbours register, add items,
borrow items, return items, and pay a late fee of 5 TL per day.
Data is kept in memory (no database, no GUI).

## How to build, test, and run

```
mvn clean test        # compile and run all JUnit 5 tests
mvn clean verify      # tests + JaCoCo coverage report
                      # report: target/site/jacoco/index.html
java -cp target/classes library.Main     # start the menu (after mvn verify)
```

## Classes (`src/main/java/library`)

| Class | Job |
|---|---|
| `Member` | ID, name, and how many items the member has borrowed now |
| `Item` | ID, name, category, owner ID, available yes/no |
| `Loan` | Item, member, borrow date, due date (borrow date + days), returned yes/no |
| `Validator` | Checks ID format (M0001 / I0001) and loan length (1-14 days) |
| `FineCalculator` | Late fee: 5 TL per day after the due date, 0 if on time or early |
| `LendingService` | All business rules (FR1-FR5); stores members, items, and loans in ArrayLists |
| `LibraryException` | The error thrown when an input or operation is rejected |
| `Main` | Text menu with Scanner; calls LendingService and prints results |

## Business rules

| Rule | Value |
|---|---|
| Member ID / Item ID format | `M` + 4 digits / `I` + 4 digits (digits 0-9 only) |
| Loan length | 1 to 14 days |
| Active loans per member | at most 2 |
| Active loans per item | at most 1 |
| Owner borrows own item | not allowed |
| Late fee | 5 TL per day after the due date |

Borrow checks run in this order; the first one that fails is reported and
nothing is changed: ID formats -> member exists -> item exists -> loan length ->
not the owner -> item available -> under the 2-item limit.

## Tests (`src/test/java/library`)

| Test class | Technique |
|---|---|
| `EquivalencePartitionTest` | Equivalence partitioning |
| `BoundaryValueTest`, `FineCalculatorTest` | Boundary value analysis |
| `BorrowDecisionTableTest` | Decision table (table is in the class comment) |
| `NegativeTest` | Negative testing |
| `RegressionTest`, `MainTest` | Regression (end-to-end story + tests for defects D1 and D2), console |
| `LendingServiceTest`, `MemberTest`, `ItemTest`, `LoanTest` | Normal behaviour of each class |

Saved results are in `test-results/`:
the final Maven log, the failing runs for D1 and D2, the console demo, and the JaCoCo HTML report.

Final run (24 Nov 2026): 78 tests, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS.
JaCoCo: 97% instructions (915/936), 100% branches (112/112).
