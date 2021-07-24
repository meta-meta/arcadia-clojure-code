(ns airship
  (:require [osc :as o] ovr)
  (:use arcadia.core arcadia.linear)
  (:import OVRInput
           (UnityEngine ForceMode Input KeyCode Quaternion Rigidbody Time Vector3))
)

(def airship-prop-ring (object-named "airship-prop-ring"))
(def airship-prop (object-named "airship-prop"))
(def airship (object-named "Airship"))
(def handlebar (object-named "Handlebar"))
(def freewheel (object-named "Freewheel"))
(def freewheel-body (cmpt freewheel Rigidbody))
(def airship-body (cmpt airship Rigidbody))



(defn- update-scene [obj key] 
  (let [
          vel (* 100 (.. freewheel-body angularVelocity magnitude))
          angle (Vector3/SignedAngle
                  (.. handlebar transform forward)
                  (.. airship transform forward)
                  Vector3/up)
         
          ]
      
      ;; rotate the prop
      (.Rotate (.. airship-prop transform)
               0
               0
               (* (Time/deltaTime)
                  1
                  vel))

      ;; rotate prop assembly according to handlebar
      (set! (.. airship-prop-ring transform localRotation)
            (euler (v3 0 angle 0)))

      
      )

)

(defn- update-physics [obj key]

    (let [
          vel (* 100 (.. freewheel-body angularVelocity magnitude))
          angle (Vector3/SignedAngle
                  (.. handlebar transform forward)
                  (.. airship transform forward)
                  Vector3/up)
          torque (* angle
                    (* (Time/deltaTime)
                       vel
                       -0.001
                       ))
          ]
      
     
      ;; move airship forward
      (.AddRelativeForce airship-body
                         (v3*
                           Vector3/forward
                           (* Time/deltaTime
                              vel
                              1)
                           )
                         ForceMode/Force)

      ;; move airship up on index trigger
      (.AddRelativeForce airship-body
                         (v3*
                           Vector3/up
                           (* Time/deltaTime
                              100
                              (ovr/get :axis-1d-pri-index-trigger :controller-r-touch))
                           )
                         ForceMode/Acceleration)

      ;; move airship down on hand trigger
      (.AddRelativeForce airship-body
                         (v3*
                           Vector3/up
                           (* Time/deltaTime
                              -100
                              (ovr/get :axis-1d-pri-hand-trigger :controller-r-touch))
                           )
                         ForceMode/Acceleration)

      ;; add torque to airship based on rotation of prop
      (.AddRelativeTorque airship-body
                          (v3*
                            Vector3/up
                            torque
                            )
                          ForceMode/Acceleration)

      ) 

    )

(defn- move-freewheel [pulse]
    (.AddRelativeTorque freewheel-body
                        (v3*
                          Vector3/up
                          (* 10 pulse)
                          )
                        ForceMode/Force)                    ; ForceMode/Impulse ??
    )

(defn- on-bike-pulse [pulse]
  (move-freewheel pulse)
  )

(. o/osc-in (MapInt "/bike" #'on-bike-pulse))



(hook+ (object-named "App") :update :update-scene #'update-scene)
(hook+ (object-named "App") :fixed-update :update-physics #'update-physics)
