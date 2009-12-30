(ns game.graphics.texture
  (:import (com.sun.opengl.util.texture TextureIO)
	   (javax.media.opengl GL)
	   (javax.imageio ImageIO)
	   (java.io File))
  (:use clojure.test))

(defn load-texture-from-disk
  [filename]
  (let [texture (TextureIO/newTexture (File. filename) false)]
    (doto texture
      (.bind)
      (.setTexParameteri GL/GL_TEXTURE_WRAP_S GL/GL_REPEAT)
      (.setTexParameteri GL/GL_TEXTURE_WRAP_T GL/GL_REPEAT)
      (.setTexParameteri GL/GL_TEXTURE_MAG_FILTER GL/GL_NEAREST)
      (.setTexParameteri GL/GL_TEXTURE_MIN_FILTER GL/GL_NEAREST))))