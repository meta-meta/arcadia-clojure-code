(ns controllers-state
  (:require [osc :as o]
            [app-state :as app]
            ovr-consts)
  (:use [arcadia.core]
        [arcadia.introspection]
        [arcadia.linear]
        [clojure.set :only [difference union]]
        [clojure.reflect]
        [clojure.pprint])
  (:import
    OVRInput
;    (SpaceNavigatorDriver Settings SpaceNavigator)
    (UnityEngine ForceMode Input KeyCode Quaternion Rigidbody Time Vector3)))






; TODO find me a better home
;(SpaceNavigator/SetRotationSensitivity 1)
;(SpaceNavigator/SetTranslationSensitivity 0.5)
;(set! Settings/RuntimeEditorNav false)

; Make sure "Filter Duplicates" is unchecked in OscIn component

(def s (let [blank-map (fn [n] (->> (range n)
                                    (map #(vector % 0))
                                    (flatten)
                                    (apply hash-map)))
             inst-initial {
                           :cc        (blank-map 128)
                           :notes     (blank-map 128)
                           :listeners #{}
                           }]
         {
          :default        inst-initial
          :a-300          inst-initial
          :bcr-2000       {
                           :knobs   (blank-map 55)
                           :buttons (blank-map 19)
                           }
          :keystation     inst-initial
          :acoustic-pitch {
                           :listeners #{}
                           :note      [0.0 0.0]
                           }

          :spacenav       {
                           :listeners   #{}
                           :rotation    (qt)
                           :translation (v3 0)
                           }

          :bike           {
                           :listeners #{}
                           }
          }))

(swap! app/s assoc :controllers s)





(defn- on-midi-evt [instrument event osc-msg]
  (let [[index val] (vec (. osc-msg (get_args)))
        listeners (get-in @app/s [:controllers instrument :listeners])]
    (swap! app/s assoc-in [:controllers instrument event index] val)
    (doseq [listener listeners] (listener event index val))
    ))

(defn- on-pitch-evt [instrument osc-msg]
  (let [pitch-and-amp (vec (. osc-msg (get_args)))
        listeners (get-in @app/s [instrument :listeners])]
    (swap! app/s assoc-in [:controllers instrument :note] pitch-and-amp)
    (doseq [listener listeners] (listener pitch-and-amp))
    ))





(defn update-spacenav []
  (let [spacenav {
                  :translation (.. SpaceNavigator Translation)
                  :rotation    (.. SpaceNavigator Rotation)}
        listeners (get-in @app/s [:spacenav :listeners])]
    (swap! app/s update-in [:controllers :spacenav] #(union % spacenav))
    (doseq [listener listeners] (listener spacenav))
    )
  )

(defn poll [obj key]
  (update-spacenav)
  ;(move-bike)
  ;(swap! s assoc :keys {
  ;                      :space (Input/GetKey (. KeyCode Space))
  ;                      :a (Input/GetKey (. KeyCode A))
  ;                      :b (Input/GetKey (. KeyCode B))
  ;                      :c (Input/GetKey (. KeyCode C))
  ;                      })
  )

;(hook+ (object-named "App") :update #'poll)
(hook+ (object-named "App") :fixed-update #'poll)

(o/listen "/a-300/note" (fn [osc-msg] (on-midi-evt :a-300 :note osc-msg)))
(o/listen "/keystation/note" (fn [osc-msg] (on-midi-evt :keystation :notes osc-msg)))
(o/listen "/bcr-2000/buttons" (fn [osc-msg] (on-midi-evt :bcr-2000 :buttons osc-msg)))
(o/listen "/bcr-2000/knobs" (fn [osc-msg] (on-midi-evt :bcr-2000 :knobs osc-msg)))
(o/listen "/acoustic-pitch/note" (fn [osc-msg] (on-pitch-evt :acoustic-pitch osc-msg)))



(defn listen
  "registers a listener for instrument events. listener must accept args: midi-evt index val"
  [instrument listener]
  (log (str instrument listener))
  (swap! app/s update-in [:controllers instrument :listeners] #(union % #{listener}))
  nil
  )

(defn get-notes
  "returns map of currently played notes and their velocities"
  ([instrument]
   (->> (get-in @app/s [:controllers instrument :notes])
        (filter #(> (second %) 0))
        (flatten)
        (apply hash-map)))
  ([] (get-notes :default)))

(defn get-note "returns current velocity of note"
  ([instrument n] (get-in @app/s [:controllers instrument :notes n]))
  ([n] (get-note :default)))
