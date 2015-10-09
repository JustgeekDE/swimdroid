set datafile separator ","
set terminal png truecolor size 12000,720  background "#FFFFFF"
set output "orientation.png"

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
