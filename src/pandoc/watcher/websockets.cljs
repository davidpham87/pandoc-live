(ns pandoc.watcher.websockets
  (:require
   ["ws" :as web-socket]
   [mount.core :refer (defstate)]))

(def config {:web-conn {:uri "ws://localhost:3000"}})

(defn ws-status [ws]
  {:url (.-url ws) :ready-state (.-readyState ws)})

(defn connect [uri]
  (let [ws (web-socket. uri)]
    (set! (.-onopen ws) #(.log js/console "Hello"))
    (set! (.-onclose ws) #(.log js/console "Stop"))
    ws))

#_(defstate web-conn :start (connect (get-in config [:web-conn :uri])))
