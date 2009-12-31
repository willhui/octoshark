; Create a blank OpenGL drawing context in Clojure.

(ns octoshark.gl
  (:import (java.awt Frame)
	   (java.awt.event WindowListener WindowAdapter KeyListener KeyEvent)
	   (java.util.concurrent CountDownLatch)
	   (javax.media.opengl GLCanvas GLEventListener GL GLAutoDrawable)
	   (javax.media.opengl.glu GLU)
	   (com.sun.opengl.util Animator))
  (:use octoshark.material
	octoshark.math.matrix
	octoshark.mesh
	octoshark.sg
	octoshark.texture))

(def glu (new GLU))
(def canvas (new GLCanvas))
(def frame (new Frame "Jogl 3D Shape/Rotation"))
(def animator (new Animator canvas))
(defn exit "Stops animation and closes the OpenGL frame." []
  (.stop animator)
  (.dispose frame))

(def plane (transform-mesh (rotation-matrix (/ Math/PI 2) 0 0) unit-plane))

(def display-fn-list (ref '()))

(def sg-node-table (ref {}))

(defn add-display-fn
  "Run a function the next time JOGL redraws the scene."
  [f]
  (dosync (alter display-fn-list conj f)))

(defn gl-call-and-wait
  "Use this function to make OpenGL calls from the REPL."
  [f]
  (let [result-ref (ref nil)
	error-ref (ref nil)
	latch (CountDownLatch. 1)
	wrapper #(try
		  (let [result (f %)]
		    (dosync (ref-set result-ref result)))
		  (catch Exception e
		    (dosync (ref-set error-ref e)))
		  (finally
		   (.countDown latch)))]
    (add-display-fn wrapper)
    (.await latch)
    (let [result @result-ref
	  error @error-ref]
      (if error
	(throw (Exception. error))
	result))))

(defn process-display-fn-list
  "Run all functions registered for execution at redraw time."
  [gl]
  (doseq [f (dosync (let [snapshot @display-fn-list]
		      (ref-set display-fn-list '())
		      snapshot))]
    (f gl)))

(defn add-resource
  "Register an id -> resource mapping. If a resource already
  exists with the same id, it is overwritten."
  [table id res]
  (dosync (alter table assoc id (ref (assoc res :id id)))))

(defn update-resource
  "Updates the resource associated with a given id. If a resource
  already exists with the same id, an exception is thrown."
  [table id res]
  (dosync (ref-set (@table id) (assoc res :id id))))

(defn remove-resource
  "Removes a given resource from the resource table."
  [table id]
  (dosync (alter table dissoc id)))

(defn add-object-to-scene
  ([id mesh]
     (add-object-to-scene id mesh nil))
  
  ([id mesh material]
     (let [child (create-sg-node mesh material identity-matrix)]
       (add-resource sg-node-table id child)
       (update-resource sg-node-table
			:root
			(add-sg-child @(sg-node-table :root) id)))))

(.addGLEventListener
 canvas
 (proxy [GLEventListener] []
   (display
    [#^GLAutoDrawable drawable]
    (let [gl (.getGL drawable)]
      (do
	(process-display-fn-list gl)
	(.glClear gl GL/GL_COLOR_BUFFER_BIT)
	;(.glClear gl GL/GL_DEPTH_BUFFER_BIT)
	(.glLoadIdentity gl)
	(.glTranslatef gl 0 0 0)

	(render-sg gl sg-node-table :root))))

   (displayChanged [drawable m d])

   (init
    [#^GLAutoDrawable drawable]
    (doto (.getGL drawable)
      (.glShadeModel GL/GL_SMOOTH)
      (.glClearColor 0 0 0 0)
      ;(.glClearDepth 1)
      (.glOrtho 0 1 0 1 -1 1))
      ;(.glEnable GL/GL_DEPTH_TEST)
      ;(.glDepthFunc GL/GL_LEQUAL)
      ;(.glHint GL/GL_PERSPECTIVE_CORRECTION_HINT GL/GL_NICEST))
    
    (let [tex (load-texture-from-disk "../../data/brick.jpg")
	  mat (create-material tex)
	  root-node (create-sg-node identity-matrix [])]
      (add-resource sg-node-table :root root-node)
      (add-object-to-scene :test-plane plane mat))
    
    (.addKeyListener
     drawable
     (proxy [KeyListener] []
       (keyPressed
	[e]
	(when (= (.getKeyCode e) KeyEvent/VK_ESCAPE)
	  (exit))))))

   (reshape
    [#^GLAutoDrawable drawable x y w h]
    (when (> h 0)
      (let [gl (.getGL drawable)]
	(.glOrtho gl 0 1 0 1 -1 1))))))
;	(.glMatrixMode gl GL/GL_PROJECTION)
;	(.glLoadIdentity gl)
;	(.gluPerspective glu 50 (/ w h) 1 1000)
;	(.glMatrixMode gl GL/GL_MODELVIEW)
;	(.glLoadIdentity gl))))))

(defn main
  "Our program begins running here..."
  []
  (doto frame
    (.add canvas)
    (.setSize 640 480)
    (.setUndecorated true)
    (.setExtendedState Frame/MAXIMIZED_BOTH)
    (.addWindowListener
     (proxy [WindowAdapter] []
       (windowClosing [e] (exit))))
    (.setVisible true))
  (.start animator)
  (.requestFocus canvas))
