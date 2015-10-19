set datafile separator ","
set terminal png truecolor size 12000,720  background "#FFFFFF"

set xtics 100

list = system('ls *classifier*.csv')
do for [file in list] {
   set output sprintf('results/classifier.-.%s.png', file)
   set title sprintf("Raw %s", file)
   plot \
    file u 1 with lines, \
    file u 2 with lines, \
    file u 3 with lines, \
    file u 4 with points
}

list = system('ls *strokes*.csv')
do for [file in list] {
   set output sprintf('results/strokes.-.%s.png', file)
   set title sprintf("Raw %s", file)
   plot \
    file u 2 with lines title "raw", \
    file u 3 with lines title "filtered", \
    file u 4 with lines title "average a", \
    file u 5 with points pointtype 1 title "stroke", \
    file u 6 with points title "lap"
}

set xtics 0.5

set arrow from graph 0, first 0 to graph 1, first 0 nohead front ls 63 lw 2
set arrow from graph 0, first 150 to graph 1, first 150 nohead front ls 63 lw 2


list = system('ls *orientation*.csv')

do for [file in list] {
    set output sprintf('results/raw.-.%s.png', file)
    set title sprintf("Raw %s", file)
    plot \
        file using ($1/60000):2 with lines, \
        file using ($1/60000):3 with lines, \
        file using ($1/60000):4 with lines


    set output sprintf('results/decay.-.%s.png', file)
    set title sprintf("Decay %s", file)
    sumA = 0
    sumB = 0
    sumC = 0
    countC = 0
    plot \
        file using ($1/60000):2 with lines, \
        file using ($1/60000):(sumA = (0.995 * sumA) + (0.005 * $2)) with lines, \
        file using ($1/60000):(sumB = (0.999 * sumB) + (0.001 * $2)) with lines
}
