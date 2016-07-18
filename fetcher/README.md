
if fresh then "make"

help is available from
java -jar fetcher.jar --help

edit config (just the parts mentioned) like this

java -jar fetcher.jar -p aaaa::212:4b00:60d: edit-config -a 21 -p ee -i 60 9e48

note:
first -p is the ipv6 prefix (he dun goofed)
 -p Power ID is always ee
 -a avr ID varies - here we used 21

check the config:
java -jar fetcher.jar -p aaaa::212:4b00:60d: get-config 9e48

java -jar fetcher.jar -p aaaa::212:4b00:60d: set-date 9e48
java -jar fetcher.jar -p aaaa::212:4b00:60d: get-date 9e48
java -jar fetcher.jar -p aaaa::212:4b00:60d: get-sample 9e48
