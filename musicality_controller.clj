(ns musicality-controller
  (:use [arcadia.core]
        [arcadia.linear]
        [arcadia.introspection]
        [clojure.set]
        )
  (:import (UnityEngine Color GameObject LineRenderer Material Mathf Renderer Resources Vector3))
  (:import NotePad)
  (:import Tonnetz))

(defn init [app-obj role-key]
  (log "MusicalityController init")
  (create-primitive :cube)
  (state+ app-obj :lattice
          {:lattice-obj (object-named "Lattice")
           :notes {}
           :notes-mod12 {}
           })
  (def app app-obj)
  #_(fretted-inst)
  )

#_(hook (object-named "App") :start :init)

#_(state app :lattice)
#_(update-state app :lattice assoc :notes {})
#_(update-state app :lattice assoc :notes-mod12 {})
#_(update-state app :lattice assoc-in [:notes :1] #{})

(defn note+ "adds a note egg to the lattice; returns note-obj"
  [note-num]
  (let [{:keys [:lattice-obj :notes :notes-mod12]} (state app :lattice)
        note-obj (with-cmpt lattice-obj [tn Tonnetz]
                   (instantiate (.. tn prefab)))]
    ;; set obj name
    (set! (.. note-obj name) (str note-num))

    ;; set material for note
    (with-cmpt note-obj [mr Renderer]
      (set! (.. mr material)
            (Resources/Load (str "Materials/n"
                                 (mod (* 7 note-num) ; color harmonically
                                      12)))))

    ;; setup NotePad component
    (with-cmpt note-obj [np NotePad]
      (set! (.. np oscAddr) "/lattice")
      (set! (.. np note) note-num))

    ;; set as child of lattice-obj
    (child+ lattice-obj note-obj)

    ;; add to state.lattice.notes
    (update-state app :lattice assoc-in [:notes note-num]
                  (conj (or (notes note-num) #{})
                        note-obj))

     ;; add to state.lattice.notes-mod12
    (update-state app :lattice assoc-in [:notes-mod12 (mod note-num 12)]
                  (conj (or (notes-mod12 (mod note-num 12)) #{})
                        note-obj))

    note-obj
    ))


(defn get-notes [note-num] (get-in (state app :lattice) [:notes note-num]))
(defn get-notes-mod12 [note-num] (get-in (state app :lattice) [:notes-mod12 (mod note-num 12)]))
#_(get-notes 11)
#_(get-notes-mod12 8)

#_(->> (range 100)
     (map note+))

(defn clear-notes "removes all notes from lattice and from state.notes and state.notes-mod12"
  []
  (let [{:keys [:lattice-obj]} (state app :lattice)]
    (->> (children lattice-obj)
         (map retire)
         (doall))
    
    (update-state app :lattice assoc :notes {})
    (update-state app :lattice assoc :notes-mod12 {})
    )
  )

(defn mv-note [x y z note-obj]
  (set! (.. note-obj transform localPosition) (v3 x y z))
  (.. note-obj transform
      (LookAt (.. (:lattice-obj (state app :lattice)) transform))))

(defn fretted-inst
  [& {:keys [fret-count fret-markers tuning]
      :or {fret-count 24
           fret-markers [3 5 7 9 12 15 17 19 21]
           tuning (->> [4 9 2 7 11 4]
                       (map #(+ % 36)))}} ]
  (->> tuning
       (map-indexed
        (fn [string-idx note-num-0]
          (->> (range (+ 1 fret-count))
               (map (fn [fret]
                      (let [note-num (+ fret note-num-0)]
                        (->> (note+ note-num)
                             (mv-note (- 1 (* 0.1 fret))
                                      (* 0.1 string-idx)
                                      -1)))))
               (doall))))
       (doall)))

#_(fretted-inst)


#_(clear-notes)




