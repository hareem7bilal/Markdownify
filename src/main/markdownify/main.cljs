(ns markdownify.main
(:require [reagent.dom :as rg]
[reagent.core :as r]
["showdown" :as showdown]
["@mui/material" :as mui]
))


(defonce showdown-converter (showdown/Converter.))
(defonce flash-message (r/atom nil))
(defonce flash-timeout (r/atom nil))

(defn flash
  ([text]
   (flash text 3000))
  ([text ms]
   (js/clearTimeout @flash-timeout)
   (reset! flash-message text)
   (reset! flash-timeout
           (js/setTimeout #(reset! flash-message nil) ms))))

(defn md->html[md]
(.makeHtml showdown-converter md))

(defn html->md[html]
(.makeMarkdown showdown-converter html))

;;https://medium.com/hackernoon/copying-text-to-clipboard-with-javascript-df4d4988697f
(defn copy-to-clipboard [str]
(let [el (.createElement js/document "textarea")
selected (when (pos? (-> js/document .getSelection .-rangeCount))
(-> js/document .getSelection (.getRangeAt 0))
)
]
(set! (.-value el) str)
(.setAttribute el "readonly" "")
(set! (-> el .-style .-postion) "absolute")
(set! (-> el .-style .-left) "-9999px")
(-> js/document .-body (.appendChild el))
(.select el)
(.execCommand js/document "copy")
(-> js/document .-body (.removeChild el))
(when selected
(-> js/document .getSelection .removeAllRanges)
(-> js/document .getSelection (.addRange selected)))
)
)

(defonce text-state (r/atom {:format :md
                                   :value ""}))

(defn ->md [{:keys [format value]}]
  (case format
    :md value
    :html (html->md value)))

(defn ->html [{:keys [format value]}]
  (case format
    :md (md->html value)
    :html value))

(defn app []
[:div {:style {:position :relative}}

 [:div
    {:style {:position :absolute
             :margin :auto
             :left 0
             :right 0
             :text-align :center
             :max-width 200
             :padding "1em"
             :background-color "gold"
             :color "white"
             :z-index 100
             :border-radius 10
             :transform (if @flash-message
                          "scaleY(1)"
                          "scaleY(0)")
             :transition "transform 0.2s ease-out"}}
    @flash-message]

[:h1 {:style {:color "SlateGrey"}} "Markdownify"]

[:div 
{:style {:display :flex :justify-content :space-between}}

[:div {:style {:flex "1"}}
[:> mui/TextField 
{:on-change (fn [e] 
 (reset! text-state {:format :md :value (-> e .-target .-value)})
)
:value (:value @text-state)
:id "outlined-multiline-flexible"
:label "Markdown"
:multiline true
:maxRows 20
:style {:width "100%"}
}]



[:> mui/Button {:variant "contained" :on-click (fn[]
(copy-to-clipboard (:value @text-state))
(flash "Markdown copied to clipboard")
)
:style {:margin-top "1em"}}
"Copy Markdown"]
]




[:div {:style {:flex "1"}}
[:> mui/TextField 
{:on-change (fn [e] 
 (reset! text-state {:format :html :value (-> e .-target .-value)})
)
:value (->html @text-state)
:id "outlined-multiline-flexible"
:label "HTML"
:multiline true
:maxRows 20
:style {:width "100%"}
}]



[:> mui/Button {:variant "contained" :on-click (fn[]
(copy-to-clipboard (->html @text-state))
(flash "HTML copied to clipboard")
)
:style {:margin-top "1em"}}
"Copy HTML"]
]

[:div {:style {:flex "1" :padding-left "2em"}}
[:h2 {:style {:color "DodgerBlue"}} "HTML Preview"]
[:div {
    :style {:height "350px"}
    :dangerouslySetInnerHTML {:__html (->html @text-state)}}]
]

]

]

)

(defn mount! []
(rg/render [app]
(.getElementById js/document "app")
)
)

(defn main! []
(println "Welcome to the app!")
(mount!)
)



(defn reload! []
(println "Reloaded")
(mount!)
)