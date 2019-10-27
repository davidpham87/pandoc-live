# Live Pandoc (bis)

A simple nodejs script (from ClojureScript) to run pandoc whenever the input
file is saved.

# Requirement

You should have a `node-js` installed and under your path. Tested against
version `12.13.0`.

# How To

Download the `config.edn` and `target/pandoc-watcher.js` file under the same
folder.

Configurations of pandoc parameters can be edited in the `config.edn` file. The
file describe a map of pandoc options pairs. The pandoc argument starts with
`:` (these are Clojure keys). Comments can be added with `;`. Example of a
`config.edn`.

``` clojure
 ;; :input is the input file
{:input "example.md"
 ;; :s will be translated to -s
 :s true
 ;; :toc is translated to --toc
 :toc true
 :number-sections true
 :katex true
 ;; :template "minidoc-template.html"
 :o "example.pdf"}
```

## Start the server

Launch the nodejs server

``` shell
node pandoc-watcher.js
```

Edit your input file and the server runs the pandoc command according the
`config.edn`.
