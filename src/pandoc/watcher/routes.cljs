(ns pandoc.watcher.routes
  (:require-macros [hiccups.core :refer (html)])
  (:require
   [macchiato.middleware.defaults :as defaults]
   [macchiato.util.response       :as mur]
   [reitit.ring                   :as ring]
   [hiccups.runtime]))


(defn home [_ res _]
  (-> (html
       [:h2 "Hello World!"]
       [:p "Your user-agent is"])
      (mur/ok)
      (mur/content-type "text/html")
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
   ["/test" {:get test-route}]])

(def router (ring/router routes))

(defn wrap-defaults [handler]
  (defaults/wrap-defaults handler defaults/site-defaults))

(def ring-handler (wrap-defaults (ring/ring-handler router not-found)))
