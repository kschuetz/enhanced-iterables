# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## [Unreleased]
No changes yet

## [1.1.0] - 2019-10-25
### Changed
- Upgrade lambda to 5.1.0
- Singletons now require less memory

### Added
- `repeat` constructors
- `cycle` method for `FiniteIterable`s
- `distinct` method for `FiniteIterable`s
- `foldRight` method for `FiniteIterable`s
- `reduceLeft` method for `NonEmptyFiniteIterable`s
- `reduceRight` method for `NonEmptyFiniteIterable`s
- `size` method for `FiniteIterable`s

## [1.0.4] - 2019-10-01
###
- Upgrade lambda to 5.0.0

## [1.0.3] - 2019-06-30
### Added
- Constructors for empty `EnhancedIterable`s

## [1.0.2] - 2019-06-26
### Changed
- `inits` method now returns an `ImmutableNonEmptyFiniteIterable`

### Added
- `last` method

## [1.0.1] - 2019-06-25
### Changed
- Reduce overhead of constructing `NonEmptyIterable`s

### Added
- `magnetizeBy` method

## [1.0.0] - 2019-06-02
### Added
- Initial release

[Unreleased]: https://github.com/kschuetz/enhanced-iterables/compare/enhanced-iterables-1.1.0...HEAD
[1.1.0]: https://github.com/kschuetz/enhanced-iterables/compare/enhanced-iterables-1.0.4...enhanced-iterables-1.1.0
[1.0.4]: https://github.com/kschuetz/enhanced-iterables/compare/enhanced-iterables-1.0.3...enhanced-iterables-1.0.4
[1.0.3]: https://github.com/kschuetz/enhanced-iterables/compare/enhanced-iterables-1.0.2...enhanced-iterables-1.0.3
[1.0.2]: https://github.com/kschuetz/enhanced-iterables/compare/enhanced-iterables-1.0.1...enhanced-iterables-1.0.2
[1.0.1]: https://github.com/kschuetz/enhanced-iterables/compare/enhanced-iterables-1.0.0...enhanced-iterables-1.0.1
[1.0.0]: https://github.com/kschuetz/enhanced-iterables/commits/enhanced-iterables-1.0.0
