(ns game.graphics.mesh
  (:import (javax.media.opengl GL GLAutoDrawable))
  (:use game.graphics.math.matrix)
  (:use clojure.test))

(def unit-plane
  { :verts [-1 0 -1
	    -1 0 1
	    1 0 -1
	    1 0 1]
   :norms (apply conj [] (repeat 4 [0 1 0]))
   :texcoords [0 0
	       0 1
	       1 0
	       1 1]
   :indexes [0 1 2 2 1 3] })

(defn- transform-3vec
  [m x y z]
  (take 3 (matrix-apply m [x y z 1])))

(defn transform-mesh
  "Applies a matrix transformation to all mesh vertices."
  [m mesh]
  (assoc mesh
    :verts (vec (loop [[x y z & rest] (mesh :verts) output []]
		  (if x
		    (recur rest (into output (transform-3vec m x y z)))
		    output)))))

(defn create-plane
  "Creates a plane."
  [xsize zsize]
  (transform-mesh (scaling-matrix xsize 1 zsize) unit-plane))

(defn upload-mesh
  "Uploads mesh data (vertices, normals, texture coordinates) to the GPU."
  [#^GLAutoDrawable gl mesh tex]
  (do
    (.enable tex)
    ;(.glTexEnvf GL/GL_TEXTURE_ENV GL/GL_TEXTURE_ENV_MODE GL/GL_REPLACE)
    (.bind tex)
    (.glBegin gl GL/GL_TRIANGLES)
    (doseq [i (mesh :indexes)]
      (let [vert-idx (* i 3)
	    texc-idx (* i 2)
	    [x y z] (subvec (mesh :verts) vert-idx (+ vert-idx 3))
	    [u v] (subvec (mesh :texcoords) texc-idx (+ texc-idx 2))]
	(.glTexCoord2f gl u v)
	(.glVertex3f gl x y z)))
    (.glEnd gl)
    (.glFlush gl)
    (.disable tex)))

(deftest test-create-plane
  (is (= (:verts (create-plane 1 2))
	 [-1 0 -2
	  -1 0 2
	  1 0 -2
	  1 0 -2
	  -1 0 2
	  1 0 2])))