(ns instaparse.core-test
  (:use clojure.test)
  (:require [instaparse.core :as insta]))

(def as-and-bs
  (insta/parser
    "S = AB*
     AB = A B
     A = 'a'+
     B = 'b'+"))

(def as-and-bs-alternative
  (insta/parser
    "S:={AB}  ;
     AB ::= (A, B)
     A : \"a\" + ;
     B ='b' + ;"))


(def as-and-bs-enlive
  (insta/parser
    "S = AB*
     AB = A B
     A = 'a'+
     B = 'b'+"    
    :output-format :enlive))

(def as-and-bs-variation1
  (insta/parser
    "S = AB*
     AB = 'a'+ 'b'+"))

(def as-and-bs-variation2
  (insta/parser
    "S = ('a'+ 'b'+)*"))

(def paren-ab
  (insta/parser
    "paren-wrapped = '(' seq-of-A-or-B ')'
     seq-of-A-or-B = ('a' | 'b')*"))

(def paren-ab-hide-parens
  (insta/parser
    "paren-wrapped = <'('> seq-of-A-or-B <')'>
     seq-of-A-or-B = ('a' | 'b')*"))

(def paren-ab-manually-flattened
  (insta/parser
    "paren-wrapped = <'('> ('a' | 'b')* <')'>"))
  
(def paren-ab-hide-tag
  (insta/parser
    "paren-wrapped = <'('> seq-of-A-or-B <')'>
     <seq-of-A-or-B> = ('a' | 'b')*"))

(def plus
  (insta/parser
    "plus = plus <'+'> plus | num
     num = '0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9'"))

(def plus-e
  (insta/parser
    "plus = plus <'+'> plus | num
     num = '0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9'"
    :output-format :enlive))

(def ambiguous
  (insta/parser
    "S = A A
     A = 'a'*"))

(def ord-test
  (insta/parser
    "S = Even / Odd
     Even = 'aa'*
     Odd = 'a'+"))

(deftest parsing-tutorial
  (are [x y] (= x y)
    (as-and-bs "aaaaabbbaaaabb")
    [:S
     [:AB [:A "a" "a" "a" "a" "a"] [:B "b" "b" "b"]]
     [:AB [:A "a" "a" "a" "a"] [:B "b" "b"]]]
    
    (as-and-bs-enlive "aaaaabbbaaaabb")
    '{:tag :S,
     :content
     ({:tag :AB,
       :content
       ({:tag :A, :content ("a" "a" "a" "a" "a")}
         {:tag :B, :content ("b" "b" "b")})}
       {:tag :AB,
      :content
      ({:tag :A, :content ("a" "a" "a" "a")}
        {:tag :B, :content ("b" "b")})})}
    
    (as-and-bs-variation1 "aaaaabbbaaaabb")
    [:S
     [:AB "a" "a" "a" "a" "a" "b" "b" "b"]
     [:AB "a" "a" "a" "a" "b" "b"]]
    
    (as-and-bs-variation2 "aaaaabbbaaaabb")
    [:S "a" "a" "a" "a" "a" "b" "b" "b" "a" "a" "a" "a" "b" "b"]
    
    (paren-ab "(aba)")
    [:paren-wrapped "(" [:seq-of-A-or-B "a" "b" "a"] ")"]
    
    (paren-ab-hide-parens "(aba)")
    [:paren-wrapped [:seq-of-A-or-B "a" "b" "a"]]
    
    (paren-ab-manually-flattened "(aba)")
    [:paren-wrapped "a" "b" "a"]
    
    (paren-ab-hide-tag "(aba)")
    [:paren-wrapped "a" "b" "a"]
    
    (insta/transform
      {:num read-string
       :plus +}
      (plus "1+2+3+4+5"))
    15

    (insta/transform
      {:num read-string
      :plus +}
      (plus-e "1+2+3+4+5"))
    15    
    
    ((insta/parser "S = 'a' S | '' ") "aaaa")
    [:S "a" [:S "a" [:S "a" [:S "a" [:S]]]]]
    
    ((insta/parser "S = S 'a' | Epsilon") "aaaa")
    [:S [:S [:S [:S [:S] "a"] "a"] "a"] "a"]
    
    (set (insta/parses ambiguous "aaaaaa"))
    (set ([:S [:A "a"] [:A "a" "a" "a" "a" "a"]]
           [:S [:A "a" "a" "a" "a" "a" "a"] [:A]]
           [:S [:A "a" "a"] [:A "a" "a" "a" "a"]]
           [:S [:A "a" "a" "a"] [:A "a" "a" "a"]]
           [:S [:A "a" "a" "a" "a"] [:A "a" "a"]]
           [:S [:A "a" "a" "a" "a" "a"] [:A "a"]]
           [:S [:A] [:A "a" "a" "a" "a" "a" "a"]]))
    
    ))
    