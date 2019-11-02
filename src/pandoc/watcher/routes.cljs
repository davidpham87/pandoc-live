(ns pandoc.watcher.routes
  (:require-macros [hiccups.core :refer (html)])
  (:require
   [macchiato.middleware.defaults :as defaults]
   [macchiato.util.response       :as mur]
   [reitit.ring                   :as ring]
   [cljs-node-io.core             :as io :refer [slurp]]
   [hiccups.runtime]))

(defn home [_ res _]
  (-> (slurp "public/index.html")
      (mur/ok)
      (mur/content-type "text/html")
      (res)))

(defn get-main-js [_ res _]
  (-> (slurp "public/js/main.js")
      (mur/ok)
      (mur/content-type "application/json")
      (res)))

(defn test-route [req res _]
  (cljs.pprint/pprint req)
  (-> (html
       [:h2 "Test"]
       [:p "What?!"])
      (mur/ok)
      (mur/content-type "text/html")
      (res)))


(defn not-found [req res _]
  (-> (html
       [:html
        [:body
         [:h2 (:uri req) " was not found"]]])
      (mur/not-found)
      (mur/content-type "text/html")
      (res)))

(def routes
  [""
   ["/" {:get home}]
   ["/js/main.js" {:get get-main-js}]
   ["/test" {:get test-route}]])

(def router (ring/router routes))

(defn wrap-defaults [handler]
  (defaults/wrap-defaults handler defaults/site-defaults))

(def ring-handler (wrap-defaults (ring/ring-handler router not-found)))
