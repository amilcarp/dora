(ns dora.repl
  "Main entry point for repl use"
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [clj-http.client :as http]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clojure.java.shell :as sh]
            [clojure.set :refer :all]
            [clojure.string :as s]
            [cloogle.core :refer :all]
            ;[dgm-analytics.core :refer :all]
            [dora.core :refer :all]
            [mongerr.core :refer :all]
            [dora.server :refer :all]
            [dora.p.agente-web :refer :all]
            [dora.p.ckan :refer :all]
            [dora.data :refer :all]
            [dora.digitalization :refer :all]
            [dora.p.adela :refer :all]
            [dora.p.data-core :refer :all]
            [dora.p.download :refer :all]
            [dora.p.ligas :refer :all]
            [dora.pro-file :refer :all]
            [dora.util :refer :all]
            [dora.p.zendesk :refer :all]
            [environ.core :refer [env]]
            [nillib.text :refer :all]
            [nillib.tipo :refer :all]
            [nillib.worm :refer :all]
            [nillib.formats :refer :all]
            [nlp.sentiment :refer :all]
            [monger.collection :as mc]
            [monger.operators :refer :all]
            [ring.adapter.jetty :refer :all]
            [ring.util.codec :as c]))
