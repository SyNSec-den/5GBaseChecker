# Changelog
All notable changes to this project will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [0.11.0-SNAPSHOT] - Unreleased

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.10.0...HEAD)

### Added

* Added modal transition systems (MTSs), modal contracts (MCs) and Membership-MCs (MMCs).
* Added `SubsequentialTransducer` interface and implementations/utilities.
* Added support for systems of procedural automata (SPAs) as well as related concepts (equivalence, etc.).
* Added the M3C model-checker for verifying µ-calculus and CTL formulas on context-free modal process systems (thanks to [Alnis Murtovi](https://github.com/AlnisM)).
* Added the ability to M3C to generate witnesses for negated safety properties (thanks to [Maximilian Freese](https://github.com/Viperish-byte)).

### Changed

* Refactored
  * `net.automatalib.automata.transducers.impl.compact.CompactMealyTransition` -> `net.automatalib.automata.base.compact.CompactTransition`
  * `net.automatalib.commons.util.BitSetIterator` -> `net.automatalib.commons.util.collections.BitSetIterator`
  * `net.automatalib.graphs.base.compact.AbstractCompactGraph#getNodeProperties(int)` -> `net.automatalib.graphs.base.compact.AbstractCompactGraph#getNodeProperty(int)`
  * `net.automatalib.graphs.FiniteKTS` -> `net.automatalib.ts.FiniteKTS` and `FiniteKTS` no longer extends the `Graph` interface but the `Automaton` interface and has its type variables re-ordered.
  * `net.automatalib.graphs.FiniteLTS` -> `net.automatalib.graphs.FiniteLabeledGraph`
  * moved the package `net.automatalib.ts.comp` to `net.automatalib.util.ts.comp` in the `automata-util` module
* AutomataLib classes no longer implement `Serializable`. We never fully supported the semantics of the interface and never intended to do so. In fact, the old approach failed miserably if any class was involved where we missed an "implements Serializable" statement. In order to prevent confusion by promising false contracts, implementing this markup interface has been removed. Serialization should now be done in user-land via one of the many external (and more optimizable) serialization frameworks such as FST, XStream, etc.
* `Minimizer` no longer provides a `getInstance()` method but can be instantiated directly.
* The `OneSEVPA` interface has been generalized to an arbitrary (k-)`SEVPA` interface. The old `OneSEVPA` specialization is still available and unchanged.
* `AbstractOneSEVPA` no longer implements the `Graph` interface, but `SEVPA`s are now `GraphViewable`.
* `Symbol` now has a type-safe user object and id-based `hashcode`/`equals` semantics.

### Fixed

* Fixed a cache consistency bug in various DAG-based incremental builders.
* Fixed a regression in `AbstractLTSminMonitorMealy` regarding BBC (#46).
* Fixed a bug in `CharacterizingSets` which ignored the semantics of acceptors, i.e., not all states of an acceptor could be distinguished solely based on acceptance.

### Removed

* Removed the (package-private) classes `net.automatalib.util.automata.predicates.{AcceptanceStatePredicate,OutputSatisfies,TransitionPropertySatisfies}`.
* Removed `net.automatalib.graphs.IndefiniteLTS.java`. By naming, this class should denote a `TransitionSystem` and not a `Graph` structure. However, since `TransitionSystem`s are inherently labeled, this class serves no more real purpose. To re-establish the previous functionality, simply implement `Graph` and `EdgeLabels`.
* Removed (unused) `SuffixTrie` class without replacement. Similar functionality can be achieved with AutomataLib's incremental module.
* Removed non-static methods on `RandomAutomata` factory (including the `getInstance()` method).
* Removed `AbstractCompactNPGraph`, use `AbstractCompactGraph` instead.
* Removed `AbstractCompactSimpleGraph`. All functionality is provided in `CompactSimpleGraph`.
* Removed `Iterable`- and `Iterator`-based methods for adjacent nodes/edges on graphs. For lazy/indefinite fetching use the `Stream`-based methods, for complete fetching use the `Collection`-based methods.


## [0.10.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.10.0) - 2020-10-11

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.9.0...automatalib-0.10.0)

### Added

* All de-serializers now can also parse GZIP'ed contents from either `InputStream`s, `File`s, `URL`s or `byte[]`s.

### Changed

* We overhauled the handling of input and output streams for all (de-)serializers. Input and output streams are no longer closed automatically. This is to prevent asymmetric code where we would close a stream that we haven't opened. This is problematic in cases where e.g. `System.out` is passed as an output stream to simply print a serialized automaton and the `System.out` stream would be closed afterwards. Since input and output streams are usually opened in client-code, they should be closed in client-code as well. We suggest to simply wrap calls to the serializers in a try-with-resource block.
* Due to the DOT parsers rewrite (see **Fixed**), the attribute parsers now receive a `Map<String, String>` instead of a `Map<String, Object>`.
* The `State` class (used by the `OneSEVPA` automaton) not longer supports the notion of a sink state. The `AbstractOneSEVPA` class now conforms with the default semantics of a `DeterministicTransitionSystem` that undefined transitions simply return `null`.

### Removed

* Removed `IOUtil#copy`, `IOUtil#skip`, `NullOutputStream`. Use the Guava equivalents from `ByteStreams` and `CharStreams`.

### Fixed

* Correctly enquote outputs containing whitespaces in `TAFWriter` ([#37](https://github.com/LearnLib/automatalib/issues/37), thanks to [Alexander Schieweck](https://github.com/aschieweck)).
* Fixed a bug in the `Graph` representation of `AbstractOneSEVPA`s ([#39](https://github.com/LearnLib/automatalib/pull/39), thanks to [DonatoClun](https://github.com/DonatoClun)).
* Fixed wrong default values in the overloaded methods of the `Minimizer` class ([#41](https://github.com/LearnLib/automatalib/issues/41)).
* Replaced the 3rd-party DOT parser with our own implementation to fix the issue that multi-edges between nodes were not properly handled.


## [0.9.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.9.0) - 2020-02-05

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.8.0...automatalib-0.9.0)

### Added

* A new alphabet implementation `MapAlphabet` that is based on the old `SimpleAlphabet` but without implementing the `GrowingAlphabet` interface.
* `IntRangeIterator` now implements `PrimitiveIterator.OfInt`.
* A parser for `dot` files has been added and is available in the `automata-serialization-dot` module via the `DOTParsers` factory.
* A new `PaigeTarjanMinimization` factory that offers convenience methods for PaigeTarjan-based automata minimization (with support for e.g. partial automata) has been added.
* Performance improvements for some alphabet implementations (`DefaultVPDAlphabet`, `ListAlphabet`, `{Int,Char}Range`, ...).
* An `LTSminLTLParser` to verify if LTL properties match the expected syntax of LTSmin.
* A `WordCollector` for collecting words from symbol `Stream`s.

### Changed

* Refactored the following packages/classes:
  * `net.automatalib.words.impl.SimpleAlphabet` -> `net.automatalib.words.impl.GrowingMapAlphabet`
  * `net.automatalib.serialization.AutomatonSerializationException` -> `net.automatalib.serialization.FormatException`
  * `net.automatalib.serialization.fsm.parser.FSMParseException` -> `net.automatalib.serialization.fsm.parser.FSMFormatException`
  * `net.automatalib.serialization.taf.parser.TAFParseException` -> `net.automatalib.serialization.taf.parser.TAFFormatException`
  * `net.automatalib.automata.transducers.impl.map.SimpleMapGraph` -> `net.automatalib.graphs.map.SimpleMapGraph`
* The `BackedGeneralPriorityQueue(Class<T> c)` constructor was replaced with a more idiomatic `BackedGeneralPriorityQueue(Supplier<T> s)` constructor.
* The TAF serialization code now forwards (i.e. `throws`) `IOException`s that occur when reading from the various data sources.
* JSR305 annotations have been replaced with checker-framework annotations.
  * AutomataLib now follows checker-framework's convention that (non-annotated) types are usually considered non-null unless explicitly annotated with `@Nullable`.
  * AutomataLib no longer has a (runtime-) dependency on JSR305 (and other `javax.*`) annotations or includes them in the distribution artifact. This now makes AutomataLib compliant with [Oracle's binary code license](https://www.oracle.com/downloads/licenses/binary-code-license.html) and allows AutomataLib artifacts as-is to be bundled in binary distributions with Oracle's JDKs/JREs.

### Removed

* `StringIndexGenerator` has been removed. Its (streamlined) functionality is now available as `Mapping` via `Mappings#stringToIndex` and `Mappings#indexToString`.
* `DelegatingIterator`: If you used this class, switch to Guavas equivalent `ForwardingIterator`.
* `ParameterMismatchException`: Was unused and never thrown.
* `StateIDDynamicMapping`: Equivalent functionality is provided by `ArrayMapping`.
* `ResizingIntArray`: Internal data structure was unused.
* The `VPDAlphabet` interface no longer specifies the `get{Internal,Call,Return}Symbols()` methods, as the `get{Internal,Call,Return}Alphabet()` methods supersede their functionality.
* `PaigeTarjan{Initializers,Extractors}` methods for non `IntAbstraction`s have been removed. You may use the new convenience methods provided by `PaigeTarjanMinimization`.
* Further `@Deprecated` methods have been removed.
* Removed `ProbMealyTransition` and replaced it with the generic `MealyTransition` directly carrying the `ProbabilisticOutput`. It is no longer allowed to pass `null` as transition property.
* Removed `OutputAndLocalInputs`, `StateLocalInputIncrementalMealyTreeBuilder` and `StateLocalInputMealyUtil`. The (LearnLib) code related to inferring partial Mealy machines no longer requires these transformed automata. Whoever used this code for transforming a partial Mealy machine to a complete one, may use `MealyMachines#complete` instead.

### Fixed

* Fixed a bug, where the PaigeTarjan-based minimization would return a wrong automaton when pruning was enabled and the initial automaton was partial.
* Fixed a bug in our Tarjan SCC implementation ([#35](https://github.com/LearnLib/automatalib/pull/35), thanks to [Malte Mues](https://github.com/mmuesly))
* Fixed a bug in the computation of characterizing sets ([#36](https://github.com/LearnLib/automatalib/issues/36)).
* Several (null-related) bugs detected by our ongoing efforts to write tests.


## [0.8.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.8.0) - 2019-02-07

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.7.1...automatalib-0.8.0)

### Added

* Added support for model checking automata using the [LTSmin](https://github.com/utwente-fmt/ltsmin) model checker (thanks to [Jeroen Meijer](https://github.com/Meijuh)).
* Added a `CompactMoore` implementation.
* Added an `AutomatonBuilder` for Moore Machines.
* Added support for adding input symbols to incremental constructions (tree caches, DAG caches).
* Support for Java 11. **Note:** AutomataLib still targets Java 8, and thus needs classes provided by this environment (specifically: annotations from `javax.annotation`). If you plan to use AutomataLib in a Java 11+ environment, make sure to provide these classes. They are not shipped with AutomataLib.

### Changed

* Refactored the following packages/classes:
  * `net.automatalib.automata.transout` -> `net.automatalib.automata.transducers`
  * `net.automatalib.ts.transout` -> `net.automatalib.ts.output`
  * `net.automatalib.util.automata.transout` -> `net.automatalib.util.automata.transducers`
  * `net.automatalib.automata.GrowableAlphabetAutomaton` -> `net.automatalib.SupportsGrowingAlphabet`
* Some runtime properties for dynamically configuring AutomataLib have been renamed. There now exists the `AutomataLibProperty` enum as a single reference point for all available properties.
* Several of the `AbstractCompact*` automata classes have been refactored to share common functionality. While this shouldn't affect its implementations (such as `CompactDFA` or `CompactMealy`) user-land code using the abstract classes may break.
* The `BricsDFA` wrapper previously allowed (via a boolean flag) to trigger determinization of the BRICS automaton. This determinization is now performed automatically if necessary. Instead, the boolean flag now triggers a totalization of the transition function, which allows to properly use `BricsDFA`s in structural equivalence tests (as BRICS automata do not allow to limit their input alphabet to certain characters).
* Adding new symbols to automata (via the `SupportsGrowingAlphabet` interface) now requires the automaton to be initialized with a `GrowingAlphabet` instance. This is to make sure that the user has full control over which alphabet instance should be used instead of AutomataLib making decisions on behalf of the user.

### Removed

* The mutable `Pair` class has been removed. The previously immutable pair class `IPair` has been renamed to `Pair`.
* Some utility classes and functions (especially from the `automata-common-util` package) have been removed without replacement, since equivalent functionality is provided by the Google Guava library we depend on. If you used any of this code, switch to the Guava equivalent.
* `RichArray` has been removed. At least regarding storage, `ArrayStorage` can be used as a replacement.
* `SymbolHidingAlphabet` has been removed without replacement. Changes to the logic of adding new input symbols should no longer make this class necessary.

### Fixed

* Serializers now use platform dependent newlines. This shouldn't affect deserialization code.
* Several bugs detected either by our ongoing efforts to write tests.


## [0.7.1](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.7.1) - 2018-05-11

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.7.0...automatalib-0.7.1)

### Added

* Added `SymbolHidingAlphabet`, which is required for a bugfix release of LearnLib.

### Fixed

* Fixed an issue where initial states were not copied in copy-constructor of NFAs.
* Fixed an NPE when traversing partially defined NFAs.
* Fixed an issue, where input alphabets were unnecessarily wrapped in `SimpleAlphabet`s in compact automata implementations.
* Fixed an out-of-bounds error in incremental automata builders.
* Fixed an error in Paige/Tarjan algorithm for partially defined automata.
* Fixed wrong computation of strongly connected components (see #21).
* General consolidations (typos, wrong documentation, etc.).


## [0.7.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.7.0) - 2018-02-07

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.6.0...automatalib-0.7.0)

### Added

* Added algorithms for computing adaptive distinguishing sequences for Mealy machines.
* Added support for adding alphabet symbols after initial automaton construction.
* Added support for Visibly Push-Down Automata (VPDA) in form of 1-SEVPAs.
* Added (de-)serializers for the AUT format (see [#14](https://github.com/LearnLib/automatalib/issues/14)).
* Added lazy (iterator based) methods for computing state- and transition covers, W-Method and Wp-Method tests.
* Added a serializer for the SAF format.

### Changed

* Refactored the Maven artifact and Java package structure. Have a look at the [List of AutomataLib Artifacts](https://github.com/LearnLib/automatalib/wiki/List-of-AutomataLib-Artifacts) for an updated overview of available artifacts. In general, no functionality should have been removed (except of code marked with `@Deprecated`). The easiest way to migrate your code to the new version is probably by using the Auto-Import feature of your IDE of choice.

  The non-trivial refactorings include:
  * API methods no longer use wildcards in generic return parameters. This allows your code to not having to deal with them.
  * The visualization facade has been generified. Any `getGraphDOTHelper()` related code has been renamed to the more general `getVisualizationHelper()` terminology. The `Visualization` class now works by providing either the `automata-dot-visualizer` or `automata-jung-visualizer` JAR at runtime. This allows for potentially more visualizers in the future.

* Replaced `System.out` logging, with calls to an SLF4j facade.
* Code improvements due to employment of several static code-analysis plugins (findbugs, checkstyle, PMD, etc.) as well as setting up continuous integration at [Travis CI](https://travis-ci.org/LearnLib/automatalib).

### Removed

* Several classes and methods annotated with `@Deprecated`.

### Fixed

* Several bugs detected either by our newly employed static code-analysis toolchain or by our ongoing efforts to write tests.


## [0.6.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.6.0) - 2015-06-03

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.5.2...automatalib-0.6.0)


## [0.5.2](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.5.2) - 2015-04-26

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.5.1...automatalib-0.5.2)


## [0.5.1](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.5.1) - 2015-01-15

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.5.0...automatalib-0.5.1)


## [0.5.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.5.0) - 2015-01-13

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.4.1...automatalib-0.5.0)


## [0.4.1](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.4.1) - 2014-06-06

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.4.0...automatalib-0.4.1)


## [0.4.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.4.0) - 2014-04-11

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.3.1...automatalib-0.4.0)


## [0.3.1-ase2013-tutorial-r1](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.3.1-ase2013-tutorial-r1) - 2013-12-13


## [0.3.1](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.3.1) - 2013-11-06

[Full changelog](https://github.com/LearnLib/automatalib/compare/automatalib-0.3.0...automatalib-0.3.1)


## [0.3.1-ase2013-tutorial](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.3.1-ase2013-tutorial) - 2013-11-06


## [0.3.0](https://github.com/LearnLib/automatalib/releases/tag/automatalib-0.3.0) - 2013-06-14

[Full changelog](https://github.com/LearnLib/automatalib/compare/942077e113e2dd879a0a92b3cad91b14e67b1311...automatalib-0.3.0)
