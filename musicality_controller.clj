(ns musicality-controller
  (:use [arcadia.core]
        [arcadia.linear]
        [arcadia.introspection]
        [clojure.set]
        )
  (:import (UnityEngine
            CapsuleCollider
            Collider
            Color
            ConfigurableJoint
            GameObject
            LineRenderer
            Material
            Mathf
            Renderer
            Resources
            Rigidbody
            SphereCollider
            TextMesh            
            Vector3))
  (:import Manipulate)
  (:import NotePad)
  (:import Tonnetz)
  (:import (uWindowCapture
            UwcWindowTexture
            WindowTextureScaleControlType)))



(defn init [app-obj role-key]
  (log "MusicalityController init")
  (set! (.. (create-primitive :cube) transform localScale)
        (v3 0.1))
  (state+ app-obj :lattice
          {:lattice-obj (object-named "Lattice")
           :notes {}
           :notes-mod12 {}
           })
  (def app app-obj)
  #_(fretted-inst)
  )

#_(hook (object-named "App") :start :init)


#_(clojure.pprint/pprint (state app :lattice))
#_(state app :lattice)
#_(update-state app :lattice assoc :notes {})
#_(update-state app :lattice assoc :notes-mod12 {})
#_(update-state app :lattice assoc-in [:notes :1] #{})


