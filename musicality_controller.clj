(ns musicality-controller
  (:use [arcadia.core]
        [arcadia.linear]
        [arcadia.introspection]
        [clojure.set]
        )
  (:import (UnityEngine Color GameObject LineRenderer Material Mathf Renderer Resources TextMesh Vector3))
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

(defn point-on-arc [vert-count r fov vert]
  (let [d-theta (/ fov vert-count)
        progress (- vert  (Mathf/Floor (/ vert-count 2)))
        theta (* progress d-theta)]
    (->> [(* -1 (Mathf/Sin theta)) (* -1 (Mathf/Cos theta))]
         (map #(* r %)))))

(defn fretted-inst
  [& {:keys [fret-count fret-markers tuning]
      :or {fret-count 24
           fret-markers [3 5 7 9 12 15 17 19 21]
           tuning [40 45 50 55 59 64]}} ]
  (->> tuning
       (reverse) ;go from +y to -y but tuning comes in lowest string first
       (map-indexed
        (fn [string-idx note-num-0]
          (->> (range (+ 1 ; 0th fret
                         fret-count))
               (map (fn [fret]
                      (let [note-num (+ fret note-num-0)
                            string-count (count tuning)

                            ; get the curve component from +x to -x (left to right)
                            [x z0] (point-on-arc 
                                    fret-count
                                    0.5 
                                    (* (/ fret-count 24)
                                       5/4
                                       Mathf/PI)
                                    fret)

                            ; get the curve component from -y to y
                            [y z1] (point-on-arc 
                                    string-count
                                    0.5
                                    (* (/ string-count 6)
                                       5/16
                                       Mathf/PI)
                                    string-idx)]
                        (->> (note+ note-num)
                             (mv-note x
                                      y
                                      (+ z0 (* 1/2 z1)))))))
               (doall))))
       (doall)))

#_(fretted-inst :fret-count 24)
#_(fretted-inst :fret-count 12 :tuning [67 60 64 69])

#_(clear-notes)


(defn mk-memo [msg]
  ;; todo title
  ;; todo timestamp
  ;; todo: join varargs msg strs with \n
  ;; todo: auto-resize to text
  (let [obj (instantiate (Resources/Load (str "Prefabs/Memo")))]
    (set! (.. obj name) "memo")
    (with-cmpt (gobj
                (.. obj transform (Find "Label")))
      [tm TextMesh]
      (set! (.. tm text) msg))
    ))

#_(mk-memo "Hi I'm just a memo.\nI have length.\nThis a new line.")


(comment "TODO" "
* use memo prefab as base for number picker
* highlight notepads
* add ways to mask off chords, temp change mallet to paddle for strumming
* save scene
* execute fn upon receiving fn-name over osc
"

)
