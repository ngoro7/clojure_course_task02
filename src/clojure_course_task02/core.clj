(ns clojure-course-task02.core
  (:require [clojure.java.io :as io])
  (:gen-class))

(defn- filter-str-list [patt str-list]
  "Filter string list 'str-list' using compiled regular"
  "expression 'patt' "
  (filter #(re-matches patt %) str-list))

(defn- filter-file-tree [dir patt]
  "Travers recurcively file free starting from 'dir'"
  "Filter and produce file list in tree by using compiled"
  "regular expression 'patt' "
  (let [
        ;; Two lists in current dir: files and dirs list
        fmap (group-by #(.isDirectory %) (.listFiles dir))
        ;; only dirs
        dir-list (get fmap true)

        ;; Prepare tree-scan tasks in background
        ;; and force to run it by using 'doall'
        ;; Keep task futures in futures-list
        futures-list (doall (map #( future (filter-file-tree % patt)) dir-list))

        ;; current dir files (names)
        file-names (map #(. % (getName)) (get fmap false))
        ;; filtered  current dir file name list
        filtered-file-names (filter-str-list patt file-names)

        ;; here we waiting for completion of background scans
        child-file-names (map deref futures-list)
      ]
    ;; union filtered current list
    ;; and files lists from subtree
    (concat filtered-file-names (flatten child-file-names))))


(defn find-files [file-name path]
  "Implement searching for a file using his name as a regexp."
  (let [patt (re-pattern file-name)
        dir (io/file path)
        ]
        (filter-file-tree dir patt)))


(defn usage []
  (println "Usage: $ run.sh file_name path"))

(defn -main [file-name path]
  (if (or (nil? file-name)
          (nil? path))
    (usage)
    (do
      (println "Searching for " file-name " in " path "...")
      (time (dorun (map println (find-files file-name path))))
      (shutdown-agents))))
