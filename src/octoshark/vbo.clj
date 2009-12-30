(ns octoshark.vbo
  (:import (javax.media.opengl GLAutoDrawable GL))
  (:import (com.sun.opengl.util BufferUtil)))

(defn- create-vbo-from-bufutil-obj
  [gl buf size kind]
  (let [id-array (make-array Integer 1)]
    (do (doto gl
	  (.glGenBuffersARB 1 id-array)
	  (.glBindBufferARB kind (id-array 0))
	  (.glBufferDataARB kind buf size (. GL GL_STATIC_DRAW_ARB)))
	(aget id-array 0))))

(defn- create-array-vbo
  "Create a new array vertex buffer object and copy data into it."
  [gl data]
  (let [buf (BufferUtil/newFloatBuffer (count data))]
    (do
      (.put buf (float-array data))