(defn mk-note [note-num] "instantiates a note egg"
  (let [{:keys [:lattice-obj]} (state app :lattice)
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
   
    note-obj
    ))

(defn note+ "adds a note egg to the lattice; returns note-obj"
  [note-num]
  (let [{:keys [:lattice-obj :notes :notes-mod12]} (state app :lattice)
        note-obj (mk-note note-num)]
   
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




(defn mk-memo [msg]
  ;; todo title
  ;; todo timestamp
  ;; todo: join varargs msg strs with \n
  ;; todo: auto-resize to text
  (let [obj (instantiate (Resources/Load (str "Prefabs/Memo")))]
    (set! (.. obj name) "memo")
    (mv-obj-in-front-cam obj)
    (with-cmpt (gobj
                (.. obj transform (Find "Label")))
      [tm TextMesh]
      (set! (.. tm text) msg))
    ))

(defn mk-soccer-ball []
  (let [obj (instantiate (Resources/Load "Prefabs/SoccerBall"))]
    (mv-obj-in-front-cam obj)))

(defn mk-golf-ball []
  (let [obj (instantiate (Resources/Load "Prefabs/GolfBall"))]
    (mv-obj-in-front-cam obj)))

#_(mk-soccer-ball)
#_(mk-golf-ball)

#_(mk-memo "Hi I'm just a memo.\nI have length.\nThis a new line.")


(comment "TODO" "
* use memo prefab as base for number picker
* highlight notepads
* add ways to mask off chords, temp change mallet to paddle for strumming
* save scene
* execute fn upon receiving fn-name over osc
"

)


(defn mv-obj-in-front-cam
  "Moves gobj dist units in front of the MainCamera"
  ([dist gobj]
   (let [tr-cam (. Camera/main transform)]
     (set! (.. gobj transform position)
           (v3+ (. tr-cam position) 
                (v3* (. tr-cam forward) dist))))
   gobj)
  ([gobj] (mv-obj-in-front-cam 0.5 gobj)))


(defmacro set-props
  "takes a component and an alternating list of key val key val. sets cmpt.key = val"
  [cmpt & key-val-pairs]
  (cons
    'do
    (->> (partition 2 key-val-pairs)
         (map (fn [[key val]] (list 'set! (list '.. cmpt key) val)))))
  )

(defn- link-objs [gobj gobj-conn anchor anchor-conn]
  (let [joint (ensure-cmpt gobj ConfigurableJoint)
        rigidbody-conn (cmpt gobj-conn Rigidbody)]
    
    (set-props joint
               connectedBody rigidbody-conn
               anchor anchor
               xMotion ConfigurableJointMotion/Locked
               yMotion ConfigurableJointMotion/Locked
               zMotion ConfigurableJointMotion/Locked
               autoConfigureConnectedAnchor false
               connectedAnchor anchor-conn))
  gobj)

(defn rigidbody+ [mass drag angularDrag useGravity gobj]
  (with-cmpt gobj [rb Rigidbody]
      (set-props rb
                 mass (float mass)
                 drag (float drag)
                 angularDrag (float angularDrag)
                 useGravity useGravity))
  gobj)

(defn mk-chain
  "makes a chain of gobjs, returns the tail gobj"
  [link-count gobj-create-fn gobj-conn]
  (if (> link-count 0)
    (let [gobj (gobj-create-fn)]
      ;; move next link into place
      (set! (.. gobj transform position)
            (v3- (.. gobj-conn transform position)
                 (v3 0
                     (+ (* 0.5 (.. gobj transform localScale y))
                        (* 0.5 (.. gobj-conn transform localScale y)))
                     0)))
      
      ;; link to previous link
      (link-objs gobj gobj-conn (v3 0 0.5 0) (v3 0 -0.5 0))
      ;; make next link
      (mk-chain (- link-count 1) gobj-create-fn gobj))
    gobj-conn))

(defn mk-link 
  ([mass r]
   (let [gobj (create-primitive :sphere "link")]
     (with-cmpt gobj [sc SphereCollider]
       (set! (.. gobj transform localScale) (v3 r)))
     (rigidbody+ mass 0.1 0.1 false gobj)
     gobj))
  ([r] (mk-link 0.25 r)))

(defn mk-root
  ([r pos]
   (let [gobj (mk-link r)]
     (set! (.. gobj name) "root")
     (set! (.. gobj transform localPosition) pos)
     (with-cmpt gobj [rigidbody Rigidbody]
       (set! (.. rigidbody isKinematic) true))
     gobj))
  ([r] (mk-root r (v3))))



(defn mk-chime [note-num]
  (let [gobj (mk-note note-num)]

  ;; setup note
  (set! (.. gobj transform localScale) (v3 0.05))
  (cmpt- gobj CapsuleCollider)
  (with-cmpt gobj [sc SphereCollider]
    (set! (.. sc isTrigger) false))
  
  gobj))


(defn manipulate+ [gobj]
  (ensure-cmpt gobj Manipulate)
  (with-cmpt gobj [col Collider]
    (set! (.. col isTrigger) true))
  gobj)

(defn grav+ 
  ([mass drag gobj]
   (with-cmpt gobj [rb Rigidbody]
     (set-props rb
                mass mass
                useGravity true
                drag drag))
   gobj)
  ([mass gobj] (grav+ mass 10 gobj))
  ([gobj] (grav+ 1 gobj)))



(defn mk-hanging-chain
  ([link-count root]
   {:root root
    :tail (->> root
               (manipulate+)
               (mv-obj-in-front-cam)
               (mk-chain link-count #(mk-link 0.008))
               )})
  ([link-count] (mk-hanging-chain
                 link-count
                 (mk-root 0.025 (v3)))))

(defn mk-hanging-chime
  ([link-count chime-mass note-num root]

   (let [tail (->> (mk-hanging-chain link-count root)
                   (:tail)
                   (mk-chain 1 #(mk-chime note-num))
                   (grav+ chime-mass))]
     {:root root
      :tail tail}))
  ([link-count chime-mass note-num]
   (mk-hanging-chime link-count
                     chime-mass
                     note-num
                     (mk-root 0.025 (v3)))))



(comment
  (def handle (->> (mk-root 0.05)
                   (manipulate+)
                   (mv-obj-in-front-cam)))



  (def chime0
    (->> (mk-hanging-chain 12)
         (:tail)
         (mk-hanging-chime 2 0.2 0)
         (:tail)
         (mk-hanging-chain 12)
         (:tail)
         (mk-chain 1 #(->> (mk-root 0.01)
                           (manipulate+)
                           ))))


  (mk-hanging-chime 10 0.2 96)


  (def chime1
    (mk-hanging-chime 10 0.2 60))

  (def chime2
    (mk-hanging-chime 10 0.2 67))

  (def chime3
    (mk-hanging-chime 10 0.2 71))

  (def chime4
    (mk-hanging-chime 10 0.2 72))

  (def chime5
    (mk-hanging-chime 17 0.2 74))

  (def chime6
    (mk-hanging-chime 17 0.2 70))

  (def chime7
    (mk-hanging-chime 17 0.2 80))

  #_(child+ handle (:root chime0) true)
  #_(child+ handle (:tail chime0) true)
  (child+ handle (:root chime1) true)
  (child+ handle (:root chime2) true)
  (child+ handle (:root chime3) true)
  (child+ handle (:root chime4) true)
  (child+ handle (:root chime5) true)
  (child+ handle (:root chime6) true)
  (child+ handle (:root chime7) true)
)

#_(clojure.repl/doc mk-hanging-chime)


(defn mk-chord-chime [chord-notes]
  (->> chord-notes
     (map (fn [note-num]
            (let [[x z] (point-on-arc 
                         12
                         0.3
                         (* 2 Mathf/PI)
                         (mod note-num 12))
                    
                  chime (mk-hanging-chime 10 0.2 note-num)]

              #_(child+ handle (:root chime) true)
              
              )
            ))
     (doall)
     )
)



(comment " Misty

૪.7.2.....૪010000૪730....78037૪૪૪8૪..8
7..8૪3..5780.02.3.5.7...........૪.7.

"

(mk-chord-chime [63 67 70 74]) ;EbMaj7

(mk-chord-chime [58 61 65 68]) ;Bbmin7
(mk-chord-chime [63 67 70 73]) ;Eb7

(mk-chord-chime [56 60 63 67]) ;Abmaj7

(mk-chord-chime [56 59 63 67]) ;Abmin7
(mk-chord-chime [61 64 68 71]) ;Db7

(mk-chord-chime [63 67 70 74]) ;EbMaj7
(mk-chord-chime [60 63 67 70]) ;Cmin7

(mk-chord-chime [53 56 60 63]) ;Fmin7
(mk-chord-chime [58 62 65 68]) ;Bb7

(mk-chord-chime [55 59 62 66]) ;Gmaj7
(mk-chord-chime [60 64 67 70]) ;C7

(mk-chord-chime [53 57 60 64]) ;Fmaj7
(mk-chord-chime [58 62 65 68]) ;Bb7
)


#_(fretted-inst :fret-count 24)
#_(fretted-inst :fret-count 12 :tuning [67 60 64 69])
#_(fretted-inst :fret-count 12 :tuning [40 45 50 55 60])
#_(fretted-inst :fret-count 12 :tuning [36 43 50 57 64 71 78])

#_(clear-notes)


(comment

;; TODO:

#_(mv-on-arc [n radius subdivisions fov up-vec gobj])
#_(mv-by [vec gobj])
#_(mv-to [vec gobj])

#_(spcnav-raycast-mouse-mode [])
#_(spcnav-inertial-ship-mode [])

)


;; TODO: instantiate these objects at runtime
(defn set-uwc-scale-control [obj-name]
  (with-cmpt (object-named obj-name)
    [win-tex UwcWindowTexture]
    (set! (..  win-tex scaleControlType)
          WindowTextureScaleControlType/Manual)))

#_(set-uwc-scale-control "REPL")
#_(set-uwc-scale-control "Emacs")



(comment "TODO: add picks/pointers/mallets to fingertips"
         (let [hands (object-named "GhostHands")
               find-fingertips (fn [hand]
                                 (->> (descendents hand)
                                      (filter #(clojure.string/ends-with? (.  % name) "_end") )
                                      ))
               [left-hand right-hand] (children hands)
               left-fingertips (find-fingertips left-hand)
               right-fingertips (find-fingertips right-hand)
               ]

           (clojure.pprint/pprint
            left-fingertips)   

           ))

