# Changelog

All notable changes to this project will be documented in this file.

## [1.4.0]

Added support for various printing options

### Added

* `toString(boolean)` - for choosing compact/pretty-print format with argument
* `compactPrint` - for compact printouts

### Changed

n/a

## [1.3.0]

Added support for operating system info and memory pools.

### Added

* `withGarbageCollectionInfo` - optional garbage collection information.

### Changed

Changed `allInfo()`

* added `withGarbageCollectionInfo`

## [1.2.0]

Added support for operating system info and memory pools.

### Added

* `withOperatingSystemInfo` - optional operating system information.
* `withMemoryInfo(boolean)` - optional memory pool information included in the _memory_ section

### Changed

Changed `allInfo()`

* added `withOperatingSystemInfo`
* changed to use `withMemoryInfo(true)`
* changed to use `withThreadInfo(3)`

## [1.1.1]

Fixed bug with missing file name and line number in the stack trace.

### Added

### Changed

Included the method name in the stack trace.   
Format is now _{class}.{method}({filename}:{line})_

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