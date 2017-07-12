This network is built from 85 RSS flows extracted from March to December 2014.

VERTICES ####################################################################################
Vertices represent countires. In the file "ectqg2015_countries.csv", the line
	15,"Mexico",2642.32 
indicates that Mexico (name) is associated to identifier 15 (id) and is cited 2642.32 times (nb_citations).
The number of citations of country A corresponds to the sum of 1/k(i) over all RSS items citing country A 
where k(i) is the number of countries cited in RSS item i. 


EDGES ####################################################################################
An edge exists between two countries when at least one RSS item cites both countries.
In the file "ectqg2015_countries.csv", the line
	6,5,16796.5
indicates that countries with id "6" (Russia) and "5" (Ukraine) are co-cited 16796.5 times.
It corresponds to the sum of 2/(k(i)*(k(i)-1)) over all RSS items citing BOTH country A and country B 
where k(i) is the the number of countries cited in RSS item i.
