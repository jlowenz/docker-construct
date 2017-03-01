(ns build.vtk)

(def spec {:name "vtk"
           :creator "Jason L. Owens <jason.l.owens.civ@mail.mil>"
           :description "An image with VTK installed. For debugging"
           :base "nvidia/cuda:8.0-cudnn5-devel-ubuntu16.04"
           :depends #{:vtk}})