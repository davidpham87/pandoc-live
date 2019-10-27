(ns pandoc.watcher.core
  (:require
   ["fs" :as fs]
   ["child_process" :as child-process]
   [clojure.reader :refer (read-string)]
   [clojure.string :as str]
   [cljs-node-io.core :as io :refer [slurp]]))


(defonce config (atom nil))

(defn read-config []
  (try
    (read-string (slurp "config.edn"))
    (catch js/Error e (println e))))

(defn watch-file! [filename on-change]
  (fs/watch
   filename
   (fn [e f]
     (println "Detecting change on: " f )
     (println "Event: " e)
     (println "filename:" filename)
     (try
       (on-change @config)
       (catch js/Error e (println e))))))

(defn pandoc-compile! [config]
  (let [opts (for [[k v] (dissoc config :input)
                   :when v]
               (str "-"  (when (> (count (name k)) 1) "-") (name k) " "
                    (when-not (or (true? v) (empty? v)) v)))]
    (when (:input config)
      (let [cmd (str "pandoc " (:input config) " " (str/join " " opts))]
        (println cmd)
        (child-process/exec cmd)
        [:success]))))

(defn main []
  (reset! config (read-config))
  (watch-file! "config.edn" (fn [_] (reset! config (read-config))))
  (watch-file! (:input @config) (fn [_] (pandoc-compile! @config))))

(defn reload []
  (println "reload this"))
