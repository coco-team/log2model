import numpy as np
from matplotlib import pyplot as plt
from matplotlib import animation
import sys

array = []
totalargs = len(sys.argv)
for i in xrange(1,totalargs):
    print sys.argv[i]
    array.append(np.genfromtxt(sys.argv[i], delimiter=','))


plotlays, plotcols = [2,5], ["black","red"]

# First set up the figure, the axis, and the plot element we want to animate
fig = plt.figure()


xmin = 0
xmax = 0
ymin = 0
ymax = 0
for data in array:
    currxmax = max(data[:,0])
    currxmin = min(data[:,0])
    if currxmax > xmax:
        xmax = currxmax
    if currxmin < xmin:
        xmin = currxmin
    currymax = max(data[:,1])
    currymin = min(data[:,1])
    if currymax > ymax:
        ymax = currymax
    if currymin < ymin:
        ymin = currymin
xdelta = 1000
ydelta = 1000
ax = plt.axes(xlim=(xmin - xdelta,xmax + xdelta), ylim=(ymin - ydelta ,ymax + ydelta))
line, = ax.plot([], [], lw=2)

# initialization function: plot the background of each frame
lines = []
for i in array:
    lobj = ax.plot([],[],lw=2)[0]
    lines.append(lobj)

def init():
    for line in lines:
        line.set_data([],[])
    return lines,

# animation function.  This is called sequentially
def animate(i):
    for s, data in enumerate(array):
        x = data[0:i,0]
        y = data[0:i,1]
        lines[s].set_data(x, y)
    return lines,

# call the animator.  blit=True means only re-draw the parts that have changed.
anim = animation.FuncAnimation(fig, animate, init_func=init,
       frames=10000, interval=10, blit=False)

plt.grid()
plt.show()
