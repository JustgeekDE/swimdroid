set datafile separator ","
set terminal png truecolor size 12000,720  background "#FFFFFF"
set output "orientation.png"

set xtics 0.5

set arrow from graph 0, first 0 to graph 1, first 0 nohead front ls 63 lw 2

plot \
     "1444162883.-.em8170.orientation.csv" using ($1/60000):2 with lines, \
     "1444162883.-.em8170.orientation.csv" using ($1/60000):3 with lines, \
     "1444162883.-.em8170.orientation.csv" using ($1/60000):4 with lines

set output "quad.png"

set arrow from graph 0, first 0 to graph 1, first 0 nohead front ls 63 lw 2

plot \
    "1444162883.-.em8170.quaternion.csv" using ($1/60000):2 with lines, \
    "1444162883.-.em8170.quaternion.csv" using ($1/60000):3 with lines, \
    "1444162883.-.em8170.quaternion.csv" using ($1/60000):4 with lines, \
    "1444162883.-.em8170.quaternion.csv" using ($1/60000):5 with lines, \
    "1444162883.-.em8170.quaternion.csv" using ($1/60000):6 with lines
