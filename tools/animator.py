import numpy as np
from matplotlib import pyplot as plt
from matplotlib import animation
import sys

array = np.genfromtxt(sys.argv[1], delimiter=',')

# First set up the figure, the axis, and the plot element we want to animate
fig = plt.figure()

x = array[0:,0]
y = array[0:,1]
xdelta = 200
ydelta = 500
ax = plt.axes(xlim=(min(x)-xdelta,max(x)+xdelta), ylim=(min(y)-ydelta,max(y)+ydelta))
line, = ax.plot([], [], lw=2)

# initialization function: plot the background of each frame
def init():
    line.set_data([], [])
    return line,

# animation function.  This is called sequentially
def animate(i):
    x = array[0:i,0]
    y = array[0:i,1]
    line.set_data(x, y)
    return line,

# call the animator.  blit=True means only re-draw the parts that have changed.
anim = animation.FuncAnimation(fig, animate, init_func=init,
       frames=array.shape[0], interval=10, blit=False)

plt.grid()
plt.show()
