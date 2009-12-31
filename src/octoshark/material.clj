(ns octoshark.material)

(defn create-material
  [texture]
  { :textures [texture] :shader nil })