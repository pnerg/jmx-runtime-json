# Changelog

All notable changes to this project will be documented in this file.

## [1.1.0]

Added support for optional stack trace.

### Added

Added

* `withThreadInfo(int)`
* `allInfo(int)`

### Changed

The thread information now always contains a `stack-trace` array, might be zero lenght if no stack trace was requested
or is available for that thread.

## [1.0.0]

First release.

### Added

Support for:

- class-loading - `withClassLoadingInfo`
- memory - `withMemoryInfo`
- runtime - `withRuntimeInfo`
- thread - `withThreadInfo`

### Changed