(ns ovr
  (:import OVRInput))

(def consts
  {
   :button-none                  OVRInput+Button/None
   :button-one                   OVRInput+Button/One
   :button-two                   OVRInput+Button/Two
   :button-three                 OVRInput+Button/Three
   :button-four                  OVRInput+Button/Four
   :button-start                 OVRInput+Button/Start
   :button-back                  OVRInput+Button/Back
   :button-pri-shoulder          OVRInput+Button/PrimaryShoulder
   :button-pri-index-trigger     OVRInput+Button/PrimaryIndexTrigger
   :button-pri-hand-trigger      OVRInput+Button/PrimaryHandTrigger
   :button-pri-thumbstick        OVRInput+Button/PrimaryThumbstick
   :button-pri-thumbstick-up     OVRInput+Button/PrimaryThumbstickUp
   :button-pri-thumbstick-down   OVRInput+Button/PrimaryThumbstickDown
   :button-pri-thumbstick-left   OVRInput+Button/PrimaryThumbstickLeft
   :button-pri-thumbstick-right  OVRInput+Button/PrimaryThumbstickRight
   :button-pri-touchpad          OVRInput+Button/PrimaryTouchpad
   :button-sec-shoulder          OVRInput+Button/SecondaryShoulder
   :button-sec-index-trigger     OVRInput+Button/SecondaryIndexTrigger
   :button-sec-hand-trigger      OVRInput+Button/SecondaryHandTrigger
   :button-sec-thumbstick        OVRInput+Button/SecondaryThumbstick
   :button-sec-thumbstick-up     OVRInput+Button/SecondaryThumbstickUp
   :button-sec-thumbstick-down   OVRInput+Button/SecondaryThumbstickDown
   :button-sec-thumbstick-left   OVRInput+Button/SecondaryThumbstickLeft
   :button-sec-thumbstick-right  OVRInput+Button/SecondaryThumbstickRight
   :button-sec-touchpad          OVRInput+Button/SecondaryTouchpad
   :button-dpad-up               OVRInput+Button/DpadUp
   :button-dpad-down             OVRInput+Button/DpadDown
   :button-dpad-left             OVRInput+Button/DpadLeft
   :button-dpad-right            OVRInput+Button/DpadRight
   :button-up                    OVRInput+Button/Up
   :button-down                  OVRInput+Button/Down
   :button-left                  OVRInput+Button/Left
   :button-right                 OVRInput+Button/Right
   :button-any                   OVRInput+Button/Any

   :touch-none                   OVRInput+Touch/None
   :touch-one                    OVRInput+Touch/One
   :touch-two                    OVRInput+Touch/Two
   :touch-three                  OVRInput+Touch/Three
   :touch-four                   OVRInput+Touch/Four
   :touch-pri-index-trigger      OVRInput+Touch/PrimaryIndexTrigger
   :touch-pri-thumbstick         OVRInput+Touch/PrimaryThumbstick
   :touch-pri-thumb-rest         OVRInput+Touch/PrimaryThumbRest
   :touch-pri-touchpad           OVRInput+Touch/PrimaryTouchpad
   :touch-sec-index-trigger      OVRInput+Touch/SecondaryIndexTrigger
   :touch-sec-thumbstick         OVRInput+Touch/SecondaryThumbstick
   :touch-sec-thumb-rest         OVRInput+Touch/SecondaryThumbRest
   :touch-sec-touchpad           OVRInput+Touch/SecondaryTouchpad
   :touch-any                    OVRInput+Touch/Any

   :near-touch-none              OVRInput+NearTouch/None
   :near-touch-pri-index-trigger OVRInput+NearTouch/PrimaryIndexTrigger
   :near-touch-pri-thumb-buttons OVRInput+NearTouch/PrimaryThumbButtons
   :near-touch-sec-index-trigger OVRInput+NearTouch/SecondaryIndexTrigger
   :near-touch-sec-thumb-buttons OVRInput+NearTouch/SecondaryThumbButtons
   :near-touch-any               OVRInput+NearTouch/Any

   :axis-1d-none                 OVRInput+Axis1D/None
   :axis-1d-pri-index-trigger    OVRInput+Axis1D/PrimaryIndexTrigger
   :axis-1d-pri-hand-trigger     OVRInput+Axis1D/PrimaryHandTrigger
   :axis-1d-sec-index-trigger    OVRInput+Axis1D/SecondaryIndexTrigger
   :axis-1d-sec-hand-trigger     OVRInput+Axis1D/SecondaryHandTrigger
   :axis-1d-any                  OVRInput+Axis1D/Any

   :axis-2d-none                 OVRInput+Axis2D/None
   :axis-2d-pri-thumbstick       OVRInput+Axis2D/PrimaryThumbstick
   :axis-2d-pri-touchpad         OVRInput+Axis2D/PrimaryTouchpad
   :axis-2d-sec-thumbstick       OVRInput+Axis2D/SecondaryThumbstick
   :axis-2d-sec-touchpad         OVRInput+Axis2D/SecondaryTouchpad
   :axis-2d-any                  OVRInput+Axis2D/Any

   :controller-none              OVRInput+Controller/None
   :controller-l-touch           OVRInput+Controller/LTouch
   :controller-r-touch           OVRInput+Controller/RTouch
   :controller-touch             OVRInput+Controller/Touch
   :controller-remote            OVRInput+Controller/Remote
   :controller-gamepad           OVRInput+Controller/Gamepad
   :controller-active            OVRInput+Controller/Active
   :controller-all               OVRInput+Controller/All
   })

(defn get "returns state of controller part" [part controller] 
 (OVRInput/Get (consts part) (consts controller))
)


(comment
  (get :button-any :controller-r-touch)
  )

