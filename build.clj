(ns build
  (:require
   [clojure.tools.build.api :as b]
   [deps-deploy.deps-deploy :as dd]))

(def version "0.1.0-alpha1")
(def class-dir "target/classes")
(def lib 'net.clojars.eoogbe/fulcro-rad-material)
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def pom-template
  [[:licenses
    [:license
     [:name "MIT License"]
     [:url "http://www.opensource.org/licenses/mit-license.php"]]]])

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (b/write-pom {:basis basis
                :class-dir class-dir
                :lib lib

                :pom-data pom-template
                :scm
                {:url "https://github.com/eoogbe/fulcro-rad-material"
                 :connection "scm:git:git://github.com/eoogbe/fulcro-rad-material.git"
                 :developerConnection "scm:git:ssh://git@github.com:eoogbe/fulcro-rad-material.git"
                 :tag (str "v" version)}

                :src-dirs ["src"]
                :version version})
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn deploy [_]
  (dd/deploy {:artifact jar-file
              :installer :remote
              :pom-file (b/pom-path {:lib lib :class-dir class-dir})}))
