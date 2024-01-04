# anyver

[![Build Status](https://img.shields.io/github/actions/workflow/status/scarletcomply/anyver/ci.yml?branch=main)](https://github.com/scarletcomply/anyver/actions)
[![cljdoc](https://cljdoc.org/badge/cc.scarlet/anyver)][cljdoc]
[![Clojars](https://img.shields.io/clojars/v/cc.scarlet/anyver.svg)][clojars]

Parse and sort versions intuitively.

## Overview

Anyver is a small library for Clojure(Script) that allows to sort version strings,
without assuming a particular versioning scheme such as semantic versioning.

Versions are parsed by extracting consecutive digits and consecutive letters,
ignoring punctuation and whitespace.  Digits are then parsed into integers, to
sort them as number and not as string. If the first component is `"v"`,
it is dropped.

Examples:

- `1.12.3 => [1 12 3]`
- `2.0-alpha3 => [2 0 "alpha" 3]`
- `v1.0 => [1 0]`

Versions are compared lexicographically, with the following rules:

- shorter versions are right-padded with `nil`
- integers compare larger than strings or `nil`
- `nil` compares larger than strings
- same types are compared using `compare`

Therefore:

- `1.2.4` is greater than `1.2.3` (`4 > 3`)
- `1.10.0.0` is greater than `1.9.9.9` (`10 > 9`)
- `0.9-rc3` is greater than `0.9-rc2` (`3 > 2`)
- `1.0` is greater than `1` (`0 > nil`)
- `1.0.0` is greater than `1.0.0-alpha2` (`nil > "alpha"`)
- `0.9-beta` is greater than `0.9-alpha` (`"beta" > "alpha"`)

## Installation

Releases are available from [Clojars][clojars].

deps.edn:

```clojure
cc.scarlet/anyver {:mvn/version "0.3.0"}
```

Leiningen/Boot:

```clojure
[cc.scarlet/anyver "0.3.0"]
```

## License

Distributed under the [MIT License].  
Copyright (c) 2023-2024 [Scarlet Global Holdings Ltd][scarlet].

[MIT License]: ./LICENSE
[scarlet]: https://scarlet.cc

[cljdoc]: https://cljdoc.org/jump/release/cc.scarlet/anyver
[clojars]: https://clojars.org/cc.scarlet/anyver
