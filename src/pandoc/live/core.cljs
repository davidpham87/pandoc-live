(ns pandoc.live.core
  "Front-End UI to read from the backend through websocket"
  (:require
   [reagent.core :as reagent]
   [pandoc.live.websockets :as ws]))

(def content (reagent/atom "<h1> Loading... </h1>"))

(defn pandoc-content [data]
  [:div {:dangerouslySetInnerHTML {:__html data}}])

(defn init! []
  (ws/make-websocket! (str "ws://localhost:3000") #(reset! content %)))

(defn app []
  [pandoc-content @content])

(defn mount-component []
  (reagent/render [app] (.getElementById js/document "app")))

(defn ^:dev/after-load main []
  (init!)
  (mount-component))
