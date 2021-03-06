(ns pandoc.live.websockets)

(defonce ws-chan (atom nil))

(defn receive-msg! [update-fn]
  (fn [msg]
    (update-fn (->> msg .-data))))

(defn send-msg!
  [msg]
  (if @ws-chan
    (.send @ws-chan msg)
    (throw (js/Error. "Websocket is not available!"))))

(defn make-websocket! [url receive-handler]
  (println "attempting to connect websocket")
  (if-let [chan (js/WebSocket. url)]
    (do
      (set! (.-onmessage chan) (receive-msg! receive-handler))
      (reset! ws-chan chan)
      (println "Websocket conncetion established with: " url))
    (throw (js/Error. "Websocket connection failed!"))))

(comment
  (.send @ws-chan "Hello"))
