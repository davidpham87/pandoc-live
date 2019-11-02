(ns pandoc.watcher.core
  (:require
   ["child_process"       :as child-process]
   ["node-watch"          :default watch]
   [cljs-node-io.core     :as io :refer [slurp]]
   [clojure.reader        :refer (read-string)]
   [clojure.string        :as str]
   [macchiato.server      :as http]
   [mount.core            :refer (defstate)]
   [pandoc.watcher.routes :refer (ring-handler)]))

(defonce config (atom nil))


;; Arguments for the live editing feature.
(def live-config-http {:host "localhost" :port 3000})

(def live-config {:output "public/index-pandoc.html" :highlight "pygments"
                  :s true :katex true})

;; Reloading functions

(defn read-config []
  (try (read-string (slurp "config.edn"))
       (catch js/Error e (println e))))

(defn watch-file! [filename on-change watchers]
  (let [watcher (watch
         filename
         (fn [e f]
           (println "Detecting " e " on: " f ", while watching " filename)
           (try (on-change @config)
                (catch js/Error e (println e)))))]
    (swap! watchers assoc filename watcher)))

(defn close-watchers [watchers]
  (doseq [w (vals watchers)]
    (.close w)))

(defonce watchers (atom {}))
(defstate watchers-manager
  :start (reset! watchers {})
  :stop (close-watchers @watchers))

;; compile pandoc according to config.edn
(defn ->pandoc-cli-args
  "Take an argument k and a value v and transform them to standard command line
  arguments. Single character having single prefixed dash (-), two otherwise. "
  [k v]
  (let [arg (name k)]
    (str (if (= (count arg) 1) "-" "--") arg " "
         (when-not (or (true? v) (empty? v)) v))))

(defn pandoc-compile! [config]
  (let [opts (for [[k v] (dissoc config :input) :when v] (->pandoc-cli-args k v))]
    (when (:input config)
      (let [cmd (str "pandoc " (:input config) " " (str/join " " opts))]
        (println cmd)
        (child-process/exec cmd
                            (fn [e std-out std-err]
                              (when e (println "Error: " e))
                              (when (seq std-out) (println "Std-out: " std-out))
                              (when (seq std-err) (println "Std-err: " std-err))))
        [:success]))))

(defn send-output [ws file]
  (let [data (try (slurp file) (catch js/Error e ""))]
    (.send ws data)))

;; fetch ui with live document and websockets

(def local-ws (atom nil))
(defn ws-handler [{:keys [websocket] :as ws-req}]
  (.on websocket "message"
       (fn [message]
         (.send websocket (str "got message: " message))))
  (reset! local-ws websocket)
  (send-output @local-ws (:output live-config)))

(defn server-start []
  (let [host   (:host live-config-http "127.0.0.1")
        port   (:port live-config-http 3000)
        server (http/start
                {:handler    ring-handler
                 :host       host
                 :port       port
                 :on-success #(.log js/console "websocket start on" host ":" port)})]
    (http/start-ws server ws-handler)
    server))

;; development utilities

(defn stop-server [server]
  (println server)
  (.close server))

(defstate server-manager
  :start (server-start)
  :stop (stop-server @server-manager))

(defn main []
  (mount.core/start)
  (when-not @config
    (println "Initializing config.")
    (reset! config (read-config)))
  (when @config
    (println "Creating watchers.")
    (watch-file! "config.edn" #(reset! config (read-config)) watchers)
    (watch-file! (clj->js [(:input @config) "config.edn"])
                 #(pandoc-compile! @config) watchers)
    (watch-file! (clj->js [(:input @config) "config.edn"])
                 #(pandoc-compile! (merge (dissoc @config :o :output)
                                          live-config))
                 watchers)
    (watch-file!
     (clj->js [(:output live-config) "config.edn"])
     (fn [_] (send-output @local-ws (:output live-config))) watchers))
  (when @local-ws
    (send-output @local-ws (:output live-config))))

(defn reload []
  (mount.core/stop)
  (main)
  (println "reload this"))
