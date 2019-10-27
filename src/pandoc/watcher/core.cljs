(ns pandoc.watcher.core
  (:require
   ["child_process" :as child-process]
   ["node-watch" :default watch]
   [clojure.reader :refer (read-string)]
   [clojure.string :as str]
   [cljs-node-io.core :as io :refer [slurp]]))

(defonce config (atom nil))

(defn read-config []
  (try
    (read-string (slurp "config.edn"))
    (catch js/Error e (println e))))

(defn watch-file! [filename on-change]
  (watch
   filename
   (fn [e f]
     (println "Detecting " e " on: " f ", while watching " filename)
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
        (child-process/exec
         cmd
         (fn [e std-out std-err]
           (when e
             (println "Error: " e))
           (when (seq std-out)
             (println "Std-out: " std-out))
           (when (seq std-err)
             (println "Std-err: " std-err))))
        [:success]))))

(defn main []
  (when-not @config
    (println "Initializing config.")
    (reset! config (read-config)))
  (when @config
    (println "Creating watchers.")
    (watch-file! "config.edn" (fn [_] (reset! config (read-config))))
    (watch-file! (clj->js [(:input @config) "config.edn"])
                 (fn [_] (pandoc-compile! @config)))))

(defn reload []
  (println "reload this"))
