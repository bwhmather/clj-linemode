(ns linemode.core)


(defprotocol Printer
  (compile-commands [printer commands])
  (run-commands [printer commands])
  (run-program [printer program])
  (shutdown [printer]))
