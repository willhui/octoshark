; Scene graph data structure

(ns octoshark.sg
  (:import (javax.media.opengl GL GLAutoDrawable))
  (:use octoshark.math.matrix
	octoshark.mesh
	octoshark.material))

(defn create-sg-node
  "Construct a scene graph node."
  ([mesh material matrix]
     { :leaf? true :transform matrix
      :mesh mesh :material material })

  ([matrix children]
     { :leaf? false :transform matrix :children children }))

(defn add-sg-child
  "Attach a scene graph node as a child of another scene graph node."
  [sg-node child-id]
  (assoc sg-node :children (conj (sg-node :children) child-id)))

(defn render-sg
  "Render the subtree represented by this scene graph node."
  [#^GLAutoDrawable gl table id]
  (let [node @(table id)]
    (doto gl
      (.glMatrixMode GL/GL_MODELVIEW)
      (.glPushMatrix)
      (.glMultMatrixf (to-gl-matrix-form (:transform node)) 0))
    (if (:leaf? node)
      (upload-mesh gl (:mesh node) (first (:textures (:material node))))
      (doseq [child-id (:children node)]
	(render-sg gl table child-id)))
    (.glPopMatrix gl)))
    