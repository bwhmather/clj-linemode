(ns linemode.drivers.star
  (:require [linemode.core :refer [run-program compile-commands shutdown
                                   Printer]]
            [clojure.java.io :as io])
  (:import [java.io OutputStream]))

(def charset-codes
  {"ASCII" 0x00  ; TODO Normal is not ascii but something weird
   "Cp930" 0x02  ; TODO Katakana
   "Cp437" 0x03
   "Cp858" 0x04
   "Cp852" 0x05
   "Cp860" 0x06
   "Cp861" 0x07
   "Cp863" 0x08
   "Cp865" 0x09
   "Cp866" 0x0a
   "Cp855" 0x0b
   "Cp857" 0x0c
   "Cp862" 0x0d
   "Cp864" 0x0e
   "Cp737" 0x0f
   "Cp851" 0x10
   "Cp869" 0x11
   "Cp928" 0x12
   "Cp772" 0x13
   "Cp774" 0x14
   "Cp874" 0x15
   "Cp1252" 0x20
   "Cp1250" 0x21
   "Cp1251" 0x22
   "Cp3840" 0x40
   "Cp3841" 0x41
   "Cp3843" 0x42
   "Cp3844" 0x43
   "Cp3845" 0x44
   "Cp3846" 0x45
   "Cp3847" 0x46
   "Cp3848" 0x47
   "Cp1001" 0x48
   "Cp2001" 0x49
   "Cp3001" 0x4a
   "Cp3002" 0x4b
   "Cp3011" 0x4c
   "Cp3012" 0x4d
   "Cp3021" 0x4e
   "Cp3041" 0x4f})


(def initial-state
  {:charset "ASCII"
   :output []})

(defn op-write-bytes [bs]
  (fn [state] (update-in state [:output] (fn [output] (conj output bs)))))

(defn op-write [s]
  (fn [state] ((op-write-bytes (.getBytes s (:charset state))) state)))

; TODO
(defn op-barcode []
  (fn [state] state))

(defn op-set-charset [charset]
  (fn [state] state))


(defn get-command-builder
  "Given a command name return a function that can be applied to the command
  arguments to generate an operation to mutate compiler state."
  [command-name]
  (case command-name
    :barcode op-barcode
    :write op-write
    ; most commands take no arguments and just dump a fixed string of bytes
    (let [opcode (case command-name
            :reset [0x18]
            :select-bold [0x1b 0x45]
            :cancel-bold [0x1b 0x46]
            :select-highlight [0x1b 0x34]
            :cancel-highlight [0x1b 0x35]
            :fontsize-small [0x1b 0x14]
            :fontsize-medium [0x1b 0x0]
            :fontsize-large [0x1b 0x68 0x32]
            ; :newline [\n]
            :cut-through [0x1b 0x64 0x02]
            :cut-partial [0x1b 0x64 0x03]
            :cut-through-immediate [0x1b 0x64 0x00]
            :cut-partial-immediate [0x1b 0x64 0x01])]
      #(op-write-bytes (byte-array opcode)))))


(defn compile-command
  "Compiles a command vector to an operation that can be applied to update the
  compiler state."
  [command]
  (let [[command-name & args] (if (symbol? command) [command] command)
        builder (get-command-builder command-name)]
    (apply builder args)))


(defn apply-command [state command]
  ((compile-command command) state))


(defrecord StarPrinter [output-stream]
  Printer
  (compile-commands [printer commands]
    (:output (reduce apply-command initial-state commands)))
  (run-program [printer program]
    (doseq [output program]
      (.write output-stream output))
    (.flush output-stream))
  (run-commands [printer commands]
    (run-program printer (compile-commands printer commands)))
  (shutdown [printer]
    (.flush output-stream)))


(defmulti with-star-printer
  (fn [obj f]
    (class obj)))

(defmethod with-star-printer String [path f]
  (with-open [stream (io/output-stream path)]
    (with-star-printer stream f)))

(defmethod with-star-printer OutputStream [stream f]
  (let [printer (StarPrinter. stream)]
    (try
      (f printer)
      (finally
        (shutdown printer)))))
