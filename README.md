---
title: Live Pandoc (bis)

...

# Live Pandoc (bis)

A simple `node-js` script (from ClojureScript) to run pandoc whenever the input
file is saved.

# Why?

The project [pandoc-live project](https://github.com/ocharles/pandoc-live)
already exists, but I never managed to run it, so I decided to try to write one
myself with my new `ClojureScript` superpower.

# Requirement

You should have a `node-js` server installed and under your path. Tested
against version `12.13.0`.

# How To

Download the `config.edn` and `target/pandoc-watcher.js` file under the same
folder.


Configurations of pandoc parameters can be edited in the `config.edn` file. The
file describe a map of pandoc options pairs. The pandoc argument starts with
`:` (these are Clojure keys). Comments can be added with `;`. Example of a
`config.edn`.

``` clojure
;; :input is the input file
 {:input "README.md"
 :number-sections true
 ;; :s will be translated to -s
 :s true
 ;; :toc is translated to --toc
 :toc true
 :katex true
 :template "minidoc-template.html" ;; comment the line if you target "*.pdf"
 :highlight "pygments"
 :o "README.html"}
```

## Start the server

Launch the nodejs server

``` shell
node pandoc-watcher.js
```

Edit your input file and the server runs the pandoc command according the
`config.edn`.

# Note

If your configuration is quite convoluted, you can test and watch the print
output in your terminal.

# How to Develop

If you want to extend or debug the script, the code is written in ClojureScript
using `node-watch` as dependency. First download
[Clojure](https://clojure.org/guides/getting_started) and install it. Then

``` shell
npm install --save # download the javascript dependency
shadow-cljs start # avoid to restart the server all the time,
# make sure is on your paths
shadow-cljs watch app # develop

# In a different terminal
node target/pandoc-watcher.js

# You can use a repl from that point on, you might want to rewrite
# the reload function.

# relase
shadow-cljs release app
```

# TODO

It would be nice to add a websocket support for live reloading `html` output.
