(ns build.keras-with-ros)

(def spec {:name "keras-with-ros"
           :creator "Jason Owens <jason.l.owens.civ@mail.mil>"
           :description "Add ROS Kinetic Base install to a Keras image"
           :base "jlowenz/keras:1.0"
           :append-components [:user-entry]
           :depends #{:ros}})