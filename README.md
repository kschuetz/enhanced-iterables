# enhanced-iterables

[![enhanced-iterables](https://img.shields.io/maven-central/v/dev.marksman/enhanced-iterables.svg)](http://search.maven.org/#search%7Cga%7C1%7Cdev.marksman.enhanced-iterables)
[![Javadoc](https://javadoc-badge.appspot.com/dev.marksman/enhanced-iterables.svg?label=javadoc)](https://kschuetz.github.io/enhanced-iterables/javadoc/)
[![CircleCI](https://circleci.com/gh/kschuetz/enhanced-iterables.svg?style=svg)](https://circleci.com/gh/kschuetz/enhanced-iterables)
[![Maintainability](https://api.codeclimate.com/v1/badges/02d956357e0a4eb21d20/maintainability)](https://codeclimate.com/github/kschuetz/enhanced-iterables/maintainability)

#### Table of Contents

 - [What is it?](#what-is-it)
 - [Types](#types)
   - [`EnhancedIterable<A>`](#enhanced-iterable)
   - [`FiniteIterable<A>`](#finite-iterable)
   - [`NonEmptyIterable<A>`](#non-empty-iterable)
   - [`NonEmptyFiniteIterable<A>`](#non-empty-finite-iterable)
   - [`ImmutableIterable<A>`](#immutable-iterable)
   - [`ImmutableFiniteIterable<A>`](#immutable-finite-iterable)
   - [`ImmutableNonEmptyIterable<A>`](#immutable-non-empty-iterable)
   - [`ImmutableNonEmptyFiniteIterable<A>`](#immutable-non-empty-finite-iterable)
 - [License](#license)   
       
# What is it?

*enhanced-iterables* is a Java library that is intended to be used in conjunction with [lambda](https://github.com/palatable/lambda). 

It provides interfaces that add some useful methods to `Iterable`s.  Most of these methods delegate to lambda functions, and return the most specific type of `Iterable` possible.    

For more details, check out the [javadoc](https://kschuetz.github.io/enhanced-iterables/javadoc/).

# [lambda](https://github.com/palatable/lambda) version compatibility

| enhanced-iterables | lambda 5.0.0 | lambda 4.0.0 |
|---|---|---|
|1.0.x|:heavy_check_mark:|:heavy_check_mark:|

# Types

## <a name="enhanced-iterable">`EnhancedIterable<A>`</a>

The base functionality that can be added to any `Iterable`.  Can be infinite, finite, or empty. 

### Methods

| Method | Returns | Lambda function |
|---|---|---|
| `append` | `NonEmptyIterable<A>`| [`Snoc.snoc`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Snoc.html) |
| `concat` | `EnhancedIterable<A>`| [`Concat.concat`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/monoid/builtin/Concat.html) |
| `drop` | `EnhancedIterable<A>`| [`Drop.drop`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Drop.html) |
| `dropWhile` | `EnhancedIterable<A>`| [`DropWhile.dropWhile`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/DropWhile.html) |
| `filter` | `EnhancedIterable<A>`| [`Filter.filter`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Filter.html) |
| `find` | `Maybe<A>`| [`Find.find`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Find.html) |
| `fmap` | `EnhancedIterable<B>`| [`Map.map`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Map.html) |
| `intersperse` | `EnhancedIterable<B>`| [`Intersperse.intersperse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Intersperse.html) |
| `isEmpty` | `boolean`| -- |
| `magnetizeBy` | `EnhancedIterable<NonEmptyIterable<A>> `| [`MagnetizeBy.magnetizeBy`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/MagnetizeBy.html) |
| `partition` | `Tuple2<EnhancedIterable<B>, EnhancedIterable<C>>`| [`Partition.partition`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Partition.html) |
| `prepend` | `NonEmptyIterable<A>`| [`Cons.cons`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Cons.html) |
| `prependAll` | `EnhancedIterable<A>`| [`PrependAll.prependAll`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/PrependAll.html) |
| `slide` | `EnhancedIterable<NonEmptyFiniteIterable<A>>`| [`Slide.slide`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Slide.html) |
| `span` | `Tuple2<EnhancedIterable<A>, EnhancedIterable<A>>`| [`Span.span`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Span.html) |
| `take` | `FiniteIterable<A>`| [`Take.take`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Take.html) |
| `takeWhile` | `EnhancedIterable<A>`| [`TakeWhile.takeWhile`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/TakeWhile.html) |
| `toArray` | `A[]`| [`ToArray.toArray`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/ToArray.html) |
| `toCollection` | `C extends Collection<A>`| [`ToCollection.toCollection`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/ToCollection.html) |
| `zipWith` | `EnhancedIterable<C>`| [`ZipWith.zipWith`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn3/ZipWith.html) |

### Constructing

Any existing `Iterable<A>` can be converted to an `EnhancedIterable<A>` by calling the `EnhancedIterable.enhance` static method.

## <a name="finite-iterable">`FiniteIterable<A>`</a>

An `EnhancedIterable` that is known at compile-time to be finite.

### Methods

In addition to all methods on `EnhancedIterable<A>`, provides the following:

| Method | Returns | Lambda function |
|---|---|---|
| `append` | `NonEmptyFiniteIterable<A>`| [`Snoc.snoc`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Snoc.html) |
| `concat` | `FiniteIterable<A>`| [`Concat.concat`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/monoid/builtin/Concat.html) |
| `cross` | `FiniteIterable<Tuple2<A, B>>>`| [`CartestianProduct.cartesianProduct`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/CartesianProduct.html) |
| `drop` | `FiniteIterable<A>`| [`Drop.drop`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Drop.html) |
| `dropWhile` | `FiniteIterable<A>`| [`DropWhile.dropWhile`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/DropWhile.html) |
| `filter` | `FiniteIterable<A>`| [`Filter.filter`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Filter.html) |
| `fmap` | `FiniteIterable<B>`| [`Map.map`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Map.html) |
| `foldLeft` | `B` | [`FoldLeft.foldLeft`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn3/FoldLeft.html) |
| `inits` | `NonEmptyIterable<FiniteIterable<A>>`| [`Inits.inits`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Inits.html) |
| `intersperse` | `FiniteIterable<A>`| [`Intersperse.intersperse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Intersperse.html) |
| `magnetizeBy` | `FiniteIterable<NonEmptyFiniteIterable<A>> `| [`MagnetizeBy.magnetizeBy`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/MagnetizeBy.html) |
| `partition` | `Tuple2<FiniteIterable<B>, FiniteIterable<C>>`| [`Partition.partition`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Partition.html) |
| `prepend` | `NonEmptyFiniteIterable<A>`| [`Cons.cons`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Cons.html) |
| `prependAll` | `FiniteIterable<A>`| [`PrependAll.prependAll`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/PrependAll.html) |
| `reverse` | `FiniteIterable<A>`| [`Reverse.reverse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Reverse.html) |
| `slide` | `FiniteIterable<NonEmptyFiniteIterable<A>>`| [`Slide.slide`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Slide.html) |
| `span` | `Tuple2<FiniteIterable<A>, FiniteIterable<A>>`| [`Span.span`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Span.html) |
| `tails` | `NonEmptyIterable<FiniteIterable<A>>`| [`Tails.tails`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Tails.html) |
| `takeWhile` | `FiniteIterable<A>`| [`TakeWhile.takeWhile`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/TakeWhile.html) |
| `zipWith` | `FiniteIterable<C>`| [`ZipWith.zipWith`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn3/ZipWith.html) |

### Constructing

- Any existing `Collection<A>` can be converted to a `FiniteIterable<A>` by calling the `FiniteIterable.finiteIterable` static method.
- An `Iterable<A>` can converted to a `FiniteIterable<A>` by calling `FiniteIterable.finiteIterable` and providing a maximum size.

## <a name="non-empty-iterable">`NonEmptyIterable<A>`</a>

An `EnhancedIterable` that is known at compile-time to be non-empty.  May be finite or infinite.

### Methods

In addition to all methods on `EnhancedIterable<A>`, provides the following:

| Method | Returns | Lambda function |
|---|---|---|
| `concat` | `NonEmptyIterable<A>`| [`Concat.concat`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/monoid/builtin/Concat.html) |
| `fmap` | `NonEmptyIterable<B>`| [`Map.map`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Map.html) |
| `head` | `A`| [`Head.head`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Head.html)|
| `intersperse` | `NonEmptyIterable<A>`| [`Intersperse.intersperse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Intersperse.html) |
| `magnetizeBy` | `NonEmptyIterable<NonEmptyIterable<A>> `| [`MagnetizeBy.magnetizeBy`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/MagnetizeBy.html) |
| `prependAll` | `NonEmptyIterable<A>`| [`PrependAll.prependAll`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/PrependAll.html) |
| `tail` | `EnhancedIterable<A>`| [`Tail.tail`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Tail.html) |
| `zipWith` | `NonEmptyIterable<C>`| [`ZipWith.zipWith`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn3/ZipWith.html) |

### Constructing

An `Iterable<A>` can converted to a `NonEmptyIterable<A>` by calling `NonEmptyIterable.nonEmptyIterable` and providing an additional element for the head.

## <a name="non-empty-finite-iterable">`NonEmptyFiniteIterable<A>`</a>

An `EnhancedIterable` that is known at compile-time to be non-empty and finite.

### Methods

In addition to all methods on `FiniteIterable<A>` and `NonEmptyIterable<A>`, provides the following:

| Method | Returns | Lambda function |
|---|---|---|
| `concat` | `NonEmptyFiniteIterable<A>`| [`Concat.concat`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/monoid/builtin/Concat.html) |
| `cross` | `NonEmptyFiniteIterable<Tuple2<A, B>>>`| [`CartestianProduct.cartesianProduct`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/CartesianProduct.html) |
| `fmap` | `NonEmptyFiniteIterable<B>`| [`Map.map`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Map.html) |
| `init` | `FiniteIterable<A>`| [`Init.init`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Init.html)|
| `intersperse` | `NonEmptyFiniteIterable<A>`| [`Intersperse.intersperse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Intersperse.html) |
| `last` | `A`| [`Last.last`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Last.html) |
| `magnetizeBy` | `NonEmptyFiniteIterable<NonEmptyFiniteIterable<A>> `| [`MagnetizeBy.magnetizeBy`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/MagnetizeBy.html) |
| `prependAll` | `NonEmptyFiniteIterable<A>`| [`PrependAll.prependAll`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/PrependAll.html) |
| `reverse` | `NonEmptyFiniteIterable<A>`| [`Reverse.reverse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Reverse.html) |
| `tail` | `FiniteIterable<A>`| [`Tail.tail`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Tail.html) |
| `zipWith` | `NonEmptyFiniteIterable<C>`| [`ZipWith.zipWith`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn3/ZipWith.html) |

### Constructing

A `FiniteIterable<A>` can converted to a `NonEmptyFiniteIterable<A>` by calling `NonEmptyFiniteIterable.nonEmptyFiniteIterable` and providing an additional element for the head.

## <a name="immutable-iterable">`ImmutableIterable<A>`</a>

An `EnhancedIterable` that is known at compile-time to be safe from mutation.  May be empty, finite, or infinite.

### Methods

In addition to all methods on `EnhancedIterable<A>`, provides the following:

| Method | Returns | Lambda function |
|---|---|---|
| `append` | `ImmutableNonEmptyIterable<A>`| [`Snoc.snoc`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Snoc.html) |
| `concat` | `ImmutableIterable<A>`| [`Concat.concat`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/monoid/builtin/Concat.html) |
| `drop` | `ImmutableIterable<A>`| [`Drop.drop`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Drop.html) |
| `dropWhile` | `ImmutableIterable<A>`| [`DropWhile.dropWhile`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/DropWhile.html) |
| `filter` | `ImmutableIterable<A>`| [`Filter.filter`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Filter.html) |
| `fmap` | `ImmutableIterable<B>`| [`Map.map`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Map.html) |
| `intersperse` | `ImmutableIterable<B>`| [`Intersperse.intersperse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Intersperse.html) |
| `magnetizeBy` | `ImmutableIterable<ImmutableNonEmptyIterable<A>> `| [`MagnetizeBy.magnetizeBy`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/MagnetizeBy.html) |
| `partition` | `Tuple2<ImmutableIterable<B>, ImmutableIterable<C>>`| [`Partition.partition`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Partition.html) |
| `prepend` | `ImmutableNonEmptyIterable<A>`| [`Cons.cons`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Cons.html) |
| `prependAll` | `ImmutableIterable<A>`| [`PrependAll.prependAll`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/PrependAll.html) |
| `slide` | `ImmutableIterable<ImmutableNonEmptyFiniteIterable<A>>`| [`Slide.slide`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Slide.html) |
| `span` | `Tuple2<ImmutableIterable<A>, ImmutableIterable<A>>`| [`Span.span`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Span.html) |
| `take` | `ImmutableFiniteIterable<A>`| [`Take.take`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Take.html) |
| `takeWhile` | `ImmutableIterable<A>`| [`TakeWhile.takeWhile`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/TakeWhile.html) |
| `zipWith` | `ImmutableIterable<C>`| [`ZipWith.zipWith`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn3/ZipWith.html) |

### Constructing
- `ImmutableIterable.of` static method
- `ImmutableIterable.copyFrom` static method constructs `ImmutableFiniteIterable`s by copying from existing finite iterables (such as `Collection`s) 
- The [*collection-views*](https://github.com/kschuetz/collection-views) library provides some implementations of `ImmutableIterable` (e.g. `ImmutableVector`).

## <a name="immutable-finite-iterable">`ImmutableFiniteIterable<A>`</a>

An `EnhancedIterable` that is known at compile-time to be safe from mutation and finite.  May be empty.

### Methods

In addition to all methods on `ImmutableIterable<A>` and `FiniteIterable<A>`, provides the following:

| Method | Returns | Lambda function |
|---|---|---|
| `append` | `ImmutableNonEmptyFiniteIterable<A>`| [`Snoc.snoc`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Snoc.html) |
| `concat` | `ImmutableFiniteIterable<A>`| [`Concat.concat`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/monoid/builtin/Concat.html) |
| `cross` | `ImmutableFiniteIterable<Tuple2<A, B>>>`| [`CartestianProduct.cartesianProduct`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/CartesianProduct.html) |
| `drop` | `ImmutableFiniteIterable<A>`| [`Drop.drop`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Drop.html) |
| `dropWhile` | `ImmutableFiniteIterable<A>`| [`DropWhile.dropWhile`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/DropWhile.html) |
| `filter` | `ImmutableFiniteIterable<A>`| [`Filter.filter`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Filter.html) |
| `fmap` | `ImmutableFiniteIterable<B>`| [`Map.map`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Map.html) |
| `intersperse` | `ImmutableFiniteIterable<B>`| [`Intersperse.intersperse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Intersperse.html) |
| `magnetizeBy` | `ImmutableFiniteIterable<ImmutableNonEmptyFiniteIterable<A>> `| [`MagnetizeBy.magnetizeBy`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/MagnetizeBy.html) |
| `partition` | `Tuple2<ImmutableFiniteIterable<B>, ImmutableFiniteIterable<C>>`| [`Partition.partition`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Partition.html) |
| `prepend` | `ImmutableNonEmptyFiniteIterable<A>`| [`Cons.cons`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Cons.html) |
| `prependAll` | `ImmutableFiniteIterable<A>`| [`PrependAll.prependAll`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/PrependAll.html) |
| `reverse` | `ImmutableFiniteIterable<A>`| [`Reverse.reverse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Reverse.html) |
| `slide` | `ImmutableFiniteIterable<ImmutableNonEmptyFiniteIterable<A>>`| [`Slide.slide`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Slide.html) |
| `span` | `Tuple2<ImmutableFiniteIterable<A>, ImmutableFiniteIterable<A>>`| [`Span.span`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Span.html) |
| `tails` | `NonEmptyIterable<ImmutableFiniteIterable<A>>`| [`Tails.tails`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Tails.html) |
| `takeWhile` | `ImmutableFiniteIterable<A>`| [`TakeWhile.takeWhile`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/TakeWhile.html) |
| `zipWith` | `ImmutableFiniteIterable<C>`| [`ZipWith.zipWith`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn3/ZipWith.html) |

### Constructing
- `ImmutableFiniteIterable.of` static method
- `ImmutableIterable.copyFrom` static method constructs `ImmutableFiniteIterable`s by copying from existing finite iterables (such as `Collection`s) 
- The [*collection-views*](https://github.com/kschuetz/collection-views) library provides some implementations of `ImmutableFiniteIterable` (e.g. `ImmutableVector`).

## <a name="immutable-non-empty-iterable">`ImmutableNonEmptyIterable<A>`</a>

An `EnhancedIterable` that is known at compile-time to be safe from mutation and non-empty.  May be finite or infinite.

### Methods

In addition to all methods on `ImmutableIterable<A>` and `NonEmptyIterable<A>`, provides the following:

| Method | Returns | Lambda function |
|---|---|---|
| `concat` | `ImmutableNonEmptyIterable<A>`| [`Concat.concat`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/monoid/builtin/Concat.html) |
| `fmap` | `ImmutableNonEmptyIterable<B>`| [`Map.map`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Map.html) |
| `intersperse` | `ImmutableNonEmptyIterable<A>`| [`Intersperse.intersperse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Intersperse.html) |
| `magnetizeBy` | `ImmutableNonEmptyIterable<ImmutableNonEmptyIterable<A>> `| [`MagnetizeBy.magnetizeBy`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/MagnetizeBy.html) |
| `prependAll` | `ImmutableNonEmptyIterable<A>`| [`PrependAll.prependAll`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/PrependAll.html) |
| `tail` | `ImmutableIterable<A>`| [`Tail.tail`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Tail.html) |
| `zipWith` | `ImmutableNonEmptyIterable<C>`| [`ZipWith.zipWith`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn3/ZipWith.html) |

### Constructing
- `ImmutableNonEmptyIterable.of` static method
- An `ImmutableIterable<A>` can converted to a `ImmutableNonEmptyIterable<A>` by calling `ImmutableNonEmptyIterable.immutableNonEmptyIterable` and providing an additional element for the head.
- The [*collection-views*](https://github.com/kschuetz/collection-views) library provides some implementations of `ImmutableNonEmptyIterable` (e.g. `ImmutableNonEmptyVector`).

## <a name="immutable-non-empty-finite-iterable">`ImmutableNonEmptyFiniteIterable<A>`</a>

An `EnhancedIterable` that is known at compile-time to be safe from mutation, non-empty, and finite.

### Methods

In addition to all methods on `ImmutableFiniteIterable<A>` and `ImmutableNonEmptyIterable<A>`, provides the following:

| Method | Returns | Lambda function |
|---|---|---|
| `concat` | `ImmutableNonEmptyFiniteIterable<A>`| [`Concat.concat`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/monoid/builtin/Concat.html) |
| `cross` | `ImmutableNonEmptyFiniteIterable<Tuple2<A, B>>>`| [`CartestianProduct.cartesianProduct`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/CartesianProduct.html) |
| `fmap` | `ImmutableNonEmptyFiniteIterable<B>`| [`Map.map`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Map.html) |
| `init` | `ImmutableFiniteIterable<A>`| [`Init.init`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Init.html)|
| `intersperse` | `ImmutableNonEmptyFiniteIterable<B>`| [`Intersperse.intersperse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/Intersperse.html) |
| `magnetizeBy` | `ImmutableFiniteIterable<ImmutableNonEmptyFiniteIterable<A>> `| [`MagnetizeBy.magnetizeBy`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/MagnetizeBy.html) |
| `prependAll` | `ImmutableNonEmptyFiniteIterable<A>`| [`PrependAll.prependAll`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn2/PrependAll.html) |
| `reverse` | `ImmutableNonEmptyFiniteIterable<A>`| [`Reverse.reverse`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn1/Reverse.html) |
| `zipWith` | `ImmutableNonEmptyFiniteIterable<C>`| [`ZipWith.zipWith`](https://palatable.github.io/lambda/javadoc/com/jnape/palatable/lambda/functions/builtin/fn3/ZipWith.html) |

### Constructing
- `ImmutableNonEmptyFiniteIterable.of` static method
- An `ImmutableFiniteIterable<A>` can converted to a `ImmutableNonEmptyFiniteIterable<A>` by calling `ImmutableNonEmptyFiniteIterable.immutableNonEmptyFiniteIterable` and providing an additional element for the head.
- The [*collection-views*](https://github.com/kschuetz/collection-views) library provides some implementations of `ImmutableNonEmptyFiniteIterable` (e.g. `ImmutableNonEmptyVector`).

# <a name="license">License</a>

[![FOSSA Status](https://app.fossa.com/api/projects/git%2Bgithub.com%2Fkschuetz%2Fenhanced-iterables.svg?type=shield)](https://app.fossa.com/projects/git%2Bgithub.com%2Fkschuetz%2Fenhanced-iterables?ref=badge_shield)

*enhanced-iterables* is distributed under [The MIT License](http://choosealicense.com/licenses/mit/).

The MIT License (MIT)

Copyright © 2019 Kevin Schuetz

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
